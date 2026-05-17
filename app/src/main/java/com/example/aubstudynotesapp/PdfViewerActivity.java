package com.example.aubstudynotesapp;

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

        webView.setWebViewClient(new WebViewClient());

        String fileUrl = getIntent().getStringExtra("fileUrl");

        if (fileUrl != null) {
            // ✅ FIXED - Google Docs viewer renders PDF/PPT from any public URL
            String googleDocsUrl = "https://docs.google.com/viewer?url=" + fileUrl + "&embedded=true";
            webView.loadUrl(googleDocsUrl);
        }
    }
}