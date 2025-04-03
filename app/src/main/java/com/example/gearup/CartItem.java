package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String productName;  // Product name
    private int quantity;
    private String sellerId;     // Seller ID
    private double totalPrice;   // Total price for this item
    private String userId;       // User ID
    private String imageUrl;     // Image URL of the product
    private String documentId;   // Firestore Document ID
    private String brand;        // Brand of the product
    private String yearModel;    // Year model of the product

    private String productId;

    // Default constructor (needed for Firebase)
    public CartItem() {}

    // Updated constructor to include brand and yearModel
    public CartItem(String productName, int quantity, String sellerId, double totalPrice,
                    String userId, String imageUrl, String brand, String yearModel, String productId) {
        this.productName = productName;
        this.quantity = quantity;
        this.sellerId = sellerId;
        this.totalPrice = totalPrice;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.yearModel = yearModel;
        this.productId = productId;
    }

    protected CartItem(Parcel in) {
        productName = in.readString();
        quantity = in.readInt();
        sellerId = in.readString();
        totalPrice = in.readDouble();
        userId = in.readString();
        imageUrl = in.readString();
        documentId = in.readString();
        brand = in.readString();   // Read brand
        yearModel = in.readString(); // Read yearModel
        productId = in.readString();
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
        dest.writeString(productName);
        dest.writeInt(quantity);
        dest.writeString(sellerId);
        dest.writeDouble(totalPrice);
        dest.writeString(userId);
        dest.writeString(imageUrl);
        dest.writeString(documentId);
        dest.writeString(brand);  // Write brand
        dest.writeString(yearModel); // Write yearModel
        dest.writeString(productId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getProductId() {
        return productId;
    }

    // Setter for productId
    public void setProductId(String productId) {
        this.productId = productId;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getYearModel() {
        return yearModel;
    }

    public void setYearModel(String yearModel) {
        this.yearModel = yearModel;
    }
}
