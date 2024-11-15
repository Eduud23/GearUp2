package com.example.gearup;

public class Shop {
    private String shopName;
    private String address;
    private String phone;
    private String sellerId;  // Add sellerId to the Shop model

    // Constructor
    public Shop(String shopName, String address, String phone, String sellerId) {
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.sellerId = sellerId;
    }

    // Getters and setters
    public String getShopName() {
        return shopName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getSellerId() {
        return sellerId;
    }
}
