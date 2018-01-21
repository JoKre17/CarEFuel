package carefuel.carefuel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private String url = "https://carefuel.ddns.net/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createWebView();
    }

    private void createWebView(){
        this.webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        this.webView.loadUrl(this.url);
        this.webView.setWebViewClient(new WebViewClient());

    }

    @Override
    public void onBackPressed(){
        if (this.webView.canGoBack()){
            this.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
