package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ServiceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);

        // Identify service type
        boolean isGasStation = getIntent().getBooleanExtra("isGasStation", false);
        boolean isTowing = getIntent().getBooleanExtra("isTowing", false);
        boolean isLocalShop = getIntent().getBooleanExtra("isLocalShop", false);

        // Find views
        ImageView serviceImage = findViewById(R.id.serviceImage);
        TextView serviceName = findViewById(R.id.serviceName);
        TextView serviceKind = findViewById(R.id.serviceKind);
        TextView timeScheduleText = findViewById(R.id.timeSchedule);
        TextView contactNumberText = findViewById(R.id.contactNumber);
        TextView ratingsText = findViewById(R.id.ratings);
        TextView websiteText = findViewById(R.id.website);
        TextView distanceText = findViewById(R.id.distance);
        Button callButton = findViewById(R.id.callButton);
        Button navigateButton = findViewById(R.id.navigateButton);
        Button visitWebsiteButton = findViewById(R.id.visitWebsiteButton);

        // Get data from intent
        String name = getIntent().getStringExtra("name");
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        String kindOfService = getIntent().getStringExtra("kindOfService");
        String place = getIntent().getStringExtra("place");
        float distance = getIntent().getFloatExtra("distance", 0.0f);
        String image = getIntent().getStringExtra("image");

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

        // Handle gas station-specific details
        if (isGasStation) {
            timeScheduleText.setText("Place: " + (place != null ? place : "N/A"));
            contactNumberText.setVisibility(TextView.GONE);
            ratingsText.setVisibility(TextView.GONE);
            websiteText.setVisibility(TextView.GONE);
            callButton.setVisibility(Button.GONE);
            visitWebsiteButton.setVisibility(Button.GONE);
        }
        // Handle towing and local shop details
        else {
            // Time Schedule (Local Shops Only)
            if (isLocalShop) {
                timeScheduleText.setText("Hours: " + (getIntent().getStringExtra("timeSchedule") != null ? getIntent().getStringExtra("timeSchedule") : "N/A"));
            } else {
                timeScheduleText.setVisibility(TextView.GONE);
            }

            // Contact Number
            String contactNumber = getIntent().getStringExtra("contactNumber");
            if (contactNumber != null && !contactNumber.isEmpty()) {
                contactNumberText.setText("Contact: " + contactNumber);
            } else {
                contactNumberText.setVisibility(TextView.GONE);
                callButton.setVisibility(Button.GONE);
            }

            // Ratings
            double ratings = getIntent().getDoubleExtra("ratings", -1);
            if (ratings != -1) {
                ratingsText.setText("Ratings: " + ratings);
            } else {
                ratingsText.setVisibility(TextView.GONE);
            }

            // Website (Local Shops Only)
            String website = getIntent().getStringExtra("website");
            if (isLocalShop && website != null && !website.isEmpty()) {
                websiteText.setText(website);
            } else {
                websiteText.setText("No Website");
                visitWebsiteButton.setVisibility(Button.GONE);
            }
        }

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

        // Visit website button listener (Local Shops Only)
        visitWebsiteButton.setOnClickListener(v -> {
            String website = getIntent().getStringExtra("website");
            if (website != null && !website.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(browserIntent);
            }
        });
    }
}
