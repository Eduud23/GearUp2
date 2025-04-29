package com.example.gearup;

import android.os.Parcel;
import android.os.Parcelable;

public class PopularProduct implements Parcelable {

    private String title;
    private String price;
    private String imageUrl;
    private String itemUrl;
    private String condition;
    private String location;
    private String shippingCost;
    private String discount;
    private String rated;
    private String seller;

    // New property to keep track of the match count for search results
    private int matchCount;

    // Constructor
    public PopularProduct(String title, String price, String imageUrl, String itemUrl,
                          String condition, String location, String shippingCost,
                          String discount, String rated, String seller) {
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.itemUrl = itemUrl;
        this.condition = condition;
        this.location = location;
        this.shippingCost = shippingCost;
        this.discount = discount;
        this.rated = rated;
        this.seller = seller;
        this.matchCount = 0; // Initialize match count to 0
    }

    // Constructor to create object from a Parcel
    protected PopularProduct(Parcel in) {
        title = in.readString();
        price = in.readString();
        imageUrl = in.readString();
        itemUrl = in.readString();
        condition = in.readString();
        location = in.readString();
        shippingCost = in.readString();
        discount = in.readString();
        rated = in.readString();
        seller = in.readString();
        matchCount = in.readInt();
    }

    // Getter and setter methods for matchCount
    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    // Getter methods for product fields
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getItemUrl() { return itemUrl; }
    public String getCondition() { return condition; }
    public String getLocation() { return location; }
    public String getShippingCost() { return shippingCost; }
    public String getDiscount() { return discount; }
    public String getRated() { return rated; }
    public String getSeller() { return seller; }

    // Parcelable implementation methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(price);
        dest.writeString(imageUrl);
        dest.writeString(itemUrl);
        dest.writeString(condition);
        dest.writeString(location);
        dest.writeString(shippingCost);
        dest.writeString(discount);
        dest.writeString(rated);
        dest.writeString(seller);
        dest.writeInt(matchCount);
    }

    // Creator
    public static final Creator<PopularProduct> CREATOR = new Creator<PopularProduct>() {
        @Override
        public PopularProduct createFromParcel(Parcel in) {
            return new PopularProduct(in);
        }

        @Override
        public PopularProduct[] newArray(int size) {
            return new PopularProduct[size];
        }
    };
}
