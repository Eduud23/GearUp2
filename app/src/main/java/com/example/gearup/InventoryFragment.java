package com.example.gearup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.storage.UploadTask;

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
    private Map<String, Integer> categoryImages = new HashMap<>();
    private Map<String, Integer> categoryCounts = new HashMap<>();
    private Uri selectedImageUri;
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

        categoryAdapter = new CategoryAdapter(getContext(),
                new ArrayList<>(categorizedProducts.keySet()),
                categorizedProducts,
                categoryImages,
                categoryCounts,
                this::showProductsForCategory);
        recyclerViewCategories.setAdapter(categoryAdapter);

        Button addProductButton = view.findViewById(R.id.btn_add_product);
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        swipeRefreshLayout.setOnRefreshListener(this::refreshProductList);

        return view;
    }

    private void initializeCategories() {
        // Define static categories
        categorizedProducts.put("Central Components", new ArrayList<>());
        categorizedProducts.put("Peripherals", new ArrayList<>());
        categorizedProducts.put("Connectors", new ArrayList<>());
        categorizedProducts.put("Body", new ArrayList<>());

        categoryImages.put("Central Components", R.drawable.automotive);
        categoryImages.put("Peripherals", R.drawable.automotive);
        categoryImages.put("Connectors", R.drawable.automotive);
        categoryImages.put("Body", R.drawable.automotive);

        // Initialize category counts to zero
        for (String category : categorizedProducts.keySet()) {
            categoryCounts.put(category, 0);
        }

        // Fetch product counts for each category from Firestore
        updateProductCounts();
    }

    private void refreshProductList() {
        updateProductCounts();
        // After refreshing data, you should call:
        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
    }

    private void updateProductCounts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Reset category counts
                        for (String category : categoryCounts.keySet()) {
                            categoryCounts.put(category, 0);
                        }

                        // Clear current products and reload
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
                    swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
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

        EditText productName = dialogView.findViewById(R.id.et_product_name);
        EditText productPrice = dialogView.findViewById(R.id.et_product_price);
        EditText productDescription = dialogView.findViewById(R.id.et_product_description);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);
        Button chooseImageButton = dialogView.findViewById(R.id.btn_choose_image);
        ImageView productImage = dialogView.findViewById(R.id.iv_product_image);
        Button addProductButton = dialogView.findViewById(R.id.btn_add_product);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(categorizedProducts.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        chooseImageButton.setOnClickListener(v -> openFileChooser());

        addProductButton.setOnClickListener(v -> {
            String name = productName.getText().toString();
            String priceString = productPrice.getText().toString();
            String description = productDescription.getText().toString();
            String category = categorySpinner.getSelectedItem().toString();

            if (name.isEmpty() || priceString.isEmpty() || selectedImageUri == null) {
                Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceString);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                uploadProductImage(userId, name, price, description, category);
            } else {
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void uploadProductImage(String userId, String name, double price, String description, String category) {
        StorageReference storageRef = storage.getReference().child("products/" + userId + "/" + System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = storageRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            saveProductToFirestore(userId, name, price, description, category, imageUrl);
        })).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void saveProductToFirestore(String userId, String name, double price, String description, String category, String imageUrl) {
        Product product = new Product(name, price, description, imageUrl, category);

        db.collection("users").document(userId)
                .collection("products").add(product)
                .addOnSuccessListener(documentReference -> {
                    categorizedProducts.get(category).add(product);
                    categoryCounts.put(category, categoryCounts.get(category) + 1);
                    Toast.makeText(requireContext(), "Product added", Toast.LENGTH_SHORT).show();
                    categoryAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add product", Toast.LENGTH_SHORT).show());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ImageView productImage = alertDialog.findViewById(R.id.iv_product_image);
            if (productImage != null) {
                productImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(selectedImageUri).into(productImage);
            }
        }
    }
}
