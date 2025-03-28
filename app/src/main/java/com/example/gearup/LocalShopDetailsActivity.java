package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.util.Objects;

public class LocalShopDetailsActivity extends AppCompatActivity {

    private ImageView shopImage;
    private TextView shopName, kindOfRepair, timeSchedule, place, ratings;
    private Button callButton, websiteButton, navigateButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_shop_details);

        shopImage = findViewById(R.id.shopImage);
        shopName = findViewById(R.id.shopName);
        kindOfRepair = findViewById(R.id.kindOfRepair);
        timeSchedule = findViewById(R.id.timeSchedule);
        place = findViewById(R.id.place);
        ratings = findViewById(R.id.ratings);
        callButton = findViewById(R.id.callButton);
        websiteButton = findViewById(R.id.websiteButton);
        navigateButton = findViewById(R.id.navigateButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("shopName");
            String kind = intent.getStringExtra("kindOfRepair");
            String time = intent.getStringExtra("timeSchedule");
            String location = intent.getStringExtra("place");
            double rating = intent.getDoubleExtra("ratings", 0.0);
            String imageUrl = intent.getStringExtra("image");
            final String contactNumber = intent.getStringExtra("contactNumber");
            final String website = intent.getStringExtra("website");
            final double latitude = intent.getDoubleExtra("latitude", 0.0);
            final double longitude = intent.getDoubleExtra("longitude", 0.0);

            shopName.setText(name);
            kindOfRepair.setText(kind);
            timeSchedule.setText(time);
            place.setText(location);
            ratings.setText("Ratings: " + rating);

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.gear)
                    .error(R.drawable.gear)
                    .into(shopImage);

            // Call button action
            callButton.setOnClickListener(v -> {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contactNumber));
                startActivity(callIntent);
            });

            // Website button action
            websiteButton.setOnClickListener(v -> {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                startActivity(webIntent);
            });

            // Navigation button action
            navigateButton.setOnClickListener(v -> {
                String uri = "geo:" + latitude + "," + longitude + "?q=" + Uri.encode(name);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(mapIntent);
            });
        }
    }
}
