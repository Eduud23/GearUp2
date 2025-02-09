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
    private String brand; // Brand field
    private String yearModel; // Year model field
    private int views; // Number of views
    private double stars; // Average star rating

    // Default constructor
    public Product() {}

    // Constructor with all parameters
    public Product(String id, String name, double price, String description, List<String> imageUrls,
                   String category, String sellerId, int quantity, String brand, String yearModel,
                   int views, double stars) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrls = imageUrls;
        this.category = category;
        this.sellerId = sellerId;
        this.sellerProfileImageUrl = ""; // Default value
        this.quantity = quantity;
        this.brand = brand;
        this.yearModel = yearModel;
        this.views = views;
        this.stars = stars;
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
        brand = in.readString();
        yearModel = in.readString();
        views = in.readInt();
        stars = in.readDouble();
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
        dest.writeString(brand);
        dest.writeString(yearModel);
        dest.writeInt(views);
        dest.writeDouble(stars);
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
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
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

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void incrementViews() {
        this.views++;
    }

    public double getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }
}
