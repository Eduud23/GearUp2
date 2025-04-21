package com.example.gearup;

public class Shop {
    private String shopName;
    private String address;
    private String phone;
    private String sellerId;
    private String profileImageUrl;  // Profile image URL
    private float distance;  // Distance in kilometers from user's location

    // Updated constructor to include distance
    public Shop(String shopName, String address, String phone, String sellerId, String profileImageUrl, float distance) {
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.sellerId = sellerId;
        this.profileImageUrl = profileImageUrl;
        this.distance = distance;  // Initialize the distance
    }

    // Getters and setters for all fields
    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public float getDistance() {
        return distance;  // Getter for distance
    }

    public void setDistance(float distance) {
        this.distance = distance;  // Setter for distance
    }
}
