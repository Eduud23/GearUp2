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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MeFragmentBuyer extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;  // Define the constant for image picking
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameTextView, emailTextView;
    private ImageView profileImageView; // Add the ImageView for the profile image
    private Uri imageUri; // Store the selected image URI

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me_buyer, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize the UI components
        nameTextView = view.findViewById(R.id.textView);
        emailTextView = view.findViewById(R.id.textView2);
        profileImageView = view.findViewById(R.id.profileImageView); // Initialize ImageView for profile image

        loadBuyerInfo();

        // Set up the upload button to open image chooser
        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> openImageChooser());

        // Logout button click listener
        view.findViewById(R.id.logoutbutton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logout button clicked", Toast.LENGTH_SHORT).show();
            logoutUser();
        });

        // Edit Profile button click listener
        Button editButton = view.findViewById(R.id.editbutton);
        editButton.setOnClickListener(v -> navigateToEditProfile());

        Button orderHistoryButton = view.findViewById(R.id.orderHistoryButton);
        orderHistoryButton.setOnClickListener(v -> navigateToCartActivity());

        // Account Settings button click listener
        Button settingButton = view.findViewById(R.id.settingbutton);
        settingButton.setOnClickListener(v -> navigateToSettingsFragment());

        return view;
    }

    // Load buyer information (name and email)
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

    // Open image chooser to pick a profile image
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle the image selected from the chooser
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
                imageUri = data.getData();
                profileImageView.setImageURI(imageUri);  // Display the selected image in ImageView
                uploadImageToFirebase(imageUri);  // Upload the image to Firebase Storage
            } else {
                Toast.makeText(getContext(), "Image selection failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Upload the selected image to Firebase Storage
    private void uploadImageToFirebase(Uri uri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference("profile_images/" + userId + ".jpg");

            storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                // Get the download URL and save it to Firestore
                storageReference.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                    db.collection("buyers").document(userId)
                            .update("profileImageUrl", downloadUrl.toString())
                            .addOnSuccessListener(aVoid -> {
                                // Update the UI with the new profile image
                                Glide.with(this)
                                        .load(downloadUrl)
                                        .into(profileImageView);
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Logout the user
    private void logoutUser() {
        mAuth.signOut();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
            getActivity().finish();
        } else {
            Toast.makeText(getContext(), "Failed to logout: Activity not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigate to EditProfileBuyerFragment
    private void navigateToEditProfile() {
        // Replace current fragment with EditProfileBuyerFragment
        Fragment editProfileFragment = new EditProfileBuyerFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editProfileFragment)
                .addToBackStack(null)  // Add transaction to back stack (so the user can navigate back)
                .commit();
    }
    private void navigateToCartActivity() {
        // Navigate to CartActivity where the order history is displayed
        Intent intent = new Intent(getActivity(), RecommendationActivity.class);
        startActivity(intent);
    }

    // Navigate to SettingsFragment
    private void navigateToSettingsFragment() {
        // Replace current fragment with SettingsFragment
        Fragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)  // Add transaction to back stack (so the user can navigate back)
                .commit();
    }
}
