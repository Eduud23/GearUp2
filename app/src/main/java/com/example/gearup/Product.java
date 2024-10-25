package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Product implements Parcelable {
    private String id;
    private String name;
    private double price;
    private String description;
    private List<String> imageUrls; // List of image URLs
    private String category;
    private String sellerId;
    private String sellerProfileImageUrl;
    private int quantity;

    // New fields for brand and year model
    private String brand; // Brand field
    private String yearModel; // Year model field

    public Product() {
        // Default constructor
    }

    public Product(String id, String name, double price, String description, List<String> imageUrls,
                   String category, String sellerId, int quantity, String brand, String yearModel) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrls = imageUrls;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerProfileImageUrl = ""; // Default value
        this.quantity = quantity;
        this.brand = brand; // Set brand
        this.yearModel = yearModel; // Set year model
    }

    // Parcelable implementation
    protected Product(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        description = in.readString();
        imageUrls = in.createStringArrayList();
        category = in.readString();
        sellerId = in.readString();
        sellerProfileImageUrl = in.readString();
        quantity = in.readInt();
        brand = in.readString(); // Read brand
        yearModel = in.readString(); // Read year model
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
        dest.writeStringList(imageUrls);
        dest.writeString(category);
        dest.writeString(sellerId);
        dest.writeString(sellerProfileImageUrl);
        dest.writeInt(quantity);
        dest.writeString(brand); // Write brand
        dest.writeString(yearModel); // Write year model
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

    public List<String> getImageUrls() {
        return imageUrls; // Getter for image URLs
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls; // Setter for image URLs
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

    // Method to adjust quantity
    public void adjustQuantity(int amount) {
        if (this.quantity + amount >= 0) {
            this.quantity += amount;
        } else {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    // Getters and setters for brand and year model
    public String getBrand() {
        return brand; // Getter for brand
    }

    public void setBrand(String brand) {
        this.brand = brand; // Setter for brand
    }

    public String getYearModel() {
        return yearModel; // Getter for year model
    }

    public void setYearModel(String yearModel) {
        this.yearModel = yearModel; // Setter for year model
    }
}
