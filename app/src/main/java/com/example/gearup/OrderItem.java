package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String documentId; // Field for the document ID
    private String productName;
    private double productPrice;
    private int productQuantity;
    private String productImageUrl;
    private String orderStatus; // Field for order status
    private String shippingMethod; // Field for shipping method (Pick Up / Delivery)

    // Default constructor for Firestore
    public OrderItem() {}

    // Constructor with all fields, including shippingMethod
    public OrderItem(String documentId, String productName, double productPrice, int productQuantity, String productImageUrl, String orderStatus, String shippingMethod) {
        this.documentId = documentId; // Initialize document ID
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.orderStatus = orderStatus; // Initialize order status
        this.shippingMethod = shippingMethod; // Initialize shipping method
    }

    // Constructor to read from Parcel
    protected OrderItem(Parcel in) {
        documentId = in.readString(); // Read document ID from parcel
        productName = in.readString();
        productPrice = in.readDouble();
        productQuantity = in.readInt();
        productImageUrl = in.readString();
        orderStatus = in.readString(); // Read order status from parcel
        shippingMethod = in.readString(); // Read shipping method from parcel
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

    public String getShippingMethod() {
        return shippingMethod; // Getter for shipping method
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod; // Setter for shipping method
    }

    @Override
    public int describeContents() {
        return 0; // No special contents
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId); // Write document ID to parcel
        dest.writeString(productName);
        dest.writeDouble(productPrice);
        dest.writeInt(productQuantity);
        dest.writeString(productImageUrl);
        dest.writeString(orderStatus); // Write order status to parcel
        dest.writeString(shippingMethod); // Write shipping method to parcel
    }
}
