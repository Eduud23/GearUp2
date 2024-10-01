package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Product implements Parcelable {
    private String id;
    private String name;
    private double price;
    private String description;
    private List<String> imageUrls; // Change this to List<String>
    private String category;
    private String sellerId;
    private String sellerProfileImageUrl; // Field for seller profile image URL
    private int quantity;

    // No-argument constructor for Firebase Firestore
    public Product() {
        // Default constructor required for calls to DataSnapshot.getValue(Product.class)
    }

    // Updated constructor for multiple images
    public Product(String id, String name, double price, String description, List<String> imageUrls, String category, String sellerId, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrls = imageUrls; // Set the list of image URLs
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
        imageUrls = in.createStringArrayList(); // Read the list of image URLs
        category = in.readString();
        sellerId = in.readString();
        sellerProfileImageUrl = in.readString();
        quantity = in.readInt();
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
        dest.writeStringList(imageUrls); // Write the list of image URLs
        dest.writeString(category);
        dest.writeString(sellerId);
        dest.writeString(sellerProfileImageUrl);
        dest.writeInt(quantity);
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

    public List<String> getImageUrls() { // New getter for image URLs
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) { // New setter for image URLs
        this.imageUrls = imageUrls;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // New method to adjust quantity
    public void adjustQuantity(int amount) {
        if (this.quantity + amount >= 0) {
            this.quantity += amount;
        } else {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }
}
