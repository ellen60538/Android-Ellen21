package tw.org.iii.ellen.ellen21;
//WebView 嵌入html網頁畫面,網頁內部可與手機其他功能互動及傳遞資料(ex:相機、GPS)

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private WebView webView ;
    private EditText max ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView) ;
        max = findViewById(R.id.max) ;
        initWebView() ;
    }

    private void initWebView(){
        webView.setWebViewClient(new WebViewClient()) ;//扮演瀏覽器,這樣就不會跳轉到網頁
        WebSettings settings = webView.getSettings() ;
        settings.setJavaScriptEnabled(true) ;
        //webView.loadUrl("http://www.iii.org.tw");
        webView.loadUrl("file:///android_asset/ellen.html");
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
}
