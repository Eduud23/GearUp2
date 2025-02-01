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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.RemoveItemListener {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private FirebaseFirestore db;
    private List<CartItem> cartItems;
    private ListenerRegistration cartListenerRegistration;
    private String currentUserId;
    private TextView totalTextView;
    private ImageView deleteImageView; // Button to delete selected items

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerViewCart = view.findViewById(R.id.recyclerView_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));

        totalTextView = view.findViewById(R.id.textView_total);
        deleteImageView = view.findViewById(R.id.cart_delete_icon); // The button to delete selected items

        db = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();

        cartAdapter = new CartAdapter(cartItems, cartItem -> updateTotal(), this);
        recyclerViewCart.setAdapter(cartAdapter);

        // Show delete confirmation dialog when delete button is clicked
        deleteImageView.setOnClickListener(v -> showDeleteConfirmationDialog());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchCartItems();
        }

        return view;
    }

    private void fetchCartItems() {
        cartListenerRegistration = db.collection("buyers")
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
                            CartItem cartItem = doc.toObject(CartItem.class);
                            cartItem.setDocumentId(doc.getId());
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();

                        // Update the total after fetching cart items
                        updateTotal();
                    }
                });
    }

    private void updateTotal() {
        double total = 0;
        // Calculate total for all items in the cart, not just selected ones
        for (CartItem cartItem : cartItems) {
            total += cartItem.getTotalPrice();
        }

        String formattedTotal = formatPrice(total);
        totalTextView.setText("Total: ₱" + formattedTotal);
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    private void showDeleteConfirmationDialog() {
        List<CartItem> selectedItems = cartAdapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "No items selected for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Selected Items")
                .setMessage("Are you sure to delete these products?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete selected items from Firestore
                    for (CartItem item : selectedItems) {
                        db.collection("buyers")
                                .document(currentUserId)
                                .collection("cartItems")
                                .document(item.getDocumentId())
                                .delete();
                    }
                    Toast.makeText(getContext(), "Items removed from cart", Toast.LENGTH_SHORT).show();
                    updateTotal();  // Update total after deletion
                    cartAdapter.setDeleteMode(false);  // Exit delete mode
                    deleteImageView.setVisibility(View.GONE);  // Hide delete button
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onItemLongPress(CartItem cartItem) {
        // Enable delete mode when an item is long-pressed
        cartAdapter.setDeleteMode(true);
        deleteImageView.setVisibility(View.VISIBLE); // Show delete button when in delete mode
    }
}
