package com.example.luka.delivery;

import android.location.Location;

import com.example.luka.delivery.entities.ApiError;
import com.example.luka.delivery.network.RetrofitBuilder;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class Utils {

    public static ApiError converErrors(ResponseBody response){
        Converter<ResponseBody, ApiError> converter = RetrofitBuilder.getRetrofit().responseBodyConverter(ApiError.class, new Annotation[0]);

        ApiError apiError = null;

        try {
            apiError = converter.convert(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiError;
    }

    public static LatLng toLatLng(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        return latLng;
    }

}