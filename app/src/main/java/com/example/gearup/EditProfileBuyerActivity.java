package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EditProfileBuyerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextInputEditText firstNameEditText, lastNameEditText, mobileEditText, addressEditText, emailEditText;
    private TextInputEditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button updateButton;
    private TextView userNameTextView, userEmailTextView;
    private ImageView profileImageView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    // Add reference for progress bar container
    private FrameLayout progressBarContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile_buyer); // Ensure the layout file has the progress bar

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind views
        bindViews();

        // Load and display user data
        loadUserData();

        // Handle update button click
        updateButton.setOnClickListener(v -> updateProfileAndPassword());

        // Back button logic
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void bindViews() {
        // Bind views from XML
        firstNameEditText = findViewById(R.id.etFirstName);
        lastNameEditText = findViewById(R.id.etLastName);
        mobileEditText = findViewById(R.id.etPhoneNumber);
        addressEditText = findViewById(R.id.etAddress);
        emailEditText = findViewById(R.id.etEmail);
        currentPasswordEditText = findViewById(R.id.etCurrentPassword);
        newPasswordEditText = findViewById(R.id.etNewPassword);
        confirmNewPasswordEditText = findViewById(R.id.etConfirmNewPassword);
        updateButton = findViewById(R.id.btnUpdate);
        userNameTextView = findViewById(R.id.tvUserName);
        userEmailTextView = findViewById(R.id.tvUserEmail);
        profileImageView = findViewById(R.id.imageProfile);

        // Bind the progress bar and container
        progressBarContainer = findViewById(R.id.progress_bar_container);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("DEBUG", "User not authenticated");
            return;
        }

        // Display progress bar while loading data
        showProgressBar();

        db.collection("buyers").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    // Hide progress bar once data is loaded
                    hideProgressBar();

                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> userData = task.getResult().getData();
                        if (userData != null) {
                            populateUserData(userData);
                        } else {
                            Log.e("DEBUG", "No user data found");
                        }
                    } else {
                        Log.e("DEBUG", "Error loading user data", task.getException());
                    }
                });
    }

    public void openImagePicker(View view) {
        // Create an intent to pick an image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private void populateUserData(Map<String, Object> userData) {
        String firstName = (String) userData.get("firstName");
        String lastName = (String) userData.get("lastName");
        String mobile = (String) userData.get("mobileNumber");
        String email = (String) userData.get("email");
        String profileImageUrl = (String) userData.get("profileImageUrl");

        // Set the user data to respective views
        userNameTextView.setText(String.format("%s %s", firstName, lastName));
        userEmailTextView.setText(email);

        // Fill edit text fields with user data
        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        mobileEditText.setText(mobile);
        emailEditText.setText(email);

        // Load profile image using Glide
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(profileImageUrl)
                    .into(profileImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.gear)
                    .into(profileImageView);
        }
    }

    private void updateProfileAndPassword() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String mobile = mobileEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && TextUtils.isEmpty(mobile)
                && TextUtils.isEmpty(address) && TextUtils.isEmpty(email) && TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, "At least one field must be updated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar before starting the update process
        showProgressBar();

        // Update profile in Firestore
        if (!TextUtils.isEmpty(firstName) || !TextUtils.isEmpty(lastName) || !TextUtils.isEmpty(mobile) || !TextUtils.isEmpty(address) || !TextUtils.isEmpty(email)) {
            updateUserProfile(firstName, lastName, mobile, address, email);
        }

        // If the user has selected a new profile image, upload it
        if (imageUri != null) {
            uploadProfileImage();
        }

        // Update password if provided
        if (!TextUtils.isEmpty(currentPassword) || !TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) {
            updatePassword(currentPassword, newPassword, confirmNewPassword);
        }
    }

    private void updateUserProfile(String firstName, String lastName, String mobile, String address, String email) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> userUpdates = new HashMap<>();
        if (!TextUtils.isEmpty(firstName)) userUpdates.put("firstName", firstName);
        if (!TextUtils.isEmpty(lastName)) userUpdates.put("lastName", lastName);
        if (!TextUtils.isEmpty(mobile)) userUpdates.put("mobileNumber", mobile);
        if (!TextUtils.isEmpty(email)) userUpdates.put("email", email);

        if (!TextUtils.isEmpty(address)) {
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("fullName", firstName + " " + lastName);
            addressMap.put("mobileNumber", mobile);
            addressMap.put("address", address);

            userUpdates.put("addresses", Collections.singletonList(addressMap));
        }

        db.collection("buyers").document(uid).update(userUpdates)
                .addOnCompleteListener(task -> {
                    // Hide the progress bar after the task is complete
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG", "Error updating profile", task.getException());
                    }
                });
    }

    private void uploadProfileImage() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images")
                    .child(mAuth.getCurrentUser().getUid() + ".jpg");

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            updateUserProfileImage(profileImageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG", "Error uploading image", e);
                    });
        }
    }

    private void updateUserProfileImage(String profileImageUrl) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("profileImageUrl", profileImageUrl);

        db.collection("buyers").document(uid).update(userUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG", "Error updating profile image", task.getException());
                    }
                });
    }

    private void updatePassword(String currentPassword, String newPassword, String confirmNewPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (!TextUtils.isEmpty(currentPassword) && !TextUtils.isEmpty(newPassword) && newPassword.equals(confirmNewPassword)) {
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword))
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPassword)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                                Log.e("DEBUG", "Error updating password", updateTask.getException());
                                            }
                                        });
                            } else {
                                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProgressBar() {
        progressBarContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBarContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
}
