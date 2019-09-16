package com.pk.vigyaan.currencydetector.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ImageResponse implements Serializable {

    @SerializedName("response")
    @Expose
    String response;

    public String getResponse() {
        return response;
    }
}
