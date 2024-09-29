package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String id;
    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private String category;
    private String sellerId;
    private String sellerProfileImageUrl; // Field for seller profile image URL
    private int quantity;

    // No-argument constructor for Firebase Firestore
    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    // Parameterized constructor (original)
    public Product(String id, String name, double price, String description, String imageUrl, String category, String sellerId, String sellerProfileImageUrl, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
        this.quantity = quantity; // Initialize the new field
    }

    // New constructor to match your InventoryFragment usage
    public Product(String id, String name, double price, String description, String imageUrl, String category, String sellerId, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerProfileImageUrl = ""; // Default value if not provided
        this.quantity = quantity;
    }

    // Parcelable implementation
    protected Product(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        description = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        sellerId = in.readString();
        sellerProfileImageUrl = in.readString();
        quantity = in.readInt(); // Read the new field
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeString(sellerId);
        dest.writeString(sellerProfileImageUrl);
        dest.writeInt(quantity); // Write the new field
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerProfileImageUrl() {
        return sellerProfileImageUrl;
    }

    public void setSellerProfileImageUrl(String sellerProfileImageUrl) {
        this.sellerProfileImageUrl = sellerProfileImageUrl;
    }

    public int getQuantity() { // Getter for quantity
        return quantity;
    }

    public void setQuantity(int quantity) { // Setter for quantity
        this.quantity = quantity;
    }
}
