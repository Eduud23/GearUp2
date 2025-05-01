package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Find the ImageView and Buttons
        ImageView gearImageView = findViewById(R.id.gear);
        Button signInButton = findViewById(R.id.btn1);
        Button registerButton = findViewById(R.id.btn2);

        // Load the rotation animation
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotation_animation);

        // Start the animation on the gear ImageView
        gearImageView.startAnimation(rotation);

        // Set onClickListeners for buttons to navigate to other activities
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Login activity
                Intent intent = new Intent(HomePage.this, Login.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ChooseUser activity
                Intent intent = new Intent(HomePage.this, ChooseUser.class);
                startActivity(intent);
            }
        });
    }
}
