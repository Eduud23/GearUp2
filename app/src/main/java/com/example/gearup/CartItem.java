package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String documentId; // Add this field for Firestore document ID
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
