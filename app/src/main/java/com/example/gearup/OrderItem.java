package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String documentId; // Field for the document ID
    private String productName;
    private String productBrand; // Add productBrand field
    private String productYearModel; // Add productYearModel field
    private double productPrice;
    private int productQuantity;
    private String productImageUrl;
    private String orderStatus; // Field for order status
    private String shippingMethod; // Field for shipping method (Pick Up / Delivery)
    private String sellerId; // Assuming sellerId is part of the order
    private String buyerId;  // Assuming buyerId is part of the order

    // Default constructor for Firestore
    public OrderItem() {}

    // Constructor with all fields
    public OrderItem(String documentId, String productName, String productBrand, String productYearModel,
                     double productPrice, int productQuantity, String productImageUrl,
                     String orderStatus, String shippingMethod, String sellerId, String buyerId) {
        this.documentId = documentId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.productYearModel = productYearModel;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productImageUrl = productImageUrl;
        this.orderStatus = orderStatus;
        this.shippingMethod = shippingMethod;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
    }

    // Constructor to read from Parcel
    protected OrderItem(Parcel in) {
        documentId = in.readString();
        productName = in.readString();
        productBrand = in.readString();  // Read productBrand from parcel
        productYearModel = in.readString();  // Read productYearModel from parcel
        productPrice = in.readDouble();
        productQuantity = in.readInt();
        productImageUrl = in.readString();
        orderStatus = in.readString();
        shippingMethod = in.readString();
        sellerId = in.readString();  // Read sellerId from parcel
        buyerId = in.readString();   // Read buyerId from parcel
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
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductBrand() {
        return productBrand; // Getter for productBrand
    }

    public String getProductYearModel() {
        return productYearModel; // Getter for productYearModel
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
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getSellerId() {
        return sellerId; // Getter for sellerId
    }

    public String getBuyerId() {
        return buyerId; // Getter for buyerId
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentId);
        dest.writeString(productName);
        dest.writeString(productBrand);  // Write productBrand to parcel
        dest.writeString(productYearModel);  // Write productYearModel to parcel
        dest.writeDouble(productPrice);
        dest.writeInt(productQuantity);
        dest.writeString(productImageUrl);
        dest.writeString(orderStatus);
        dest.writeString(shippingMethod);
        dest.writeString(sellerId);  // Write sellerId to parcel
        dest.writeString(buyerId);   // Write buyerId to parcel
    }
}
