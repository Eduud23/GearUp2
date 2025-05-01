package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileSellerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private TextInputEditText firstNameEditText, lastNameEditText, phoneEditText, shopNameEditText, addressEditText, emailEditText;
    private TextInputEditText latitudeEditText, longitudeEditText;
    private TextInputEditText servicesEditText;

    private Button updateButton, pinLocationButton;
    private TextView userNameTextView, userEmailTextView;
    private ImageView profileImageView;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_LOCATION_REQUEST = 2;
    private Uri profileImageUri;
    private double latitude, longitude;

    private FrameLayout progressBarContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile_seller);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        bindViews();
        loadSellerData();

        updateButton.setOnClickListener(v -> updateProfile());
        profileImageView.setOnClickListener(v -> openGallery());
        pinLocationButton.setOnClickListener(v -> openLocationPicker());

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void bindViews() {
        profileImageView = findViewById(R.id.imageProfile);
        firstNameEditText = findViewById(R.id.etfn);
        lastNameEditText = findViewById(R.id.etln);
        phoneEditText = findViewById(R.id.etphone);
        shopNameEditText = findViewById(R.id.etshop);
        addressEditText = findViewById(R.id.etaddress);
        emailEditText = findViewById(R.id.etEmail);
        servicesEditText = findViewById(R.id.etservices);

        latitudeEditText = findViewById(R.id.etLatitude);
        longitudeEditText = findViewById(R.id.etLongitude);
        latitudeEditText.setEnabled(false);
        longitudeEditText.setEnabled(false);

        updateButton = findViewById(R.id.btnUpdate);
        pinLocationButton = findViewById(R.id.pinLocation);
        userNameTextView = findViewById(R.id.tvUserName);
        userEmailTextView = findViewById(R.id.tvUserEmail);

        progressBarContainer = findViewById(R.id.progress_bar_container);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadSellerData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("DEBUG", "User not authenticated");
            return;
        }

        db.collection("sellers").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> sellerData = task.getResult().getData();
                        if (sellerData != null) {
                            populateSellerData(sellerData);
                        }
                    } else {
                        Log.e("DEBUG", "Error loading seller data", task.getException());
                    }
                });
    }

    private void populateSellerData(Map<String, Object> sellerData) {
        String firstName = (String) sellerData.get("firstName");
        String lastName = (String) sellerData.get("lastName");
        String phone = (String) sellerData.get("phone");
        String shopName = (String) sellerData.get("shopName");
        String address = (String) sellerData.get("address");
        String services = (String) sellerData.get("services");
        String profileImageUrl = (String) sellerData.get("profileImageUrl");

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        phoneEditText.setText(phone);
        shopNameEditText.setText(shopName);
        addressEditText.setText(address);
        servicesEditText.setText(services);
        emailEditText.setText(mAuth.getCurrentUser().getEmail());

        userNameTextView.setText(String.format("%s %s", firstName, lastName));
        userEmailTextView.setText(mAuth.getCurrentUser().getEmail());

        if (sellerData.containsKey("latitude") && sellerData.containsKey("longitude")) {
            latitude = (double) sellerData.get("latitude");
            longitude = (double) sellerData.get("longitude");
            latitudeEditText.setText(String.valueOf(latitude));
            longitudeEditText.setText(String.valueOf(longitude));
        }

        if (profileImageUrl != null) {
            Glide.with(this).load(profileImageUrl).into(profileImageView);
        }
    }

    private void updateProfile() {
        progressBarContainer.setVisibility(View.VISIBLE);

        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String shopName = shopNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String services = servicesEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(shopName) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            progressBarContainer.setVisibility(View.GONE);
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> sellerUpdates = new HashMap<>();
        sellerUpdates.put("firstName", firstName);
        sellerUpdates.put("lastName", lastName);
        sellerUpdates.put("phone", phone);
        sellerUpdates.put("shopName", shopName);
        sellerUpdates.put("address", address);
        sellerUpdates.put("services", services);
        sellerUpdates.put("latitude", latitude);
        sellerUpdates.put("longitude", longitude);

        if (profileImageUri != null) {
            uploadProfileImage(uid, sellerUpdates);
        } else {
            updateFirestoreProfile(uid, sellerUpdates);
        }
    }

    private void uploadProfileImage(String uid, Map<String, Object> sellerUpdates) {
        StorageReference storageRef = storage.getReference().child("profileImages/" + uid + ".jpg");

        storageRef.putFile(profileImageUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            sellerUpdates.put("profileImageUrl", uri.toString());
                            updateFirestoreProfile(uid, sellerUpdates);
                        });
                    } else {
                        Toast.makeText(this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                        progressBarContainer.setVisibility(View.GONE);
                    }
                });
    }

    private void updateFirestoreProfile(String uid, Map<String, Object> sellerUpdates) {
        db.collection("sellers").document(uid).update(sellerUpdates)
                .addOnCompleteListener(task -> {
                    progressBarContainer.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG", "Error updating profile", task.getException());
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openLocationPicker() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivityForResult(intent, PICK_LOCATION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        } else if (requestCode == PICK_LOCATION_REQUEST && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            latitudeEditText.setText(String.valueOf(latitude));
            longitudeEditText.setText(String.valueOf(longitude));
            Log.d("Location", "Updated: Lat=" + latitude + ", Lon=" + longitude);
        }
    }
}
