package com.example.gearup;

public class PopularItem {
    private String address;
    private String zipCode;
    private String productImage;
    private int productQuantity;

    // Default constructor
    public PopularItem() {
        // No initialization, can set fields later
    }

    // Constructor with all fields
    public PopularItem(String address, String zipCode, String productImage, int productQuantity) {
        this.address = address;
        this.zipCode = zipCode;
        this.productImage = productImage;
        this.productQuantity = productQuantity;
    }

    // Getters and setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }
}
