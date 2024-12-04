package com.example.gearup;

import java.util.List;

public class AddressData {
    private String address;
    private List<Prediction> nextMonthPredictions;

    public AddressData(String address, List<Prediction> nextMonthPredictions) {
        this.address = address;
        this.nextMonthPredictions = nextMonthPredictions;
    }

    public String getAddress() {
        return address;
    }

    public List<Prediction> getNextMonthPredictions() {
        return nextMonthPredictions;
    }
}
