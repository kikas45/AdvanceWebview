package com.example.webfirestore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebcview extends AppCompatActivity {

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_webcview);

        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();
        String web = bundle.getString("URL");

        webView = findViewById(R.id.myWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(web);
       // Intent in = getIntent();

       // String url = getIntent().getStringExtra("URL");
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
      finish();
    }
}

