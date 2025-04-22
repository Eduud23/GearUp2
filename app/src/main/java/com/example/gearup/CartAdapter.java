package com.example.gearup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnItemClickListener listener;
    private List<CartItem> selectedItems;
    private RemoveItemListener removeItemListener;
    private boolean showCheckboxes = false;
    private FirebaseFirestore db;
    private Context context;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(CartItem cartItem);
    }

    // Interface for removing item
    public interface RemoveItemListener {
        void onItemLongPress(CartItem cartItem); // Method that will be called for long press
        void onItemSelectionChanged(List<CartItem> selectedItems); // Method for selected items
    }

    // Constructor to initialize variables
    public CartAdapter(Context context, List<CartItem> cartItems, OnItemClickListener listener, RemoveItemListener removeItemListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
        this.selectedItems = new ArrayList<>();
        this.removeItemListener = removeItemListener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        String productName = cartItem.getProductName();
        double totalPrice = cartItem.getTotalPrice(); // Already calculated in CartItem
        String formattedPrice = formatPrice(totalPrice);
        String imageUrl = cartItem.getImageUrl();

        holder.tvProductName.setText(productName);
        holder.tvProductPrice.setText("Total Price: ₱" + formattedPrice);
        holder.tvProductQuantity.setText("Quantity: " + cartItem.getQuantity());

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.ivProductImage);
        }

        // Show or hide checkbox based on flag
        if (showCheckboxes) {
            holder.checkbox.setVisibility(View.VISIBLE);
        } else {
            holder.checkbox.setVisibility(View.GONE);
        }

        // Handle checkbox selection
        holder.checkbox.setChecked(selectedItems.contains(cartItem));
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(cartItem);
            } else {
                selectedItems.remove(cartItem);
            }
            removeItemListener.onItemSelectionChanged(selectedItems);
        });

        // Long press listener to toggle checkbox visibility
        holder.itemView.setOnLongClickListener(v -> {
            showCheckboxes = !showCheckboxes;
            notifyDataSetChanged();
            removeItemListener.onItemLongPress(cartItem);
            return true;
        });

        // Regular click listener to open the edit quantity dialog
        holder.itemView.setOnClickListener(v -> {
            if (showCheckboxes) {
                showCheckboxes = false;
                notifyDataSetChanged();
            } else {
                openEditQuantityDialog(cartItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public List<CartItem> getSelectedItems() {
        return selectedItems;
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    // Method to toggle delete mode externally (show/hide checkboxes)
    public void setDeleteMode(boolean deleteMode) {
        showCheckboxes = deleteMode;
        notifyDataSetChanged();
    }

    // Method to open the edit quantity dialog
    private void openEditQuantityDialog(CartItem cartItem) {
        String sellerId = cartItem.getSellerId();
        String productId = cartItem.getProductId();

        // Log these values to check if they're null or not
        Log.d("CartAdapter", "sellerId: " + sellerId);
        Log.d("CartAdapter", "productId: " + productId);

        // Validate if sellerId or productId is null
        if (sellerId == null || productId == null) {
            Log.e("CartAdapter", "Either sellerId or productId is null.");
            Toast.makeText(context, "Invalid seller or product ID.", Toast.LENGTH_SHORT).show();
            return; // Early exit if IDs are null
        }

        // Proceed with the rest of the code if IDs are valid
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Quantity");

        // Inflate custom layout for the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_quantity, null);
        builder.setView(dialogView);

        ImageView ivProductImage = dialogView.findViewById(R.id.iv_dialog_product_image);
        TextView tvProductName = dialogView.findViewById(R.id.tv_dialog_product_name);
        TextView tvProductPrice = dialogView.findViewById(R.id.tv_dialog_product_price);
        TextView tvAvailableQuantity = dialogView.findViewById(R.id.tv_dialog_available_quantity);
        EditText etQuantity = dialogView.findViewById(R.id.et_dialog_quantity);

        // Set the product details in the dialog
        String productName = cartItem.getProductName();
        double totalPrice = cartItem.getTotalPrice(); // Already calculated in CartItem
        String formattedPrice = formatPrice(totalPrice);
        String imageUrl = cartItem.getImageUrl();

        tvProductName.setText(productName);
        tvProductPrice.setText("Total Price: ₱" + formattedPrice);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(ivProductImage);
        }

        // Fetch available quantity from Firestore and show it in the dialog
        db.collection("users") // The 'users' collection
                .document(sellerId) // Accessing the seller's document
                .collection("products") // The 'products' subcollection
                .document(productId) // Accessing the product document using the productId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.contains("quantity")) { // Ensure 'quantity' field exists
                            Long availableQuantityLong = document.getLong("quantity");
                            if (availableQuantityLong != null) {
                                long availableQuantity = availableQuantityLong;
                                tvAvailableQuantity.setText("Available Quantity: " + availableQuantity);

                                // Set the current quantity in the input field
                                etQuantity.setText(String.valueOf(cartItem.getQuantity()));

                                builder.setPositiveButton("Save", (dialog, which) -> {
                                    String quantityText = etQuantity.getText().toString();

                                    if (!quantityText.isEmpty()) {
                                        try {
                                            int newQuantity = Integer.parseInt(quantityText); // Parse quantity

                                            // Validate the new quantity
                                            if (newQuantity <= 0) {
                                                Toast.makeText(context, "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
                                            } else if (newQuantity <= availableQuantity) {
                                                cartItem.setQuantity(newQuantity);
                                                notifyDataSetChanged();

                                                String documentId = cartItem.getDocumentId();
                                                if (documentId != null) {
                                                    db.collection("buyers")
                                                            .document(cartItem.getUserId())
                                                            .collection("cartItems")
                                                            .document(documentId)
                                                            .update("quantity", newQuantity)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(context, "Quantity updated successfully.", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("FirestoreError", "Error updating quantity", e);
                                                                Toast.makeText(context, "Failed to update quantity.", Toast.LENGTH_SHORT).show();
                                                            });
                                                } else {
                                                    Log.e("CartAdapter", "Document ID is null. Cannot update quantity.");
                                                    Toast.makeText(context, "Failed to update quantity. Invalid document.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(context, "Not enough stock available.", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (NumberFormatException e) {
                                            Toast.makeText(context, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "Quantity cannot be empty.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                                builder.show();
                            } else {
                                Toast.makeText(context, "Quantity not available for this product.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Quantity field not found in the product.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Product not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching product details", e);
                    Toast.makeText(context, "Failed to fetch product details.", Toast.LENGTH_SHORT).show();
                });
    }







    // ViewHolder class to hold UI components
    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity;
        ImageView ivProductImage;
        CheckBox checkbox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductQuantity = itemView.findViewById(R.id.tv_product_quantity);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            checkbox = itemView.findViewById(R.id.checkbox_cart_item);
        }
    }
}
