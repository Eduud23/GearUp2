package com.example.gearup;

public class RecommendLocalShop {
    private String image, shopName, kindOfService, timeSchedule, place, contactNumber, website;
    private double ratings, latitude, longitude;

    public RecommendLocalShop() {
        // No-argument constructor required by Firestore
    }

    public RecommendLocalShop(String image, String shopName, String kindOfService, String timeSchedule, String place, String contactNumber, double ratings, String website, double latitude, double longitude) {
        this.image = image;
        this.shopName = shopName;
        this.kindOfService = kindOfService;
        this.timeSchedule = timeSchedule;
        this.place = place;
        this.contactNumber = contactNumber;
        this.ratings = ratings;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getKindOfService() { return kindOfService; }
    public void setKindOfService(String kindOfService) { this.kindOfService = kindOfService; }

    public String getTimeSchedule() { return timeSchedule; }
    public void setTimeSchedule(String timeSchedule) { this.timeSchedule = timeSchedule; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public double getRatings() { return ratings; }
    public void setRatings(double ratings) { this.ratings = ratings; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}