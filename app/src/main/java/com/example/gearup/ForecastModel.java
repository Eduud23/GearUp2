package com.example.gearup;

import java.util.List;

public class ForecastModel {
    private String productLine;
    private List<Float> xValues;
    private List<Float> yValues;
    private List<String> labels;
    private String forecastDate;
    private float forecastSales; // Renamed from forecastValue to forecastSales
    private float forecastQuantity; // New field for forecasted quantity
    private String trendDirection;

    // Updated constructor to include forecasted quantity
    public ForecastModel(String productLine, List<Float> xValues, List<Float> yValues, List<String> labels,
                         String forecastDate, float forecastSales, float forecastQuantity, String trendDirection) {
        this.productLine = productLine;
        this.xValues = xValues;
        this.yValues = yValues;
        this.labels = labels;
        this.forecastDate = forecastDate;
        this.forecastSales = forecastSales;
        this.forecastQuantity = forecastQuantity;
        this.trendDirection = trendDirection;
    }

    // Getters
    public String getProductLine() { return productLine; }
    public List<Float> getXValues() { return xValues; }
    public List<Float> getYValues() { return yValues; }
    public List<String> getLabels() { return labels; }
    public String getForecastDate() { return forecastDate; }
    public float getForecastSales() { return forecastSales; }
    public float getForecastQuantity() { return forecastQuantity; }
    public String getTrendDirection() { return trendDirection; }
}
