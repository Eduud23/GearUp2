package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellerRegister extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, firstNameEditText, lastNameEditText, phoneEditText, shopNameEditText, addressEditText, servicesEditText;
    private TextView tvLatitude, tvLongitude;
    private Button registerButton;
    private ImageButton locationButton;
    private Button selectImageButton;

    private ImageView imgProfilePreview;


    private Uri selectedImageUri = null;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    private static final int IMAGE_PICK_CODE = 101;
    private static final int LOCATION_PICK_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind UI elements
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmpassword);
        firstNameEditText = findViewById(R.id.etfn);
        lastNameEditText = findViewById(R.id.etln);
        phoneEditText = findViewById(R.id.etphone);
        shopNameEditText = findViewById(R.id.etshop);
        addressEditText = findViewById(R.id.etaddress);
        servicesEditText = findViewById(R.id.etservices);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        registerButton = findViewById(R.id.btnregister);
        locationButton = findViewById(R.id.imgAddress);
        selectImageButton = findViewById(R.id.btnSelectImage);
        imgProfilePreview = findViewById(R.id.imgProfile);


        // Choose profile image
        selectImageButton.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT);
            pickImage.setType("image/*");
            startActivityForResult(pickImage, IMAGE_PICK_CODE);
        });



        // Open map to select location
        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(SellerRegister.this, MapsActivity.class);
            startActivityForResult(intent, LOCATION_PICK_CODE);
        });

        // Register seller
        registerButton.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0);
            selectedLongitude = data.getDoubleExtra("longitude", 0);
            tvLatitude.setText(String.valueOf(selectedLatitude));
            tvLongitude.setText(String.valueOf(selectedLongitude));
        }

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgProfilePreview.setImageURI(selectedImageUri); // Display the image
            Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
        }
    }

        private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String shopName = shopNameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String services = servicesEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword) || !password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(shopName) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        if (selectedImageUri != null) {
                            uploadProfileImage(userId, selectedImageUri, imageUrl -> {
                                saveSellerToFirestore(userId, email, firstName, lastName, phone, shopName, address, services, imageUrl);
                            });
                        } else {
                            saveSellerToFirestore(userId, email, firstName, lastName, phone, shopName, address, services, null);
                        }
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadProfileImage(String userId, Uri imageUri, OnImageUploadCallback callback) {
        String fileName = "sellers/" + userId + "/" + UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(imageUri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String imageUrl = task.getResult().toString();
                callback.onUploadSuccess(imageUrl);
            } else {
                Toast.makeText(this, "Image Upload Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveSellerToFirestore(String userId, String email, String firstName, String lastName, String phone, String shopName, String address, String services, @Nullable String imageUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("phone", phone);
        user.put("shopName", shopName);
        user.put("address", address);
        user.put("services", services);
        user.put("latitude", selectedLatitude);
        user.put("longitude", selectedLongitude);
        user.put("role", "seller");
        user.put("sold", 0);
        user.put("review", 0.0);
        user.put("status", "pending");
        if (imageUrl != null) {
            user.put("profileImageUrl", imageUrl);
        }

        db.collection("sellers").document(userId).set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Wait for Confirmation", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void navigateToLogin() {
        startActivity(new Intent(SellerRegister.this, Login.class));
        finish();
    }

    interface OnImageUploadCallback {
        void onUploadSuccess(String imageUrl);
    }
}
