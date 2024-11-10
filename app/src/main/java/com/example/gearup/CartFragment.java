package com.example.gearup;

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

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private FirebaseFirestore db;
    private List<CartItem> cartItems;
    private ListenerRegistration cartListenerRegistration;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerViewCart = view.findViewById(R.id.recyclerView_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems, cartItem -> showItemDetailsDialog(cartItem));
        recyclerViewCart.setAdapter(cartAdapter);

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
                            return; // Handle the error
                        }

                        cartItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem cartItem = doc.toObject(CartItem.class);
                            // Set the documentId (Firestore document ID)
                            cartItem.setDocumentId(doc.getId());
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showItemDetailsDialog(final CartItem cartItem) {
        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.cart_dialog_item, null);
        builder.setView(dialogView);

        // Bind the dialog views
        ImageView itemImageView = dialogView.findViewById(R.id.imageView_item);
        TextView itemNameTextView = dialogView.findViewById(R.id.textView_item_name);
        TextView itemPriceTextView = dialogView.findViewById(R.id.textView_item_price);
        TextView itemDescriptionTextView = dialogView.findViewById(R.id.textView_item_description);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);
        Button btnBuyNow = dialogView.findViewById(R.id.btn_buy_now);
        ImageView btnExit = dialogView.findViewById(R.id.icon_exit);

        // Set the item details
        Product product = cartItem.getProduct();
        itemNameTextView.setText(product.getName());
        itemPriceTextView.setText("₱" + product.getPrice());
        itemDescriptionTextView.setText(product.getDescription());

        // Load the product image using Glide
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(getContext()).load(imageUrls.get(0)).into(itemImageView);
        }

        // Create the dialog (must be done before using the dialog object)
        final AlertDialog dialog = builder.create();

        // Set up the Exit button (dismiss the dialog)
        btnExit.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Delete the item from Firestore using the document ID
            db.collection("buyers")
                    .document(currentUserId)
                    .collection("cartItems")
                    .document(cartItem.getDocumentId()) // Use getDocumentId() here
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // On success, dismiss the dialog and notify the user
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Item deleted from cart", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure (show an error message)
                        Toast.makeText(getContext(), "Failed to delete item. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        });


        btnBuyNow.setOnClickListener(v -> {
            dialog.dismiss();
            // Handle "Buy Now" action (e.g., navigate to the checkout screen)
        });

        // Show the dialog
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cartListenerRegistration != null) {
            cartListenerRegistration.remove();
        }
    }
}
