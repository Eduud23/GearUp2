package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Forecast extends AppCompatActivity {

    private WebView webView;
    private Button btnCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup WebView
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://forecast-6rln.onrender.com/");

        // Initialize and handle button click
        btnCategory = findViewById(R.id.btnCategory);
        btnCategory.setOnClickListener(v -> {
            Intent intent = new Intent(Forecast.this, ForecastCategory.class);
            startActivity(intent);
        });
    }
}
