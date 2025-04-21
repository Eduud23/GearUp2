package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class BuyerRegister extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private EditText firstNameEditText, lastNameEditText, mobileNumberEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private Button selectImageButton;

    private ImageView imgProfilePreview;

    private Uri selectedImageUri = null;
    private static final int IMAGE_PICK_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        firstNameEditText = findViewById(R.id.etfn);
        lastNameEditText = findViewById(R.id.etln);
        mobileNumberEditText = findViewById(R.id.etemobileno);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.cpassword);
        signupButton = findViewById(R.id.btn_signup);
        selectImageButton = findViewById(R.id.btnSelectImage);  // Add this in XML
        imgProfilePreview = findViewById(R.id.imgProfile);       // Add this in XML

        selectImageButton.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT);
            pickImage.setType("image/*");
            startActivityForResult(pickImage, IMAGE_PICK_CODE);
        });

        signupButton.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgProfilePreview.setImageURI(selectedImageUri);
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String mobileNumber = mobileNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(mobileNumber) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(BuyerRegister.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(BuyerRegister.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(BuyerRegister.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.equals(confirmPassword)) {
            signupButton.setEnabled(false);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        signupButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            if (selectedImageUri != null) {
                                uploadProfileImage(userId, selectedImageUri, imageUrl -> {
                                    saveUserInformation(firstName, lastName, mobileNumber, email, imageUrl);
                                });
                            } else {
                                saveUserInformation(firstName, lastName, mobileNumber, email, null);
                            }
                        } else {
                            Toast.makeText(BuyerRegister.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(BuyerRegister.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage(String userId, Uri imageUri, OnImageUploadCallback callback) {
        String fileName = "buyers/" + userId + "/" + UUID.randomUUID();
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String imageUrl = task.getResult().toString();
                        callback.onUploadSuccess(imageUrl);
                    } else {
                        Toast.makeText(this, "Image upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserInformation(String firstName, String lastName, String mobileNumber, String email, @Nullable String imageUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("mobileNumber", mobileNumber);
        user.put("email", email);
        user.put("role", "buyer");
        user.put("status", "pending");
        if (imageUrl != null) {
            user.put("profileImageUrl", imageUrl);
        }

        db.collection("buyers").document(mAuth.getCurrentUser().getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BuyerRegister.this, "Wait for Confirmation", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(BuyerRegister.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(BuyerRegister.this, Login.class);
        startActivity(intent);
        finish();
    }

    interface OnImageUploadCallback {
        void onUploadSuccess(String imageUrl);
    }
}
