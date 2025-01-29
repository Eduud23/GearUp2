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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerViewCart = view.findViewById(R.id.recyclerView_cart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));

        totalTextView = view.findViewById(R.id.textView_total);

        db = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();

        // Pass both the OnItemClickListener and RemoveItemListener
        cartAdapter = new CartAdapter(cartItems, cartItem -> updateTotal(), this);
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
                            return;
                        }

                        cartItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem cartItem = doc.toObject(CartItem.class);
                            cartItem.setDocumentId(doc.getId());
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateTotal() {
        double total = 0;
        List<CartItem> selectedItems = cartAdapter.getSelectedItems();

        for (CartItem cartItem : selectedItems) {
            total += cartItem.getTotalPrice();
        }

        String formattedTotal = formatPrice(total);
        totalTextView.setText("Total: ₱" + formattedTotal);
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    // Show bottom dialog for long press on item
    private void showRemoveItemDialog(final CartItem cartItem) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_bottom_remove, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        Button btnRemove = dialogView.findViewById(R.id.btn_remove_from_cart);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        final AlertDialog dialog = builder.create();

        btnRemove.setOnClickListener(v -> {
            // Remove item from Firestore
            db.collection("buyers")
                    .document(currentUserId)
                    .collection("cartItems")
                    .document(cartItem.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
                        updateTotal(); // Update total after deletion
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to remove item. Try again.", Toast.LENGTH_SHORT).show();
                    });

            dialog.dismiss(); // Dismiss the dialog after removal
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cartListenerRegistration != null) {
            cartListenerRegistration.remove();
        }
    }

    // Implementing RemoveItemListener interface method
    @Override
    public void onItemLongPress(CartItem cartItem) {
        showRemoveItemDialog(cartItem); // Show the dialog on long press
    }
}
