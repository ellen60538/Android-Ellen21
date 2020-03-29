package tw.org.iii.ellen.ellen21;
//WebView 嵌入html網頁畫面,網頁內部可與手機其他功能互動及傳遞資料(ex:相機、GPS)
//超級超級混亂的code....

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private WebView webView ;
    private EditText max ;
    private LocationManager lmgr ;
    private RequestQueue queue ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }else{
            init() ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) ;
        init() ;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (! lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.v("ellen","No GPS") ;
        }
    }

    private void init(){
        webView = findViewById(R.id.webView) ;
        max = findViewById(R.id.max) ;
        initWebView() ;

        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE) ;
        if (! lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //如果user的GPS沒開啟
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,123) ;
        }

        queue = Volley.newRequestQueue(this) ;
    }

    private void initWebView(){
        webView.setWebViewClient(new WebViewClient()) ;//扮演瀏覽器,這樣就不會跳轉到網頁
        WebSettings settings = webView.getSettings() ;
        settings.setJavaScriptEnabled(true) ;

        webView.addJavascriptInterface(new MyJSObject(),"myJSObject");


        //webView.loadUrl("http://www.iii.org.tw");
        webView.loadUrl("file:///android_asset/ellen.html");
    }

    @Override
    protected void onStart() {
        //GPS打開
        super.onStart();
        myListener = new MyListener() ;
        lmgr.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0,0,myListener) ;
    }

    private MyListener myListener ;

    private class MyListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude() ;
            double lng = location.getLongitude() ;
            Log.v("ellen",lat + "," + lng) ;
            Message message = new Message() ;
            Bundle data = new Bundle() ;
            data.putString("urname",lat + "," + lng) ;
            message.setData(data) ;
            uiHandler.sendMessage(message) ;
            webView.loadUrl(String.format("javascript:moveTo(%f,%f)",lat,lng));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lmgr.removeUpdates(myListener) ;    //GPS關掉
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack() ;
        }else {
            //TODO: 想先出現Toast,要連續按兩次返回才關閉APP
            super.onBackPressed();
        }
    }

    public void test1(View view) {
        String strMax = max.getText().toString() ;
        webView.loadUrl(String.format("javascript:test1(%s)",strMax)) ;
    }

    public class MyJSObject{
        @JavascriptInterface
        //定義在javascript會呼叫的方法
        public void callFromJS(String urname){
            Log.v("ellen","callFromJS: "+urname) ;
            Message message = new Message() ;
            Bundle data = new Bundle() ;
            data.putString("urname",urname) ;
            message.setData(data) ;
            uiHandler.sendMessage(message) ;
            //max.setText(urname) ; //可能會掛掉閃退,所以要透過Handler
        }
    }

    private UIHandler uiHandler = new UIHandler() ;
    private class UIHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg) ;
            String urname = msg.getData().getString("urname") ;
            //max.setText(urname) ;
        }
    }

    public void test2(View view) {
        String url="https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyCLk8W31pUZyUEwd2z6Wzld99iipFvo85Y" ;
        String url2 = String.format(url,max.getText().toString()) ;
        StringRequest request = new StringRequest(
                Request.Method.GET, url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJSON(response) ;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        queue.add(request) ;
    }

    private void parseJSON(String json){
        try{
            JSONObject root = new JSONObject(json) ;
            String status = root.getString("status") ;
            if (status.equals("OK")){
                JSONArray results = root.getJSONArray("results") ;
                JSONObject result = results.getJSONObject(0) ;
                JSONObject geometry = result.getJSONObject("geometry") ;
                JSONObject location = geometry.getJSONObject("location") ;
                double lat = location.getDouble("lat") ;
                double lng = location.getDouble("lng") ;
                Log.v("ellen","geocoding : " + lat + "," + lng) ;
                webView.loadUrl(String.format("javascript:moveTo(%f,%f)",lat,lng)) ;

            }else {
                Log.v("ellen","status = " + status) ;
            }


        }catch (Exception e){
            Log.v("ellen",e.toString()) ;
        }
    }

}
