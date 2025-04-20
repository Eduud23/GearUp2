package com.example.gearup;

public class LocalTrendsData {
    private String image;
    private String link;
    private String name;
    private String place;
    private double price;
    private String promo;
    private double ratings;
    private int sale;
    private String sold;  // Changed to String
    private int matchCount; // New field to store the number of matches

    // Constructor
    public LocalTrendsData(String image, String link, String name, String place, double price, String promo, double ratings, int sale, String sold) {
        this.image = image;
        this.link = link;
        this.name = name;
        this.place = place;
        this.price = price;
        this.promo = promo;
        this.ratings = ratings;
        this.sale = sale;
        this.sold = sold;
        this.matchCount = 0; // Default value for match count
    }

    // Default Constructor
    public LocalTrendsData() {
        this.matchCount = 0; // Initialize matchCount to 0
    }

    // Getters and Setters
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPromo() {
        return promo;
    }

    public void setPromo(String promo) {
        this.promo = promo;
    }

    public double getRatings() {
        return ratings;
    }

    public void setRatings(double ratings) {
        this.ratings = ratings;
    }

    public int getSale() {
        return sale;
    }

    public void setSale(int sale) {
        this.sale = sale;
    }

    public String getSold() {
        return sold;
    }

    public void setSold(String sold) {
        this.sold = sold;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }
}
