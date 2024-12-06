package com.example.gearup;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Define the endpoint to get the sales prediction
    @GET("predict")
    Call<JsonObject> getSalesPrediction(@Query("address") String address);
}

