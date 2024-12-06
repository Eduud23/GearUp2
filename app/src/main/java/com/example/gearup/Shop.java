package com.example.gearup;

public class Shop {
    private String shopName;
    private String address;
    private String phone;
    private String sellerId;
    private String profileImageUrl; // New field for the profile image URL

    public Shop(String shopName, String address, String phone, String sellerId, String profileImageUrl) {
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.sellerId = sellerId;
        this.profileImageUrl = profileImageUrl; // Initialize the profile image URL
    }

    // Getters and setters for all fields
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

    public String getProfileImageUrl() {
        return profileImageUrl; // Getter for profile image URL
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
