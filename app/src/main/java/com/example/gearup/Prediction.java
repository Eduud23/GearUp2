package com.example.gearup;

public class Prediction {
    private String product;
    private double predicted_sales;

    // Constructor
    public Prediction(String product, double predicted_sales) {
        this.product = product;
        this.predicted_sales = predicted_sales;
    }

    // Getters and Setters
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getPredicted_sales() {
        return predicted_sales;
    }

    public void setPredicted_sales(double predicted_sales) {
        this.predicted_sales = predicted_sales;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "product='" + product + '\'' +
                ", predicted_sales=" + predicted_sales +
                '}';
    }
}
