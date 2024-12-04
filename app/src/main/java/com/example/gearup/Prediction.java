package com.example.gearup;

public class Prediction {
    private String product;
    private double predictedSales;

    public Prediction(String product, double predictedSales) {
        this.product = product;
        this.predictedSales = predictedSales;
    }

    public String getProduct() {
        return product;
    }

    public double getPredictedSales() {
        return predictedSales;
    }
}