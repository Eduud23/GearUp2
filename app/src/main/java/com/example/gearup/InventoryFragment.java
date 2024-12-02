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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class InventoryFragment extends Fragment {
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CODE_PRODUCT_DETAILS = 2;

    private Map<String, List<Product>> categorizedProducts = new HashMap<>();
    private Map<String, String> categoryImages = new HashMap<>();
    private Map<String, Integer> categoryCounts = new HashMap<>();
    private List<Uri> selectedImageUris = new ArrayList<>(3); // Initialize for up to 3 images
    private AlertDialog alertDialog;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Retrofit for price prediction and product addition
    private PriceApi priceApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        recyclerViewCategories = view.findViewById(R.id.recyclerView_categories);
        recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 2));



        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        String baseUrl;

        if (DeviceUtils.isEmulator()) {
            baseUrl = "http://10.0.2.2:5001/";
        } else {
            baseUrl = "http://192.168.254.192:5001/";
        }

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson)) // Use the customized Gson instance
                .build();

        priceApi = retrofit.create(PriceApi.class);

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
        EditText productNameInput = dialogView.findViewById(R.id.et_product_name);
        EditText brandInput = dialogView.findViewById(R.id.et_product_brand);
        EditText yearModelInput = dialogView.findViewById(R.id.et_product_year_model);
        EditText descriptionInput = dialogView.findViewById(R.id.et_product_description);
        EditText predictedPriceText = dialogView.findViewById(R.id.et_product_price);
        predictedPriceText.setEnabled(true); // Make it non-editable

        Button addProductButton = dialogView.findViewById(R.id.btn_add_product);
        Button selectImageButton = dialogView.findViewById(R.id.btn_choose_image);
        Button predictPriceButton = dialogView.findViewById(R.id.btn_predict_price);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinner_category);

        // Populate the spinner with categories
        List<String> categoryList = new ArrayList<>(categorizedProducts.keySet());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Image selection button listener
        selectImageButton.setOnClickListener(v -> openImageChooser());

        // Predict price button listener
        predictPriceButton.setOnClickListener(v -> {
            String productName = productNameInput.getText().toString();
            String brand = brandInput.getText().toString();
            String yearModelString = yearModelInput.getText().toString();

            // Validate year model input
            if (yearModelString.isEmpty()) {
                Toast.makeText(getContext(), "Please enter the year model.", Toast.LENGTH_SHORT).show();
                return;
            }

            int yearModel;
            try {
                yearModel = Integer.parseInt(yearModelString);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid year model. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create PriceRequest and call the API
            PriceRequest request = new PriceRequest(productName, brand, yearModel);
            priceApi.predictPrice(request).enqueue(new Callback<PriceResponse>() {
                @Override
                public void onResponse(Call<PriceResponse> call, Response<PriceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        double predictedPrice = response.body().getPredictedPrice();
                        predictedPriceText.setText("₱" + String.format("%.2f", predictedPrice));
                    } else {
                        Toast.makeText(getContext(), "Prediction failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PriceResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });

        // Add product button listener
        addProductButton.setOnClickListener(v -> {
            String name = productNameInput.getText().toString().trim();
            String priceString = predictedPriceText.getText().toString().replace("₱", "");
            double price = priceString.isEmpty() ? 0 : Double.parseDouble(priceString);
            String brand = brandInput.getText().toString().trim();
            String yearModelString = yearModelInput.getText().toString().trim(); // Keep this as a String
            String category = categorySpinner.getSelectedItem().toString();
            String description = descriptionInput.getText().toString().trim();

            if (name.isEmpty() || brand.isEmpty() || yearModelString.isEmpty() || description.isEmpty()) {
                Toast.makeText(getContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate year model again
            int yearModel;
            try {
                yearModel = Integer.parseInt(yearModelString);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid year model. Please enter a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload images and save the product
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            uploadProductImages(userId, name, price, description, 1, category, brand, String.valueOf(yearModel), selectedImageUris);
        });

        // Create and show the dialog
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUris.clear(); // Clear previous selections
            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), 3); // Limit to 3 images
                for (int i = 0; i < count; i++) {
                    Uri selectedImageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImageUris.add(selectedImageUri);
                    ImageView productImage = alertDialog.findViewById(getImageViewId(i));
                    if (productImage != null) {
                        productImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(selectedImageUri).into(productImage);
                    }
                }
            } else if (data.getData() != null) {
                // Handle single image selection
                Uri selectedImageUri = data.getData();
                selectedImageUris.add(selectedImageUri);
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
        Product newProduct = new Product(
                null, // id can be null initially
                name,
                price,
                description, // Store the description
                imageUrls,
                category,
                userId,
                quantity,
                brand,
                yearModel
        );

        db.collection("users").document(userId)
                .collection("products")
                .add(newProduct)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    refreshProductList(); // Refresh the product list

                    // Now save the product data to CSV
                    saveProductDataToCSV(name, brand, yearModel, price);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding product", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProductDataToCSV(String name, String brand, String yearModel, double price) {
        ConcreteProductData productData = new ConcreteProductData(name, brand, yearModel, price);

        priceApi.addProduct(productData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product data saved to CSV", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to save product data to CSV", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
