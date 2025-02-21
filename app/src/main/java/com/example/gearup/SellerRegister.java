package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SellerRegister extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, firstNameEditText, lastNameEditText, phoneEditText, shopNameEditText, addressEditText, servicesEditText;
    private TextView tvLatitude, tvLongitude;
    private Button registerButton;
    private ImageButton locationButton;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI components
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

        locationButton.setOnClickListener(v -> {
            Intent intent = new Intent(SellerRegister.this, MapsActivity.class);
            startActivityForResult(intent, 100);
        });

        registerButton.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedLatitude = data.getDoubleExtra("latitude", 0);
            selectedLongitude = data.getDoubleExtra("longitude", 0);

            tvLatitude.setText(String.valueOf(selectedLatitude));
            tvLongitude.setText(String.valueOf(selectedLongitude));
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

        // Validate inputs
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
            Toast.makeText(SellerRegister.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SellerRegister.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(SellerRegister.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the button to prevent multiple clicks
        registerButton.setEnabled(false);

        // Register user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    registerButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Store additional user info in Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("firstName", firstName);
                        user.put("lastName", lastName);
                        user.put("phone", phone);
                        user.put("email", email);
                        user.put("shopName", shopName);
                        user.put("address", address);
                        user.put("services", services);
                        user.put("latitude", selectedLatitude);
                        user.put("longitude", selectedLongitude);
                        user.put("role", "seller");

                        db.collection("sellers").document(userId).set(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SellerRegister.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        navigateToLogin();
                                    } else {
                                        Toast.makeText(SellerRegister.this, "Failed to store user data: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SellerRegister.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SellerRegister.this, Login.class);
        startActivity(intent);
        finish();
    }
}