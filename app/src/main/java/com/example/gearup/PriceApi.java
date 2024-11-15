package com.example.gearup;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PriceApi {
    @POST("/predict")
    Call<PriceResponse> predictPrice(@Body PriceRequest request);

    @POST("/add_product")
    Call<ResponseBody> addProduct(@Body ConcreteProductData productData);

}
