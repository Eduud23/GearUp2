package com.example.gearup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CODE_PRODUCT_DETAILS = 2;

    private Map<String, List<Product>> categorizedProducts = new HashMap<>();
    private Map<String, String> categoryImages = new HashMap<>();
    private Map<String, Integer> categoryCounts = new HashMap<>();
    private List<Uri> selectedImageUris = new ArrayList<>();
    private AlertDialog alertDialog;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        recyclerViewCategories = view.findViewById(R.id.recyclerView_categories);
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeCategories();

        swipeRefreshLayout.setOnRefreshListener(this::refreshProductList);

        Button addProductButton = view.findViewById(R.id.btn_add_product);
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        return view;
    }

    private void initializeCategories() {
        categorizedProducts.clear();
        categoryImages.clear();
        categoryCounts.clear();

        db.collection("category")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("name");
                            String logoUrl = document.getString("logo");

                            if (categoryName != null && logoUrl != null) {
                                categorizedProducts.put(categoryName, new ArrayList<>());
                                categoryCounts.put(categoryName, 0);
                                categoryImages.put(categoryName, logoUrl);
                            }
                        }
                        Log.d("InventoryFragment", "Categories: " + categorizedProducts.keySet());

                        categoryAdapter = new CategoryAdapter(getContext(),
                                new ArrayList<>(categorizedProducts.keySet()),
                                categorizedProducts,
                                categoryImages,
                                categoryCounts,
                                this::showProductsForCategory);
                        recyclerViewCategories.setAdapter(categoryAdapter);

                        updateProductCounts();
                    } else {
                        Log.e("InventoryFragment", "Error fetching categories: ", task.getException());
                        Toast.makeText(getContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshProductList() {
        updateProductCounts();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateProductCounts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (String category : categoryCounts.keySet()) {
                            categoryCounts.put(category, 0);
                        }

                        for (List<Product> products : categorizedProducts.values()) {
                            products.clear();
                        }

                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                String category = product.getCategory();
                                if (categorizedProducts.containsKey(category)) {
                                    categorizedProducts.get(category).add(product);
                                    categoryCounts.put(category, categoryCounts.get(category) + 1);
                                }
                            }
                        }
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch product counts", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void showProductsForCategory(String category) {
        List<Product> products = categorizedProducts.get(category);
        Intent intent = new Intent(getContext(), ProductList.class);
        intent.putExtra("CATEGORY", category);
        intent.putParcelableArrayListExtra("PRODUCTS", new ArrayList<>(products));
        startActivityForResult(intent, REQUEST_CODE_PRODUCT_DETAILS);
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        // Initialize dialog views
        EditText productName = dialogView.findViewById(R.id.et_product_name);
        EditText productPrice = dialogView.findViewById(R.id.et_product_price);
        EditText productDescription = dialogView.findViewById(R.id.et_product_description);
        EditText productQuantity = dialogView.findViewById(R.id.et_product_quantity);
        EditText productBrand = dialogView.findViewById(R.id.et_product_brand);
        EditText productYearModel = dialogView.findViewById(R.id.et_product_year_model);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);

        // Populate the spinner with categories
        List<String> categoryList = new ArrayList<>(categorizedProducts.keySet());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        ImageView productImage1 = dialogView.findViewById(R.id.iv_product_image1);
        ImageView productImage2 = dialogView.findViewById(R.id.iv_product_image2);
        ImageView productImage3 = dialogView.findViewById(R.id.iv_product_image3);
        Button chooseImageButton = dialogView.findViewById(R.id.btn_choose_image);

        // Reset selected image URIs for new product
        selectedImageUris.clear();
        for (int i = 0; i < 3; i++) {
            selectedImageUris.add(null);
        }

        // Set up single image selection button
        chooseImageButton.setOnClickListener(v -> openFileChooser());

        // Add product button listener
        Button addProductButton = dialogView.findViewById(R.id.btn_add_product);
        addProductButton.setOnClickListener(v -> {
            // Get input values
            String name = productName.getText().toString().trim();
            String priceString = productPrice.getText().toString().trim();
            String description = productDescription.getText().toString().trim();
            String quantityString = productQuantity.getText().toString().trim();
            String brand = productBrand.getText().toString().trim();
            String yearModel = productYearModel.getText().toString().trim();
            String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "";

            // Validate input fields
            if (name.isEmpty() || priceString.isEmpty() || quantityString.isEmpty() || selectedImageUris.size() < 3 || brand.isEmpty() || yearModel.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all fields and choose 3 images", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceString);
                int quantity = Integer.parseInt(quantityString);

                // Get current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    uploadProductImages(userId, name, price, description, quantity, category, brand, yearModel, selectedImageUris);
                } else {
                    Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid price or quantity", Toast.LENGTH_SHORT).show();
            }
        });

        // Create and show the dialog
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), 3); // Limit to 3 images
                for (int i = 0; i < count; i++) {
                    Uri selectedImageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.set(i, selectedImageUri);
                    ImageView productImage = alertDialog.findViewById(getImageViewId(i));
                    if (productImage != null) {
                        productImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedImageUri).into(productImage);
                    }
                }
            } else if (data.getData() != null) {
                // Handle single image selection if multiple not allowed
                Uri selectedImageUri = data.getData();
                selectedImageUris.set(0, selectedImageUri);
                ImageView productImage = alertDialog.findViewById(R.id.iv_product_image1);
                if (productImage != null) {
                    productImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(productImage);
                }
            }
        }
    }

    private int getImageViewId(int index) {
        switch (index) {
            case 0: return R.id.iv_product_image1;
            case 1: return R.id.iv_product_image2;
            case 2: return R.id.iv_product_image3;
            default: return -1;
        }
    }

    private void uploadProductImages(String userId, String name, double price, String description, int quantity, String category, String brand, String yearModel, List<Uri> selectedImageUris) {
        List<String> imageUrls = new ArrayList<>();
        StorageReference storageRef = storage.getReference().child("products/" + userId);

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            if (imageUri != null) {
                StorageReference imageRef = storageRef.child(System.currentTimeMillis() + "_" + i + ".jpg");

                imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrls.add(uri.toString());
                        if (imageUrls.size() == selectedImageUris.size()) {
                            saveProductToFirestore(userId, name, price, description, quantity, category, brand, yearModel, imageUrls);
                        }
                    });
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void saveProductToFirestore(String userId, String name, double price, String description, int quantity, String category, String brand, String yearModel, List<String> imageUrls) {
        Product product = new Product("", name, price, description, imageUrls, category, userId, quantity, brand, yearModel);

        db.collection("users").document(userId)
                .collection("products").add(product)
                .addOnSuccessListener(documentReference -> {
                    if (categorizedProducts.containsKey(category)) {
                        categorizedProducts.get(category).add(product);

                        // Safely update category count
                        Integer currentCount = categoryCounts.get(category);
                        if (currentCount == null) {
                            currentCount = 0; // Initialize if null
                        }
                        categoryCounts.put(category, currentCount + 1);

                        Toast.makeText(requireContext(), "Product added", Toast.LENGTH_SHORT).show();
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("InventoryFragment", "Category not found: " + category);
                    }
                    alertDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add product", Toast.LENGTH_SHORT).show());
    }
}
