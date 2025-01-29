package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String documentId; // Firestore document ID
    private Product product;
    private int quantity;

    // No-argument constructor required for Firestore
    public CartItem() {
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    protected CartItem(Parcel in) {
        documentId = in.readString(); // Read document ID from Parcel
        product = in.readParcelable(Product.class.getClassLoader());
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    // Getter and setter for documentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Method to get the total price for this CartItem (price * quantity)
    public double getTotalPrice() {
        double price = 0.0;
        try {
            // Directly use getPrice() since it's already a double
            price = product.getPrice();
        } catch (Exception e) {
            // Handle the case where the price is invalid (optional)
            e.printStackTrace(); // Optionally log the exception
        }
        return price * quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId); // Write document ID to Parcel
        dest.writeParcelable(product, flags);
        dest.writeInt(quantity);
    }
}
