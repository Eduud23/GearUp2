package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Forecast extends AppCompatActivity {

    private static final String TAG = "Forecasting";
    private WebView webView;
    private Button btnDrySeason, btnRainySeason, btnNextMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://forecast-6rln.onrender.com/");

        // Initialize buttons
        btnDrySeason = findViewById(R.id.btnDrySeason);
        btnRainySeason = findViewById(R.id.btnRainySeason);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        btnDrySeason.setOnClickListener(v -> openForecastCategory("Dry Season"));
        btnRainySeason.setOnClickListener(v -> openForecastCategory("Rainy Season"));
        btnNextMonth.setOnClickListener(v -> openForecastCategory("Next Month"));
    }

    private void openForecastCategory(String season) {
        Intent intent = new Intent(Forecast.this, ForecastCategory.class);
        intent.putExtra("productTitle", season); // Only pass the title/season
        startActivity(intent);
    }

}
