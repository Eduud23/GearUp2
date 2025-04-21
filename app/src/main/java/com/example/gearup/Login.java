package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView newAcc = findViewById(R.id.newAccount);

        newAcc.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ChooseUser.class);
            startActivity(intent);
        });

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginbtn);

        loginButton.setOnClickListener(v -> loginUser());
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


    private void checkUserRole(String uid) {
        // Check if the user exists in the 'sellers' collection
        db.collection("sellers").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // User found in 'sellers' collection
                            String status = document.getString("status"); // Get status from seller document

                            // Check if the seller is approved
                            if ("approved".equalsIgnoreCase(status)) {
                                navigateToHomePage("seller");
                            } else if ("pending".equalsIgnoreCase(status)) {
                                Toast.makeText(Login.this, "Your account is still pending approval. Please wait for admin confirmation.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Sign out pending seller
                            } else if ("rejected".equalsIgnoreCase(status)) {
                                Toast.makeText(Login.this, "Your account was rejected. Please contact support for more info.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Sign out rejected seller
                            } else {
                                Toast.makeText(Login.this, "Unknown account status. Please contact admin.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Sign out if status is unknown
                            }
                        } else {
                            // User not found in 'sellers', check 'buyers' collection
                            db.collection("buyers").document(uid).get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot doc = task1.getResult();
                                            if (doc != null && doc.exists()) {
                                                // Check if the buyer is approved
                                                String buyerStatus = doc.getString("status"); // Get status from buyer document

                                                if ("approved".equalsIgnoreCase(buyerStatus)) {
                                                    navigateToHomePage("buyer");
                                                } else if ("pending".equalsIgnoreCase(buyerStatus)) {
                                                    Toast.makeText(Login.this, "Your account is still pending approval. Please wait for admin confirmation.", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut(); // Sign out pending buyer
                                                } else if ("rejected".equalsIgnoreCase(buyerStatus)) {
                                                    Toast.makeText(Login.this, "Your account was rejected. Please contact support for more info.", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut(); // Sign out rejected buyer
                                                } else {
                                                    Toast.makeText(Login.this, "Unknown account status. Please contact admin.", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut(); // Sign out if status is unknown
                                                }
                                            } else {
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
