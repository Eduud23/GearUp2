package com.example.gearup;

public class PopularProduct {
    private String title;
    private String price;
    private String imageUrl;
    private String itemUrl;

    // Constructor
    public PopularProduct(String title, String price, String imageUrl, String itemUrl) {
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
        this.itemUrl = itemUrl;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getItemUrl() {
        return itemUrl;
    }
}