package com.example.gearup;

public class RecommendGasStation {
    private String name;
    private String kindOfService;
    private String place;
    private String contactNumber;
    private String website;
    private String timeSchedule;
    private double latitude;
    private double longitude;

    // Constructor
    public RecommendGasStation(String name, String kindOfService, String place, String contactNumber, String website, String timeSchedule, double latitude, double longitude) {
        this.name = name;
        this.kindOfService = kindOfService;
        this.place = place;
        this.contactNumber = contactNumber;
        this.website = website;
        this.timeSchedule = timeSchedule;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Default Constructor
    public RecommendGasStation() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKindOfService() {
        return kindOfService;
    }

    public void setKindOfService(String kindOfService) {
        this.kindOfService = kindOfService;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTimeSchedule() {
        return timeSchedule;
    }

    public void setTimeSchedule(String timeSchedule) {
        this.timeSchedule = timeSchedule;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

