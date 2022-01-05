package com.example.webfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    // creating a variable for our Firebase Database.
    FirebaseDatabase firebaseDatabase;
    // creating a variable for our Database
    // Reference for Firebase.
    DatabaseReference databaseReference;

    // creating a variable for our webview
    private WebView webView;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;


    private FirebaseAnalytics mFirebaseAnalytics;

    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ///The method for Svse instnace ( for SCreen Roataion ) are created bewlow

        if (savedInstanceState != null) {
            ((WebView) findViewById(R.id.myWebView)).restoreState(savedInstanceState.getBundle("webViewState"));
        } else {
            webView = (WebView) findViewById(R.id.myWebView);
            load(); // create seprate functin to loadweb
        }


        //////


        progressBar = findViewById(R.id.progressBar);


        /// for analyics purspose
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        /// conncecting our offline from the class created data

        reference = FirebaseDatabase.getInstance().getReference("url");
        reference.keepSynced(true);

        // initializing variable for web view.
        webView = findViewById(R.id.myWebView);
        // below line is used to get the instance
        // of our Firebase database.
        firebaseDatabase = FirebaseDatabase.getInstance();
        // below line is used to get reference for our database.
        databaseReference = firebaseDatabase.getReference("url");

        // calling method to initialize
        // our web view.
        initializeWebView(); //// callling the method


        ////


        webView.setDownloadListener(
                new DownloadListener() {
                    @Override
                    public void onDownloadStart(final String s, final String s1, final String s2, final String s3, long l) {

                        Dexter.withActivity(MainActivity.this)
                                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .withListener(new PermissionListener() {
                                    @Override
                                    public void onPermissionGranted(PermissionGrantedResponse response) {


                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
                                        request.setMimeType(s3);
                                        String cookies = CookieManager.getInstance().getCookie(s);
                                        request.addRequestHeader("cookie", cookies);
                                        request.addRequestHeader("User-Agent", s1);
                                        request.setDescription("Downloading File.....");
                                        request.setTitle(URLUtil.guessFileName(s, s2, s3));
                                        request.allowScanningByMediaScanner();
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        request.setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                                        s, s2, s3));
                                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                        downloadManager.enqueue(request);
                                        Toast.makeText(MainActivity.this, "Downloading File..", Toast.LENGTH_SHORT).show();



                                    }

                                    @Override
                                    public void onPermissionDenied(PermissionDeniedResponse response) {
                                    }


                                    @Override
                                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                        token.continuePermissionRequest();
                                    }
                                }).check();
                    }

                });


        /// START SWIPE


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });


        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_orange_dark),
                getResources().getColor(android.R.color.holo_green_dark),
                getResources().getColor(android.R.color.holo_red_dark)
        );


        //solved problem on swipe

        //Solved WebView SwipeUp Problem
        webView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (webView.getScrollY() == 0) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });


        ///ENd SWIPE


    }

    private void initializeWebView() {

        // calling add value event listener method for getting the values from database.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // this method is call to get the realtime updates in the data.
                // this method is called when the data is changed in our Firebase console.
                // below line is for getting the data from snapshot of our database.
                String webUrl = snapshot.getValue(String.class);
                // after getting the value for our webview url we are
                // setting our value to our webview view in below line.
                webView.loadUrl(webUrl);
                webView.getSettings().setJavaScriptEnabled(true);

                ///Intent intent = new Intent(getApplicationContext(), MyWebcview.class);
               // intent.putExtra("URL", webUrl);
                //startActivityForResult(intent, 1);



                //modified web client
                webView.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        swipeRefreshLayout.setRefreshing(false);
                        super.onPageFinished(view, url);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);

                      //  Intent intent = new Intent(view.getContext(), MyWebcview.class);
                        //startActivity(intent);

                        Intent intent = new Intent(getApplicationContext(), MyWebcview.class);
                        intent.putExtra("URL", url);
                        startActivityForResult(intent, 0);

                        webView.stopLoading();
                        webView.getSettings().getSaveFormData();

                      

                        return true;
                    }



                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        super.onReceivedError(view, request, error);
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();

                    }
                });


                ///// we set our   web view chrome client to control our ui
                webView.setWebChromeClient(new WebChromeClient() {

                    @Override
                    public void onProgressChanged(WebView view, int newProgress) {

                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(newProgress);
                        setTitle("Loading...");
                        // progressDialog.show();
                        if (newProgress == 100) {

                            progressBar.setVisibility(View.GONE);
                            setTitle(view.getTitle());
                            ///progressDialog.dismiss();

                        }

                        super.onProgressChanged(view, newProgress);

                    }


                });


                //end of chrome client

                ///Defining the settings of the web view

                webView.getSettings().setAppCacheMaxSize(900 * 1024 * 1024); // 5MB
                webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath() + "cache");
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAppCacheEnabled(true);
                webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

                ///for downlao
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setLoadsImagesAutomatically(true);


                if (!isNetworkAvailable()) { // loading offline
                    webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(MainActivity.this, "Fail to get URL.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();

        else {
            super.onBackPressed();
        }
    }


    //Method for Network activity

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();


    }




//// This method is respnisble to save instnce if SCREEN is Rotated

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        webView.saveState(bundle);
        outState.putBundle("webViewState", bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        Bundle bundle = new Bundle();
        webView.saveState(bundle);
        state.putBundle("webViewState", bundle);
    }


////

    public void load() {
        webView = (WebView) findViewById(R.id.myWebView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl("url");


    }

}



