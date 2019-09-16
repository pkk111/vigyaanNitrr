package com.pk.vigyaan.currencydetector.restapi;

import com.pk.vigyaan.currencydetector.Models.ImageModel;
import com.pk.vigyaan.currencydetector.Models.ImageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIServices {

    @POST("sdf/")
    Call<ImageResponse> sendImage(@Body ImageModel imageModel);
}
