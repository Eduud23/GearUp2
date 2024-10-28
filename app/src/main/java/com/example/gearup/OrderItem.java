package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String productName;
    private double price; // Changed from productPrice to price
    private int quantity;
    private String productImageUrl;

    // Required empty constructor for Firestore
    public OrderItem() {}

    public OrderItem(String productName, double price, int quantity, String productImageUrl) {
        this.productName = productName;
        this.price = price; // Update constructor to use price
        this.quantity = quantity;
        this.productImageUrl = productImageUrl;
    }

    protected OrderItem(Parcel in) {
        productName = in.readString();
        price = in.readDouble(); // Update to read price
        quantity = in.readInt();
        productImageUrl = in.readString();
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    public String getProductName() {
        return productName;
    }

    public double getPrice() { // Updated getter
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productName);
        dest.writeDouble(price); // Update to write price
        dest.writeInt(quantity);
        dest.writeString(productImageUrl);
    }
}
