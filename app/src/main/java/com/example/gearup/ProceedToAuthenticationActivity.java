package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProceedToAuthenticationActivity extends AppCompatActivity {

    private static final int REQUEST_PHOTO = 101;
    private static final int REQUEST_ID = 102;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ImageView ivSellerPhoto, ivSellerID;
    private Button btnUploadPhoto, btnUploadID, btnSubmit;
    private Uri sellerPhotoUri, sellerIDUri;

    // FrameLayout for progress bar
    private FrameLayout progressBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proceed_to_authentication);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind views
        ivSellerPhoto = findViewById(R.id.ivSellerPhoto);
        ivSellerID = findViewById(R.id.ivSellerID);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnUploadID = findViewById(R.id.btnUploadID);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Bind progress bar container
        progressBarContainer = findViewById(R.id.progress_bar_container);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());

        // Get data from Intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String phone = intent.getStringExtra("phone");
        String shopName = intent.getStringExtra("shopName");
        String address = intent.getStringExtra("address");
        String services = intent.getStringExtra("services");
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);

        // Upload image button click listeners
        btnUploadPhoto.setOnClickListener(v -> openImageSelector(REQUEST_PHOTO));
        btnUploadID.setOnClickListener(v -> openImageSelector(REQUEST_ID));

        // Submit registration
        btnSubmit.setOnClickListener(v -> {
            showProgressBar(true); // Show progress bar before starting submission
            submitRegistration(email, password, firstName, lastName, phone, shopName, address, services, latitude, longitude);
        });
    }

    private void openImageSelector(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == REQUEST_PHOTO) {
                sellerPhotoUri = selectedImageUri;
                ivSellerPhoto.setImageURI(sellerPhotoUri);
            } else if (requestCode == REQUEST_ID) {
                sellerIDUri = selectedImageUri;
                ivSellerID.setImageURI(sellerIDUri);
            }
        }
    }

    private void submitRegistration(String email, String password, String firstName, String lastName,
                                    String phone, String shopName, String address, String services,
                                    double latitude, double longitude) {

        if (sellerPhotoUri == null || sellerIDUri == null) {
            showProgressBar(false); // Hide progress bar if conditions aren't met
            Toast.makeText(this, "Please upload both photo and ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Create Firebase Auth account first
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Step 2: Continue with image upload
                StorageReference storageReference = storage.getReference();
                StorageReference photoRef = storageReference.child("seller_photos/" + System.currentTimeMillis() + ".jpg");
                StorageReference idRef = storageReference.child("seller_ids/" + System.currentTimeMillis() + ".jpg");

                photoRef.putFile(sellerPhotoUri).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        photoRef.getDownloadUrl().addOnSuccessListener(photoUri -> {
                            idRef.putFile(sellerIDUri).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    idRef.getDownloadUrl().addOnSuccessListener(idUri -> {
                                        createUserInFirestore(email, firstName, lastName, phone, shopName, address,
                                                services, latitude, longitude, photoUri.toString(), idUri.toString());
                                    });
                                } else {
                                    showProgressBar(false);
                                    Toast.makeText(this, "ID Upload Failed: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    } else {
                        showProgressBar(false);
                        Toast.makeText(this, "Photo Upload Failed: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                showProgressBar(false);
                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressBar(boolean show) {
        if (show) {
            progressBarContainer.setVisibility(View.VISIBLE);  // Show the progress bar container
        } else {
            progressBarContainer.setVisibility(View.GONE);  // Hide the progress bar container
        }
    }

    private void createUserInFirestore(String email, String firstName, String lastName, String phone,
                                       String shopName, String address, String services,
                                       double latitude, double longitude, String photoUrl, String idUrl) {

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("email", email);
        user.put("shopName", shopName);
        user.put("address", address);
        user.put("services", services);
        user.put("latitude", latitude);
        user.put("longitude", longitude);
        user.put("role", "seller");
        user.put("sold", 0);
        user.put("review", 0.0);
        user.put("photoUrl", photoUrl);
        user.put("idUrl", idUrl);
        user.put("status", "pending"); // Admin will review

        db.collection("sellers").document(userId).set(user)
                .addOnCompleteListener(task -> {
                    showProgressBar(false); // Hide progress bar once registration is complete
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Registration submitted for admin review", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(this, "Failed to save user: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, Login.class));
        finish();
    }
}
