package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragmentBuyer extends Fragment implements ProductAdapterBuyer.OnProductClickListener {
    private FirebaseFirestore db;
    private ViewPager2 viewPagerCentralComponents, viewPagerBody, viewPagerConnectors, viewPagerPeripherals, viewPagerRecommended;
    private ProductAdapterBuyer adapterCentralComponents, adapterBody, adapterConnectors, adapterPeripherals;
    private List<Product> centralComponentsList = new ArrayList<>();
    private List<Product> bodyList = new ArrayList<>();
    private List<Product> connectorsList = new ArrayList<>();
    private List<Product> peripheralsList = new ArrayList<>();
    private List<Product> recommendedProductsList = new ArrayList<>();

    private RecommendationAdapter recommendationAdapter;

    private EditText searchBar;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_buyer, container, false);

        viewPagerCentralComponents = view.findViewById(R.id.viewPager_central_components);
        viewPagerBody = view.findViewById(R.id.viewPager_body);
        viewPagerConnectors = view.findViewById(R.id.viewPager_connectors);
        viewPagerPeripherals = view.findViewById(R.id.viewPager_peripherals);
        searchBar = view.findViewById(R.id.search_bar);
        viewPagerRecommended = view.findViewById(R.id.viewPager_recommended_products);
        recommendationAdapter = new RecommendationAdapter(this::onRecommendedProductClick);




        viewPagerRecommended.setAdapter(recommendationAdapter);
        viewPagerRecommended.setVisibility(View.GONE);
        loadRecommendations();



        ImageView iconCart = view.findViewById(R.id.icon_cart);
        iconCart.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CartActivity.class);
            startActivity(intent);
        });

        ImageView iconMessage = view.findViewById(R.id.icon_message);
        iconMessage.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                String currentUserId = currentUser.getUid();

                Intent intent = new Intent(getContext(), ConversationListActivity.class);
                intent.putExtra("CURRENT_USER_ID", currentUserId);
                startActivity(intent);
            } else {
                // Handle case where user is not authenticated
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        });



        // Click listeners for See All buttons
        TextView textSeeAllCentral = view.findViewById(R.id.text_see_all_central);
        TextView textSeeAllBody = view.findViewById(R.id.text_see_all_body);
        TextView textSeeAllConnectors = view.findViewById(R.id.text_see_all_connectors);
        TextView textSeeAllPeripherals = view.findViewById(R.id.text_see_all_peripherals);
        TextView textShops = view.findViewById(R.id.text_shops);  // Assuming you have a TextView for Shops

        // Set click listener for Shops button
        textShops.setOnClickListener(v -> {
            // Navigate to the ShopsFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ShopsFragment());  // Assuming R.id.fragment_container is your FrameLayout container
            transaction.addToBackStack(null); // Optional, allows user to go back to the HomeFragment
            transaction.commit();
        });
        
        // Click listeners for See All buttons
        textSeeAllConnectors.setOnClickListener(v -> {
            // Pass the connectors list to ConnectorsActivity
            Intent intent = new Intent(getContext(), ConnectorsActivity.class);
            intent.putParcelableArrayListExtra("PRODUCT_LIST", new ArrayList<>(connectorsList));  // Pass the connectors list
            startActivity(intent);
        });

        textSeeAllCentral.setOnClickListener(v -> {
            // Pass the central components list to CentralComponentsActivity
            Intent intent = new Intent(getContext(), CentralComponentsActivity.class);
            intent.putParcelableArrayListExtra("PRODUCT_LIST", new ArrayList<>(centralComponentsList));  // Pass the central components list
            startActivity(intent);
        });

        textSeeAllBody.setOnClickListener(v -> {
            // Pass the body list to BodyActivity
            Intent intent = new Intent(getContext(), BodyActivity.class);
            intent.putParcelableArrayListExtra("PRODUCT_LIST", new ArrayList<>(bodyList));  // Pass the body list
            startActivity(intent);
        });

        textSeeAllPeripherals.setOnClickListener(v -> {
            // Pass the peripherals list to PeripheralsActivity
            Intent intent = new Intent(getContext(), PeripheralsActivity.class);
            intent.putParcelableArrayListExtra("PRODUCT_LIST", new ArrayList<>(peripheralsList));  // Pass the peripherals list
            startActivity(intent);
        });


        db = FirebaseFirestore.getInstance();
        loadProducts();

        // Handle Search Bar Click
        searchBar.setOnClickListener(v -> {
            // Disable editing and prevent keyboard from popping up
            searchBar.clearFocus(); // Removes focus from the EditText
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && requireActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0); // Hide the keyboard
            }

            // Log the action and navigate to SearchActivity
            Log.d("SearchBar", "Clicked, navigating to SearchActivity");
            try {
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                startActivity(intent); // Go directly to the SearchActivity without any typing enabled
            } catch (Exception e) {
                Log.e("SearchBar", "Error opening SearchActivity", e);
                Toast.makeText(requireContext(), "Failed to open search", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }


    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        centralComponentsList.clear();
                        bodyList.clear();
                        connectorsList.clear();
                        peripheralsList.clear();

                        // Load products into their respective categories
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId()); // Set the Firestore document ID
                                categorizeProduct(product); // Categorize the product
                                loadSellerProfile(product); // Load the seller profile image
                                Log.d("HomeFragmentBuyer", "Loaded product: " + product.getName() + " with ID: " + product.getId());
                            }
                        }

                        // Shuffle the product lists
                        shuffleProductLists();

                        // Set adapters with shuffled lists
                        setAdapters();
                    } else {
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void shuffleProductLists() {
        // Shuffle the lists using Collections.shuffle()
        Collections.shuffle(centralComponentsList);
        Collections.shuffle(bodyList);
        Collections.shuffle(connectorsList);
        Collections.shuffle(peripheralsList);
    }



    private void categorizeProduct(Product product) {
        String category = product.getCategory();
        if (category == null) {
            Log.e("HomeFragmentBuyer", "Product category is null for product: " + product.getName());
            return;
        }

        switch (category) {
            case "Central Components":
                centralComponentsList.add(product);
                break;
            case "Body":
                bodyList.add(product);
                break;
            case "Connectors":
                connectorsList.add(product);
                break;
            case "Peripherals":
                peripheralsList.add(product);
                break;
            default:
                Log.e("HomeFragmentBuyer", "Unknown category: " + category + " for product: " + product.getName());
                break;
        }
    }

    private void setAdapters() {
        adapterCentralComponents = new ProductAdapterBuyer(new ArrayList<>(centralComponentsList), "Central Components", this);
        viewPagerCentralComponents.setAdapter(adapterCentralComponents);
        viewPagerCentralComponents.setOffscreenPageLimit(1);

        adapterBody = new ProductAdapterBuyer(new ArrayList<>(bodyList), "Body", this);
        viewPagerBody.setAdapter(adapterBody);
        viewPagerBody.setOffscreenPageLimit(1);

        adapterConnectors = new ProductAdapterBuyer(new ArrayList<>(connectorsList), "Connectors", this);
        viewPagerConnectors.setAdapter(adapterConnectors);
        viewPagerConnectors.setOffscreenPageLimit(1);

        adapterPeripherals = new ProductAdapterBuyer(new ArrayList<>(peripheralsList), "Peripherals", this);
        viewPagerPeripherals.setAdapter(adapterPeripherals);
        viewPagerPeripherals.setOffscreenPageLimit(1);

        Log.d("HomeFragmentBuyer", "Total products loaded: " + (centralComponentsList.size() + bodyList.size() + connectorsList.size() + peripheralsList.size()));
    }

    private void filterProducts(String query) {
        List<Product> filteredCentralComponents = new ArrayList<>();
        List<Product> filteredBody = new ArrayList<>();
        List<Product> filteredConnectors = new ArrayList<>();
        List<Product> filteredPeripherals = new ArrayList<>();

        for (Product product : centralComponentsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredCentralComponents.add(product);
            }
        }

        for (Product product : bodyList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredBody.add(product);
            }
        }

        for (Product product : connectorsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredConnectors.add(product);
            }
        }

        for (Product product : peripheralsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredPeripherals.add(product);
            }
        }

        adapterCentralComponents.updateProductList(filteredCentralComponents);
        adapterBody.updateProductList(filteredBody);
        adapterConnectors.updateProductList(filteredConnectors);
        adapterPeripherals.updateProductList(filteredPeripherals);
    }

    private void loadSellerProfile(Product product) {
        String sellerId = product.getSellerId();
        if (sellerId != null) {
            db.collection("sellers").document(sellerId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String profileImageUrl = document.getString("profileImageUrl");
                        product.setSellerProfileImageUrl(profileImageUrl);
                        notifyAdapters();
                    }
                }
            });
        }
    }

    private void notifyAdapters() {
        adapterCentralComponents.notifyDataSetChanged();
        adapterBody.notifyDataSetChanged();
        adapterConnectors.notifyDataSetChanged();
        adapterPeripherals.notifyDataSetChanged();
    }

    @Override
    public void onProductClick(int position, String category) {
        Product clickedProduct;

        if (category.equals("Central Components")) {
            clickedProduct = centralComponentsList.get(position);
        } else if (category.equals("Body")) {
            clickedProduct = bodyList.get(position);
        } else if (category.equals("Connectors")) {
            clickedProduct = connectorsList.get(position);
        } else { // Peripherals
            clickedProduct = peripheralsList.get(position);
        }

        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            UserInteractionLogger.logProductClick(
                    userId,
                    clickedProduct.getId(),
                    clickedProduct.getName(),
                    clickedProduct.getCategory()
            );
        } else {
            Log.e("FirebaseDebug", "❌ User not authenticated. Cannot log interaction.");
        }

        // Open product details
        Intent intent = new Intent(getContext(), ProductDetailsBuyerActivity.class);
        intent.putExtra("PRODUCT", clickedProduct);
        startActivity(intent);
    }
    private void loadRecommendations() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated. Cannot load recommendations.");
            return;
        }

        String currentUserId = currentUser.getUid();
        RecommendationManager recommendationManager = new RecommendationManager();

        recommendationManager.loadRecommendations(currentUserId, recommendedProducts -> {
            getActivity().runOnUiThread(() -> {
                if (!recommendedProducts.isEmpty()) {
                    recommendedProductsList.clear();
                    recommendedProductsList.addAll(recommendedProducts);
                    recommendationAdapter.setProductList(recommendedProductsList);
                    viewPagerRecommended.setVisibility(View.VISIBLE);
                } else {
                    viewPagerRecommended.setVisibility(View.GONE);
                }
            });
        });
    }
    public void onRecommendedProductClick(Product clickedProduct) {
        if (clickedProduct == null) {
            Log.e("RecommendationClick", "❌ Clicked product is null");
            return;
        }

        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            UserInteractionLogger.logProductClick(
                    userId,
                    clickedProduct.getId(),
                    clickedProduct.getName(),
                    "Recommended" // Logging as a recommended product
            );
        } else {
            Log.e("FirebaseDebug", "❌ User not authenticated. Cannot log interaction.");
        }

        // Open product details
        Intent intent = new Intent(getContext(), ProductDetailsBuyerActivity.class);
        intent.putExtra("PRODUCT", clickedProduct);
        startActivity(intent);
    }



}
