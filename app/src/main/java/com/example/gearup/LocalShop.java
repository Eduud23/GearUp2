package com.example.gearup;

public class LocalShop {
    private String shopName, image, kindOfRepair, timeSchedule, place, contactNumber, website;
    private double ratings, latitude, longitude, distance;

    public LocalShop(String shopName, String image, String kindOfRepair, String timeSchedule, String place, String contactNumber, double ratings, String website, double latitude, double longitude, double distance) {
        this.shopName = shopName;
        this.image = image;
        this.kindOfRepair = kindOfRepair;
        this.timeSchedule = timeSchedule;
        this.place = place;
        this.contactNumber = contactNumber;
        this.ratings = ratings;
        this.website = website;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public String getShopName() { return shopName; }
    public String getImage() { return image; }
    public String getKindOfRepair() { return kindOfRepair; }
    public String getTimeSchedule() { return timeSchedule; }
    public String getPlace() { return place; }
    public String getContactNumber() { return contactNumber; }
    public double getRatings() { return ratings; }
    public String getWebsite() { return website; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getDistance() { return distance; }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
