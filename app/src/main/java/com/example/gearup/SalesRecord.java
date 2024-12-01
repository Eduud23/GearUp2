package com.example.gearup;

public class SalesRecord {
    private String productName;
    private String productBrand;
    private String productYearModel;
    private int productQuantity;
    private long timestamp;
    private String sellerId;
    private String buyerId;
    private String address;  // New field for seller's address
    private String shopName; // New field for seller's shop name
    private double productPrice;
    private String productImage;

    // Constructor to include address and shopName
    public SalesRecord(String productName, String productBrand, String productYearModel, int productQuantity,
                       long timestamp, String sellerId, String buyerId, String address, String shopName, double productPrice,String productImage) {
        this.productName = productName;
        this.productBrand = productBrand;
        this.productYearModel = productYearModel;
        this.productQuantity = productQuantity;
        this.timestamp = timestamp;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.address = address;
        this.shopName = shopName;
        this.productPrice = productPrice;
        this.productImage = productImage;
    }
    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productName = productPrice;
    }
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductYearModel() {
        return productYearModel;
    }

    public void setProductYearModel(String productYearModel) {
        this.productYearModel = productYearModel;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
