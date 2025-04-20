package com.example.gearup;

public class PopularProduct {
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
}
