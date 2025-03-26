package com.example.gearup;

import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import androidx.appcompat.widget.Toolbar;

public class StepByStepActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_step_by_step);

        TextView tipsTextView = findViewById(R.id.tipsTextView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        // Get the tips URL (which gives the prediction) from intent
        String tipsUrl = getIntent().getStringExtra("tips");

        if (tipsUrl != null) {
            // Fetch prediction in a background thread
            new Thread(() -> {
                try {
                    URL url = new URL(tipsUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Extract tips from the JSON response
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String tips = jsonObject.getString("tips");

                    // Format tips for better readability:
                    String formattedTips = tips
                            .replace("**", "")  // Remove markdown bold
                            .replace(":", ":\n")  // Add newline after colon
                            .replaceAll("(?<=\\d)\\.\\s", ". ")  // No newline after "1." or "2."
                            .replaceAll("(?<!\\d)\\.\\s", ".\n\n")  // Add newlines after sentences, ignore numbers
                            .trim();  // Remove leading/trailing spaces

                    // Update the UI with the formatted prediction
                    runOnUiThread(() -> tipsTextView.setText(formattedTips));

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> tipsTextView.setText("Failed to get prediction."));
                }
            }).start();
        } else {
            tipsTextView.setText("No tips available.");
        }
    }
}
