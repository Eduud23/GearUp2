package com.example.gearup;

import java.util.List;

public class ForecastModel {
    private String productLine;
    private List<Float> xValues;
    private List<Float> yValues;
    private List<String> labels;
    private String forecastDate;
    private float forecastValue;
    private String trendDirection;

    public ForecastModel(String productLine, List<Float> xValues, List<Float> yValues, List<String> labels,
                         String forecastDate, float forecastValue, String trendDirection) {
        this.productLine = productLine;
        this.xValues = xValues;
        this.yValues = yValues;
        this.labels = labels;
        this.forecastDate = forecastDate;
        this.forecastValue = forecastValue;
        this.trendDirection = trendDirection;
    }

    // Getters
    public String getProductLine() { return productLine; }
    public List<Float> getXValues() { return xValues; }
    public List<Float> getYValues() { return yValues; }
    public List<String> getLabels() { return labels; }
    public String getForecastDate() { return forecastDate; }
    public float getForecastValue() { return forecastValue; }
    public String getTrendDirection() { return trendDirection; }
}
