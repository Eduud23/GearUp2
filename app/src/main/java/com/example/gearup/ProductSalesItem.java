package com.example.gearup;

public class ProductSalesItem {
    private String productName;
    private double productPrice;
    private int productQuantity;
    private String productYearModel;
    private String productImage; // Add productImage field

    public ProductSalesItem(String productName, double productPrice, int productQuantity,
                            String productYearModel, String productImage) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productYearModel = productYearModel;
        this.productImage = productImage;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductYearModel() {
        return productYearModel;
    }

    public void setProductYearModel(String productYearModel) {
        this.productYearModel = productYearModel;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
}

