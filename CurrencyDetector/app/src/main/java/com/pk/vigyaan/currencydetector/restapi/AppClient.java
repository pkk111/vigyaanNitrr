package com.pk.vigyaan.currencydetector.restapi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppClient {

    private static String BASE_URL = "";
    private static AppClient mInstance;

    private AppClient() {
    }

    public static synchronized AppClient getInstance() {
        if (mInstance == null) mInstance = new AppClient();
        return mInstance;
    }

    public <S> S createService(Class<S> serviceClass) {
        OkHttpClient.Builder httpClient = getOKHttpClient();
        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL).client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(serviceClass);
    }

    private OkHttpClient.Builder getOKHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.connectTimeout(15, TimeUnit.SECONDS);
        httpClient.writeTimeout(15, TimeUnit.SECONDS);
        httpClient.readTimeout(15, TimeUnit.SECONDS);

        return httpClient;
    }

}