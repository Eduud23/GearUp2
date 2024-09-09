package com.example.gearup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Access the UI elements
        TextView welcomeText = view.findViewById(R.id.text_welcome);
        ImageView welcomeImage = view.findViewById(R.id.image_welcome);
        Button actionButton = view.findViewById(R.id.button_action);

        // Set up click listener for the button
        actionButton.setOnClickListener(v -> {
            // Handle button click
        });

        return view;
    }
}
