package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView eyeToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView newAcc = findViewById(R.id.newAccount);
        TextView forgot = findViewById(R.id.forgot);

        newAcc.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ChooseUser.class);
            startActivity(intent);
        });

        forgot.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgotPassword.class);
            startActivity(intent);
        });

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        eyeToggle = findViewById(R.id.eyeToggle);
        loginButton = findViewById(R.id.loginbtn);

        loginButton.setOnClickListener(v -> loginUser());
        eyeToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, get the user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        }
                    } else {
                        // If sign in fails, display a message to the user
                        Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void togglePasswordVisibility() {
        // Toggle the visibility of the password
        if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            // If password is hidden, show it
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            eyeToggle.setImageResource(R.drawable.eye); // Set the open eye icon
        } else {
            // If password is visible, hide it
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            eyeToggle.setImageResource(R.drawable.eye_close); // Set the closed eye icon
        }
        passwordEditText.setSelection(passwordEditText.getText().length()); // Keep the cursor at the end of the text
    }

    private void checkUserRole(String uid) {
        // Check if the user exists in the 'sellers' collection
        db.collection("sellers").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // User found in 'sellers' collection, now check the 'status' field
                            String status = document.getString("status");
                            if ("active".equals(status)) {
                                // Seller is active, proceed to seller home page
                                navigateToHomePage("seller");
                            } else {
                                // Seller is not active
                                Toast.makeText(Login.this, "Your account is not active. Please contact support.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // User not found in 'sellers', check 'buyers' collection
                            db.collection("buyers").document(uid).get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot doc = task1.getResult();
                                            if (doc != null && doc.exists()) {
                                                // User found in 'buyers' collection
                                                navigateToHomePage("buyer");
                                            } else {
                                                // User not found in either collection
                                                Toast.makeText(Login.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(Login.this, "Failed to retrieve user role: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Login.this, "Failed to retrieve user role: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHomePage(String role) {
        if ("seller".equals(role)) {
            Intent intent = new Intent(Login.this, HomePageSeller.class);
            startActivity(intent);
            finish();
        } else if ("buyer".equals(role)) {
            Intent intent = new Intent(Login.this, HomePageBuyer.class);
            startActivity(intent);
            finish();
        }
    }
}
