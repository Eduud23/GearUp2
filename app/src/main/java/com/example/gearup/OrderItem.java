package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String documentId; // Add this field for the document ID
    private String productName;
    private double productPrice;
    private int productQuantity;
    private String productImageUrl;
    private String orderStatus; // Assuming you have an order status field

    // Required empty constructor for Firestore
    public OrderItem() {}

    public OrderItem(String documentId, String productName, double productPrice, int productQuantity, String productImageUrl, String orderStatus) {
        this.documentId = documentId; // Initialize document ID
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.orderStatus = orderStatus; // Initialize order status
    }

    protected OrderItem(Parcel in) {
        documentId = in.readString(); // Read document ID from parcel
        productName = in.readString();
        productPrice = in.readDouble();
        productQuantity = in.readInt();
        productImageUrl = in.readString();
        orderStatus = in.readString(); // Read order status from parcel
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

    // Getters and Setters
    public String getDocumentId() {
        return documentId; // Getter for document ID
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId; // Setter for document ID
    }

    public String getProductName() {
        return productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public String getOrderStatus() {
        return orderStatus; // Getter for order status
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus; // Setter for order status
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId); // Write document ID to parcel
        dest.writeString(productName);
        dest.writeDouble(productPrice);
        dest.writeInt(productQuantity);
        dest.writeString(productImageUrl);
        dest.writeString(orderStatus); // Write order status to parcel
    }
}
