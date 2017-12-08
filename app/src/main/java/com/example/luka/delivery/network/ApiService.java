package com.example.luka.delivery.network;

/**
 * Created by luka on 16.7.2017..
 */

import com.example.luka.delivery.entities.AccessToken;
import com.example.luka.delivery.entities.PostResponse;

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
                                   @Field("password") String password);

        @POST("login")
        @FormUrlEncoded
        Call<AccessToken> login(@Field("username") String username,
                                @Field("password") String password);

        @POST("social_auth")
        @FormUrlEncoded
        Call<AccessToken> socialAuth(@Field("name") String name,
                                     @Field("email") String email,
                                     @Field("provider") String provider,
                                     @Field("provider_user_id") String providerUserId);

        @POST("refresh")
        @FormUrlEncoded
        Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

        @GET("posts")
        Call<PostResponse> posts();
}


