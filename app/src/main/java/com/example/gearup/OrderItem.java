package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String productName;
    private double productPrice; // Changed to productPrice
    private int productQuantity;  // Changed to productQuantity
    private String productImageUrl;

    // Required empty constructor for Firestore
    public OrderItem() {}

    public OrderItem(String productName, double productPrice, int productQuantity, String productImageUrl) {
        this.productName = productName;
        this.productPrice = productPrice; // Update constructor to use productPrice
        this.productQuantity = productQuantity; // Update constructor to use productQuantity
        this.productImageUrl = productImageUrl;
    }

    protected OrderItem(Parcel in) {
        productName = in.readString();
        productPrice = in.readDouble(); // Update to read productPrice
        productQuantity = in.readInt();  // Update to read productQuantity
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

    public double getProductPrice() { // Updated getter for productPrice
        return productPrice;
    }

    public int getProductQuantity() { // Updated getter for productQuantity
        return productQuantity;
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
        dest.writeDouble(productPrice); // Update to write productPrice
        dest.writeInt(productQuantity);   // Update to write productQuantity
        dest.writeString(productImageUrl);
    }
}
