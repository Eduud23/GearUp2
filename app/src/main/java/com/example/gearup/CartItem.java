package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private Product product;
    private int quantity;
    private String documentId; // Firestore Document ID
    private String sellerId; // Seller ID

    // Default constructor (needed for Firebase)
    public CartItem() {}

    public CartItem(Product product, int quantity, String sellerId) {
        this.product = product;
        this.quantity = quantity;
        this.sellerId = sellerId;
    }

    protected CartItem(Parcel in) {
        product = in.readParcelable(Product.class.getClassLoader());
        quantity = in.readInt();
        documentId = in.readString();
        sellerId = in.readString();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(product, flags);
        dest.writeInt(quantity);
        dest.writeString(documentId);
        dest.writeString(sellerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters
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

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}