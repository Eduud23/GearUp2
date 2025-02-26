package com.example.gearup;

public class PopularProduct {
    private String title;
    private String price;
    private String imageUrl;
    private String itemUrl;
    private String condition;
    private String location;
    private String shippingCost;


    public PopularProduct(String title, String price, String imageUrl, String itemUrl, String condition, String location, String shippingCost) {
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.itemUrl = itemUrl;
        this.condition = condition;
        this.location = location;
        this.shippingCost = shippingCost;
    }

    // Getters
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getItemUrl() { return itemUrl; }
    public String getCondition() { return condition; }
    public String getLocation() { return location; }
    public String getShippingCost() { return shippingCost; }

}
