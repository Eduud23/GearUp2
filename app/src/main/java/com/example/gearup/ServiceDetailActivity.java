package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServiceDetailActivity extends AppCompatActivity {

    private static final String TAG = "ServiceDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        Log.d(TAG, "onCreate: ServiceDetailActivity started");

        // Identify service type
        boolean isGasStation = getIntent().getBooleanExtra("isGasStation", false);
        boolean isTowing = getIntent().getBooleanExtra("isTowing", false);
        boolean isLocalShop = getIntent().getBooleanExtra("isLocalShop", false);
        boolean isSmoke = getIntent().getBooleanExtra("isSmoke", false);
        boolean isParking = getIntent().getBooleanExtra("isParking", false);
        Log.d(TAG, "Service Type - GasStation: " + isGasStation + ", Towing: " + isTowing + ", LocalShop: " + isLocalShop + ", Smoke" + isSmoke + ", isParking" + isParking);

        // Find views
        ImageView serviceImage = findViewById(R.id.serviceImage);
        TextView serviceName = findViewById(R.id.serviceName);
        TextView serviceKind = findViewById(R.id.serviceKind);
        TextView timeScheduleText = findViewById(R.id.timeSchedule);
        TextView placeText = findViewById(R.id.place);

        TextView contactNumberText = findViewById(R.id.contactNumber);
        TextView ratingsText = findViewById(R.id.ratings);
        TextView websiteText = findViewById(R.id.website);
        TextView distanceText = findViewById(R.id.distance);
        Button callButton = findViewById(R.id.callButton);
        Button navigateButton = findViewById(R.id.navigateButton);
        Button visitWebsiteButton = findViewById(R.id.visitWebsiteButton);
        RecyclerView similarServicesRecycler = findViewById(R.id.similarServicesRecycler);
        TextView seeAllTextView = findViewById(R.id.seeAllTextView);
        TextView labelCall = findViewById(R.id.labelCall);
        TextView labelNavigate = findViewById(R.id.labelNavigate);
        TextView labelWebsite = findViewById(R.id.labelWebsite);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        // Get selected service and all services
        Object selectedService = getIntent().getSerializableExtra("selectedService");
        List<Object> allServices = (List<Object>) getIntent().getSerializableExtra("allServices");
        List<Object> similarServices = new ArrayList<>();

        if (allServices != null) {
            for (Object service : allServices) {
                if (!service.equals(selectedService)) {
                    if (isLocalShop && service instanceof RecommendLocalShop) {
                        RecommendLocalShop selected = (RecommendLocalShop) selectedService;
                        RecommendLocalShop shop = (RecommendLocalShop) service;
                        if (selected.getKindOfService().equals(shop.getKindOfService())) {
                            similarServices.add(shop);
                        }
                    } else if (isGasStation && service instanceof RecommendGasStation) {
                        similarServices.add(service);
                    } else if (isTowing && service instanceof RecommendTowing) {
                        similarServices.add(service);
                    }
                    else if (isSmoke && service instanceof RecommendSmokeService) {
                        similarServices.add(service);
                    }
                    else if (isParking && service instanceof RecommendParking) {
                        similarServices.add(service);
                    }
                }
            }
        }
        Log.d(TAG, "Similar services found: " + similarServices.size());

        // Set up RecyclerView with GridLayoutManager
        similarServicesRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        RecommendCombinedAdapter adapter = new RecommendCombinedAdapter(this, similarServices, selectedService);
        similarServicesRecycler.setAdapter(adapter);

        // Get data from intent
        String name = getIntent().getStringExtra("name");
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        String kindOfService = getIntent().getStringExtra("kindOfService");
        String place = getIntent().getStringExtra("place");
        float distance = getIntent().getFloatExtra("distance", 0.0f);
        String image = getIntent().getStringExtra("image");

        Log.d(TAG, "Service Details - Name: " + name + ", Kind: " + kindOfService + ", Distance: " + distance);

        // Set data to views
        serviceName.setText(name);
        serviceKind.setText(kindOfService);
        distanceText.setText("Distance: " + (distance >= 1000 ? String.format("%.2f km", distance / 1000) : String.format("%.0f m", distance)));

        // Load image using Glide
        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).placeholder(R.drawable.gear).into(serviceImage);
        } else {
            serviceImage.setImageResource(R.drawable.gear);
        }

        if (place != null && !place.isEmpty()) {
            placeText.setText("Place: " + place);
        } else {
            placeText.setVisibility(View.GONE);
        }

        String timeSchedule = getIntent().getStringExtra("timeSchedule");
        if (timeSchedule != null && !timeSchedule.isEmpty()) {
            timeScheduleText.setText("Time: " + timeSchedule);
        } else {
            timeScheduleText.setVisibility(View.GONE);
        }

        if (isGasStation) {
            contactNumberText.setVisibility(View.GONE);
            ratingsText.setVisibility(View.GONE);
            websiteText.setVisibility(View.GONE);
            callButton.setVisibility(View.GONE);
            visitWebsiteButton.setVisibility(View.GONE);

            String contactNumber = getIntent().getStringExtra("contactNumber");
            if (contactNumber != null && !contactNumber.isEmpty() && !contactNumber.equalsIgnoreCase("none")) {
                contactNumberText.setText("Contact: " + contactNumber);
                contactNumberText.setVisibility(View.VISIBLE);
                labelCall.setVisibility(View.VISIBLE);
            } else {
                labelCall.setVisibility(View.GONE);
            }

            String website = getIntent().getStringExtra("website");
            if (website != null && !website.isEmpty() && !website.equalsIgnoreCase("none")) {
                websiteText.setText("Website: " + website);
                websiteText.setVisibility(View.VISIBLE);
                labelWebsite.setVisibility(View.VISIBLE);
            } else {
                labelWebsite.setVisibility(View.GONE);
            }
            if (latitude != 0.0 && longitude != 0.0) {
                labelNavigate.setVisibility(View.VISIBLE);
            } else {
                labelNavigate.setVisibility(View.GONE);  // Hide the 'Navigate' label if no coordinates
            }
        } else {

        }


        visitWebsiteButton.setOnClickListener(v -> {
            String website = getIntent().getStringExtra("website");
            if (website != null && !website.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(browserIntent);
            }
        });
        // Call button listener
        callButton.setOnClickListener(v -> {
            String contactNumber = getIntent().getStringExtra("contactNumber");
            if (contactNumber != null && !contactNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactNumber));
                startActivity(intent);
            }
        });

        // Navigate button listener
        navigateButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        });
        if (similarServices.size() > 4) {
            seeAllTextView.setVisibility(View.VISIBLE);
        } else {
            seeAllTextView.setVisibility(View.GONE);
        }
        seeAllTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceDetailActivity.this, SeeAllServicesActivity.class);
            intent.putExtra("similarServices", (ArrayList<Object>) similarServices);
            intent.putExtra("selectedService", (Serializable) selectedService);

            startActivity(intent);
        });





    }

}