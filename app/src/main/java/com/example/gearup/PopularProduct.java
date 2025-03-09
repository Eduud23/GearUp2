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
    }

    // Getter methods
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getItemUrl() { return itemUrl; }
    public String getCondition() { return condition; }  // <- Make sure this exists!
    public String getLocation() { return location; }
    public String getShippingCost() { return shippingCost; }
    public String getDiscount() { return discount; }
    public String getRated() { return rated; }
    public String getSeller() { return seller; }
}

