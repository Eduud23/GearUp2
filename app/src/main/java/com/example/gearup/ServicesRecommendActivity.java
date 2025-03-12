package com.example.gearup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServicesRecommendActivity extends AppCompatActivity {

    private EditText queryInput;
    private Button predictButton;
    private TextView resultView;
    private RecyclerView recyclerView;
    private RecommendLocalShopAdapter adapter;
    private List<RecommendLocalShop> shopList = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();
    private static final String API_KEY = "AIzaSyAqN2a7lbuzQGe20b8cZ6UhMF2K9jHAIHs";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_recommend);

        queryInput = findViewById(R.id.queryInput);
        predictButton = findViewById(R.id.predictButton);
        resultView = findViewById(R.id.resultView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecommendLocalShopAdapter(shopList, this, shop -> {});
        recyclerView.setAdapter(adapter);

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = queryInput.getText().toString().trim();
                if (!userQuery.isEmpty()) {
                    new Thread(() -> {
                        String prediction = askGemini(userQuery).trim();
                        runOnUiThread(() -> {
                            resultView.setText(prediction);
                            DisplayMethodServices.getAutoPartsShops(shops -> {
                                List<RecommendLocalShop> filteredShops = new ArrayList<>();
                                for (RecommendLocalShop shop : shops) {
                                    if (shop.getKindOfService().equalsIgnoreCase(prediction)) {
                                        // Convert website field to string if it's not already
                                        Object websiteField = shop.getWebsite();
                                        if (websiteField != null) {
                                            shop.setWebsite(websiteField.toString());
                                        }
                                        filteredShops.add(shop);
                                    }
                                }
                                RecommendLocalShopAdapter newAdapter = new RecommendLocalShopAdapter(filteredShops, ServicesRecommendActivity.this, shop -> {});
                                recyclerView.setAdapter(newAdapter);
                            });
                        });
                    }).start();
                }
            }
        });
    }

    private String askGemini(String prompt) {
        String formattedPrompt = "The user describes a vehicle-related issue. Your task is to categorize it into the relevant services:\n" +
                "\n" +
                "Categories:\n" +
                "- Car Repair\n" +
                "- Auto Parts Store\n" +
                "- Fuel Station\n" +
                "- Towing Services\n" +
                "\n" +
                "If the issue is not related to any of the above services, respond with \"Error: No relevant services found.\"\n" +
                "\n" +
                "User Query: \"" + prompt + "\"\n" +
                "\n" +
                "Provide only the relevant service categories, nothing extra.";

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("contents", new JSONObject().put("parts", new JSONObject().put("text", formattedPrompt)));

            RequestBody body = RequestBody.create(jsonRequest.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                JSONObject jsonResponse = new JSONObject(response.body().string());
                String prediction = jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                return prediction.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
