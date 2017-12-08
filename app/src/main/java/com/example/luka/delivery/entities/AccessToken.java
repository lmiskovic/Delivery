package com.example.luka.delivery.entities;

import com.squareup.moshi.Json;

/**
 * Created by luka on 16.7.2017..
 */

public class AccessToken {

    @Json(name="token_type")
    String tokenType;

    @Json(name="expires_in")
    int expiresIn;

    @Json(name="access_token")
    String accessToken;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Json(name="refresh_token")
    String refreshToken;

}
