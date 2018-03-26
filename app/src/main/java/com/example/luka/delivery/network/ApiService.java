package com.example.luka.delivery.network;

import com.example.luka.delivery.entities.AccessToken;
import com.example.luka.delivery.entities.DeliveryResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("register")
    @FormUrlEncoded
    Call<AccessToken> register(@Field("name") String name,
                               @Field("email") String email,
                               @Field("password") String password,
                               @Field("required_role") String requiredRole);

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("username") String username,
                            @Field("password") String password);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @GET("getDeliveries")
    Call<DeliveryResponse> deliveries();

    @POST("setDelivered")
    @FormUrlEncoded
    Call<AccessToken> setDelivered(@Field("delivery_id") int id);

    @POST("updateLastLocation")
    @FormUrlEncoded
    Call<AccessToken> updateLastLocation(@Field("lastLocation") String lastLocation);

    @POST("logout")
    @FormUrlEncoded
    Call<AccessToken> logout(@Field("access_token") AccessToken accessToken);

}


