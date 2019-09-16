package com.pk.vigyaan.currencydetector.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ImageModel implements Serializable {

    @SerializedName("image")
    @Expose
    String apptoken;

    public ImageModel(String apptoken) {
        this.apptoken = apptoken;
    }

    public String getApptoken() {
        return apptoken;
    }
}
