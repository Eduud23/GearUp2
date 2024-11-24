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

public class MeFragmentSeller extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, addressTextView;
    private Uri imageUri;
    private final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_me_seller, container, false);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.textView);
        emailTextView = view.findViewById(R.id.textView2);
        addressTextView = view.findViewById(R.id.addressTextView);

        // Load seller info
        loadSellerInfo();

        // Set up the upload button
        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> openImageChooser());

        // Set up the manage order button
        Button manageOrderButton = view.findViewById(R.id.manageOrderButton);
        manageOrderButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageOrderActivity.class);
            startActivity(intent);
        });

        // Set up the logout button
        view.findViewById(R.id.logoutbutton).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
            logoutUser();
        });

        return view;
    }

    /**
     * Load seller's information from Firestore
     */
    private void loadSellerInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Fetch seller's data from Firestore
            db.collection("sellers").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String fullName = firstName + " " + lastName;
                        String email = currentUser.getEmail();
                        String address = document.getString("address");
                        String profileImageUrl = document.getString("profileImageUrl");

                        // Set the data to the TextViews
                        nameTextView.setText(fullName);
                        emailTextView.setText(email);
                        addressTextView.setText(address != null ? address : "Address not available");

                        // Load profile image with Glide
                        if (profileImageUrl != null) {
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .into(profileImageView);
                        }
                    } else {
                        nameTextView.setText("No seller info found.");
                    }
                } else {
                    nameTextView.setText("Failed to load seller info.");
                }
            });
        }
    }

    /**
     * Open the image chooser to select a profile picture
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

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

    /**
     * Upload the selected image to Firebase Storage
     */
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
                    db.collection("sellers").document(userId)
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

    /**
     * Log out the user and navigate to the Login screen
     */
    private void logoutUser() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();  // Finish the current activity to prevent navigating back
        } else {
            Toast.makeText(getContext(), "Failed to logout: Activity not found", Toast.LENGTH_SHORT).show();
        }
    }
}
