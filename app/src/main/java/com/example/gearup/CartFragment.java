package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.RemoveItemListener {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private FirebaseFirestore db;
    private List<CartItem> cartItems;
    private String currentUserId;
    private TextView totalTextView;
    private ImageView deleteImageView;
    private Button checkoutButton;

    private boolean isDeleteModeActive = false; // Track the delete mode state

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerViewCart = view.findViewById(R.id.recyclerView_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));

        totalTextView = view.findViewById(R.id.textView_total);
        deleteImageView = view.findViewById(R.id.cart_delete_icon);
        checkoutButton = view.findViewById(R.id.button_checkout);

        db = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();

        cartAdapter = new CartAdapter(getContext(), cartItems, cartItem -> updateTotal(), this);

        recyclerViewCart.setAdapter(cartAdapter);

        deleteImageView.setOnClickListener(v -> showDeleteConfirmationDialog());
        checkoutButton.setOnClickListener(v -> proceedToCheckout());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchCartItems();
        }

        // Add back press callback to handle delete mode
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isDeleteModeActive) {
                    // Disable delete mode when back is pressed
                    isDeleteModeActive = false;
                    cartAdapter.setDeleteMode(false);
                    deleteImageView.setVisibility(View.GONE);
                } else {
                    // Disable the callback to prevent infinite loop and handle normal back press
                    remove();
                    requireActivity().onBackPressed(); // Perform normal back press
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return view;
    }

    private void fetchCartItems() {
        db.collection("buyers")
                .document(currentUserId)
                .collection("cartItems")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        cartItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String productName = doc.getString("productName");
                            int quantity = doc.getLong("quantity").intValue();
                            String sellerId = doc.getString("sellerId");

                            Double totalPriceObj = doc.getDouble("totalPrice");
                            double totalPrice = (totalPriceObj != null) ? totalPriceObj : 0.0;

                            totalPrice *= quantity;

                            String imageUrl = doc.getString("imageUrl");
                            String brand = doc.getString("brand");
                            String yearModel = doc.getString("yearModel");
                            String productId = doc.getString("productId");
                            String documentId = doc.getId();

                            CartItem cartItem = new CartItem(productName, quantity, sellerId, totalPrice, currentUserId, imageUrl, brand, yearModel, productId,documentId);
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                        updateTotal();
                    }
                });
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = 0;
        for (CartItem cartItem : cartItems) {
            totalAmount += cartItem.getTotalPrice();
        }

        String formattedTotalAmount = formatPrice(totalAmount);

        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartItems));
        intent.putExtra("totalAmount", formattedTotalAmount);

        startActivity(intent);
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem cartItem : cartItems) {
            total += cartItem.getTotalPrice();
        }
        totalTextView.setText("Total: ₱" + formatPrice(total));
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    // This is where the "Are you sure you want to delete these products?" dialog is displayed
    private void showDeleteConfirmationDialog() {
        List<CartItem> selectedItems = cartAdapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "No items selected for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Selected Items")
                .setMessage("Are you sure you want to delete these products?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the selected items from Firestore
                    for (CartItem item : selectedItems) {
                        db.collection("buyers")
                                .document(currentUserId)
                                .collection("cartItems")
                                .document(item.getDocumentId())
                                .delete();
                    }
                    Toast.makeText(getContext(), "Items removed from cart", Toast.LENGTH_SHORT).show();
                    updateTotal();
                    isDeleteModeActive = false;
                    cartAdapter.setDeleteMode(false);
                    deleteImageView.setVisibility(View.GONE);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onItemLongPress(CartItem cartItem) {
        isDeleteModeActive = true;
        cartAdapter.setDeleteMode(true);
        deleteImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelectionChanged(List<CartItem> selectedItems) {
        if (selectedItems.isEmpty()) {
            deleteImageView.setVisibility(View.GONE);
        } else {
            deleteImageView.setVisibility(View.VISIBLE);
        }
    }
}
