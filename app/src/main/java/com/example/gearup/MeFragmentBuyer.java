package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MeFragmentBuyer extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameTextView, emailTextView;
    private ImageView profileImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me_buyer, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        nameTextView = view.findViewById(R.id.textView);
        emailTextView = view.findViewById(R.id.textView2);
        profileImageView = view.findViewById(R.id.profileImageView);

        loadBuyerInfo();

        // Logout button click listener
        view.findViewById(R.id.logoutbutton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logout button clicked", Toast.LENGTH_SHORT).show();
            logoutUser();
        });

        // Edit Profile button click listener
        Button editButton = view.findViewById(R.id.editbutton);
        editButton.setOnClickListener(v -> navigateToEditProfile());

        // Order History button click listener
        Button orderHistoryButton = view.findViewById(R.id.orderHistoryButton);
        orderHistoryButton.setOnClickListener(v -> navigateToCartActivity());

        // Settings button click listener
        Button settingButton = view.findViewById(R.id.settingbutton);
        settingButton.setOnClickListener(v -> navigateToSettingsFragment());

        return view;
    }

    private void loadBuyerInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("buyers").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        String email = currentUser.getEmail();

                        nameTextView.setText(fullName);
                        emailTextView.setText(email);

                        // Load profile image if exists
                        String profileImageUrl = document.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);
                        }
                    } else {
                        nameTextView.setText("No buyer info found.");
                    }
                } else {
                    nameTextView.setText("Failed to load buyer info.");
                }
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        } else {
            Toast.makeText(getContext(), "Failed to logout: Activity not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(getActivity(), EditProfileBuyerActivity.class);
        startActivity(intent);
    }



    private void navigateToCartActivity() {
        Intent intent = new Intent(getActivity(), CartActivity.class);
        startActivity(intent);
    }

    private void navigateToSettingsFragment() {
        Fragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit();
    }
}
