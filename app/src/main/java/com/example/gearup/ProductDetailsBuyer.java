package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailsBuyer extends AppCompatActivity {
    private TextView productName, productPrice, productDescription, availableQuantityText, sellerName;
    private Button addToCartButton, checkoutButton;
    private EditText productQuantity;
    private ViewPager2 viewPager;
    private ImageView sellerProfileImage;
    private int maxQuantity;

    // Stripe variables
    private String PublishableKey = "pk_test_51PF3ByC6MmcIFikTjKhzCftwVaWmffD2iAqfquBroHxyujRLOG6QJ07t0tljO8FzDYbsNZld6sSjbTSTFUfT8J1c00D2b0tfvg";
    private String SecretKey = "sk_test_51PF3ByC6MmcIFikTxmE9dhgo5ZLxCWlNgqBaBMwZUKCCeRd0pkgKBQZOBO9UymYma2sNPpNIKlU2befDh0JeISU700OoXXptWX";
    private String CustomerId;
    private String EphericalKey;
    private String ClientSecret;
    private PaymentSheet paymentSheet;

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details_buyer);

        productName = findViewById(R.id.tv_product_name);
        productPrice = findViewById(R.id.tv_product_price);
        productDescription = findViewById(R.id.tv_product_description);
        availableQuantityText = findViewById(R.id.tv_available_quantity);
        sellerName = findViewById(R.id.tv_seller_name);
        sellerProfileImage = findViewById(R.id.iv_seller_profile);
        addToCartButton = findViewById(R.id.btn_add_to_cart);
        checkoutButton = findViewById(R.id.btn_checkout);
        productQuantity = findViewById(R.id.et_product_quantity);
        viewPager = findViewById(R.id.viewPager);

        db = FirebaseFirestore.getInstance();

        // Initialize Stripe
        PaymentConfiguration.init(this, PublishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Get product from intent
        Product product = getIntent().getParcelableExtra("PRODUCT");

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.format("₱%.2f", product.getPrice()));
            productDescription.setText(product.getDescription());
            maxQuantity = product.getQuantity();

            // Display available quantity
            availableQuantityText.setText("Available Quantity: " + maxQuantity);

            // Load images into ViewPager2
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(product.getImageUrls());
            viewPager.setAdapter(imageSliderAdapter);

            // Retrieve seller info from Firestore
            getSellerInfo(product.getSellerId());
        }

        // Set up button click listeners
        addToCartButton.setOnClickListener(v -> addToCart(product));
        checkoutButton.setOnClickListener(v -> paymentFlow());
    }

    private void getSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String sellerNameStr = documentSnapshot.getString("shopName");
                            String sellerProfileImageUrl = documentSnapshot.getString("profile_image_url");

                            sellerName.setText(sellerNameStr);
                            Glide.with(ProductDetailsBuyer.this)
                                    .load(sellerProfileImageUrl)
                                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                                    .into(sellerProfileImage);
                        } else {
                            Toast.makeText(ProductDetailsBuyer.this, "Seller not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductDetailsBuyer.this, "Error getting seller info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToCart(Product product) {
        if (product != null) {
            String quantityText = productQuantity.getText().toString();
            int quantity = quantityText.isEmpty() ? 1 : Integer.parseInt(quantityText);

            // Validate quantity
            if (quantity < 1 || quantity > maxQuantity) {
                Toast.makeText(this, "Please enter a quantity between 1 and " + maxQuantity, Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the product is already in the cart
            boolean alreadyInCart = false;
            for (CartItem item : Cart.getInstance().getItems()) {
                if (item.getProduct().getId().equals(product.getId())) {
                    item.setQuantity(item.getQuantity() + quantity);
                    alreadyInCart = true;
                    break;
                }
            }

            if (!alreadyInCart) {
                Cart.getInstance().addToCart(product, quantity);
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();

            // Navigate to CartActivity
            Intent intent = new Intent(ProductDetailsBuyer.this, CartActivity.class);
            startActivity(intent);
        }
    }

    private void paymentFlow() {
        // Create customer and start payment flow
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            CustomerId = object.getString("id");
                            getEmphericalKey();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailsBuyer.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEmphericalKey() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            EphericalKey = object.getString("id");
                            getClientSecret(CustomerId, EphericalKey);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailsBuyer.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                header.put("Stripe-Version", "2024-09-30.acacia");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getClientSecret(String customerId, String ephericalKey) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");
                            paymentFlowPresent();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProductDetailsBuyer.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SecretKey);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", CustomerId);
                params.put("amount", "10000"); // Example amount
                params.put("currency", "PHP");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void paymentFlowPresent() {
        paymentSheet.presentWithPaymentIntent(ClientSecret, new PaymentSheet.Configuration("Learn with Arvind", new PaymentSheet.CustomerConfiguration(CustomerId, EphericalKey)));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
        }
    }
}
