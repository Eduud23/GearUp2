package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class LocalTrendsDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_local_trends_details);

        ImageView imageView = findViewById(R.id.product_image);
        TextView nameTextView = findViewById(R.id.product_name);
        TextView placeTextView = findViewById(R.id.product_place);
        TextView priceTextView = findViewById(R.id.product_price);
        TextView ratingsTextView = findViewById(R.id.product_ratings);
        TextView soldTextView = findViewById(R.id.product_sold);
        TextView promoTextView = findViewById(R.id.product_promo);
        Button openLinkButton = findViewById(R.id.open_link_button);

        // Get data from intent
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("image");
        String name = intent.getStringExtra("name");
        String place = intent.getStringExtra("place");
        double price = intent.getDoubleExtra("price", 0.0);
        double ratings = intent.getDoubleExtra("ratings", 0.0);
        String sold = intent.getStringExtra("sold");
        String promo = intent.getStringExtra("promo");
        String link = intent.getStringExtra("link");

        // Set data to views
        Glide.with(this).load(imageUrl).into(imageView);
        nameTextView.setText(name);
        placeTextView.setText("Location: " + place);
        priceTextView.setText("Price: ₱" + price);
        ratingsTextView.setText("Ratings: " + ratings);
        soldTextView.setText("Sold: " + sold);
        promoTextView.setText("Promo: " + (promo.isEmpty() ? "No Promo" : promo));

        // Open product link in browser
        openLinkButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        });
    }
}
