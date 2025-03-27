package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServicesRecommendActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private EditText queryInput;
    private Button predictButton;
    private ImageButton  seeVideos, nearbyServicesButton, tutorialsButton;
    private TextView resultView;
    private final OkHttpClient client = new OkHttpClient();
    private static final String VERCEL_URL = "https://test-sage-nine-37.vercel.app/?q=";
    private static final String YOUTUBE_VERCEL_URL = "https://youtube-mu-one.vercel.app/search?query=";
    private static final String TIPS_URL = "https://stepbystep-iota.vercel.app/predict?q=";
    private FusedLocationProviderClient fusedLocationClient;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;
    private String prediction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_recommend);

        queryInput = findViewById(R.id.queryInput);
        predictButton = findViewById(R.id.predictButton);
        resultView = findViewById(R.id.resultView);
        seeVideos = findViewById(R.id.recommendedVideosButton);
        nearbyServicesButton = findViewById(R.id.recommendedServicesButton);
        tutorialsButton = findViewById(R.id.stepByStepTutorialsButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();

        predictButton.setOnClickListener(v -> {
            String userQuery = queryInput.getText().toString().trim();
            if (!userQuery.isEmpty()) {
                if (userLatitude != 0.0 && userLongitude != 0.0) {
                    new Thread(() -> {
                        prediction = askVercel(userQuery).trim();
                        runOnUiThread(() -> resultView.setText("Prediction: " + prediction));
                    }).start();
                } else {
                    getUserLocation();
                }
            }
        });

        nearbyServicesButton.setOnClickListener(v -> {
            if (!prediction.isEmpty()) {
                Intent intent = new Intent(ServicesRecommendActivity.this, RecommendedServicesActivity.class);
                intent.putExtra("prediction", prediction);
                intent.putExtra("userLatitude", userLatitude);
                intent.putExtra("userLongitude", userLongitude);
                startActivity(intent);
            }
        });

        seeVideos.setOnClickListener(v -> {
            String userQuery = queryInput.getText().toString().trim();
            String youtubeUrl = YOUTUBE_VERCEL_URL + userQuery;
            Intent intent = new Intent(ServicesRecommendActivity.this, VideoResultsActivity.class);
            intent.putExtra("youtubeUrl", youtubeUrl);
            startActivity(intent);
        });

        tutorialsButton.setOnClickListener(v -> {
            String userQuery = queryInput.getText().toString().trim();
            String tipsUrl = TIPS_URL + userQuery;
            Intent intent = new Intent(ServicesRecommendActivity.this, StepByStepActivity.class);
            intent.putExtra("tips", tipsUrl);
            startActivity(intent);
        });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLatitude = location.getLatitude();
                    userLongitude = location.getLongitude();
                }
            });
        } else {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            resultView.setText("Location permission is required to get nearby services.");
        }
    }

    private String askVercel(String prompt) {
        try {
            Request request = new Request.Builder()
                    .url(VERCEL_URL + prompt)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                JSONObject jsonResponse = new JSONObject(response.body().string());
                return jsonResponse.optString("prediction", "Error: No relevant services found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
