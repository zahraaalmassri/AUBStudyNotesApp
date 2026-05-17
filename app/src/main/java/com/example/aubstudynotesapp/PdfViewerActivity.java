package com.example.aubstudynotesapp;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class PdfViewerActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setLoadWithOverviewMode(true);

        settings.setUseWideViewPort(true);

        settings.setBuiltInZoomControls(true);

        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient());

        String fileUri =
                getIntent().getStringExtra("fileUri");

        if (fileUri != null) {

            String googleDocsUrl =
                    "https://docs.google.com/gview?embedded=true&url="
                            + Uri.encode(fileUri);

            webView.loadUrl(googleDocsUrl);
        }
    }
}