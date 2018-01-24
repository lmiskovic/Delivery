package com.example.luka.delivery.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.util.Log;

import com.example.luka.delivery.TokenManager;
import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.DeliveryResponse;
import com.example.luka.delivery.entities.MapLocation;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.loginActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.internal.zzahn.runOnUiThread;

public class DeliveryGetter {

    TokenManager tokenManager;

    private ApiService service;
    private Call<DeliveryResponse> call;
    private List<Delivery> deliveryList;
    private String TAG = "deliveryGetter";
    private Context context;
    SharedPreferences prefs;

    public DeliveryGetter(Context context){
        this.context = context;
        prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
    }

    public void call(final onDeliveryListener onDeliveryListener) {

        tokenManager = TokenManager.getInstance(prefs);

        if(tokenManager.getToken()==null){
            context.startActivity(new Intent(context,loginActivity.class));
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        deliveryList = new ArrayList<>();

        call = service.deliveries();

        call.enqueue(new Callback<DeliveryResponse>() {
            @Override
            public void onResponse(Call<DeliveryResponse> call, Response<DeliveryResponse> response) {

                if (response.isSuccessful()) {

                    Log.i(TAG,"RESPONSE");

                    for (int i = 0; i < response.body().getData().size(); i++) {
                        Log.i(TAG,"CONVERTING...");
                        deliveryList.add(

                        new Delivery(
                                        response.body().getData().get(i).getId(),
                                        response.body().getData().get(i).getCreated_at(),
                                        response.body().getData().get(i).getUpdated_at(),
                                        response.body().getData().get(i).getUser_id(),
                                        response.body().getData().get(i).getDeliveryAddress(),
                                        response.body().getData().get(i).getCustomerName(),
                                        response.body().getData().get(i).getContactPhoneNumber(),
                                        response.body().getData().get(i).getNote(),
                                        response.body().getData().get(i).getMapLocation()
                                ));

                        Log.i(TAG,"CONVERTING DONE");


                        if (deliveryList.get(i).getMapLocation() == null) {
                            Log.i(TAG,"GEOCODING...");

                            Geocoder geocoder = new Geocoder(context);

                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocationName(deliveryList.get(i).getDeliveryAddress(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (addresses.size() > 0) {
                                double latitude = addresses.get(0).getLatitude();
                                double longitude = addresses.get(0).getLongitude();
                                deliveryList.get(i).setMapLocation(new MapLocation(latitude, longitude));
                            }

                            Log.i(TAG,"GEOCODING DONE");

                        }
                    }
                } else {
                    tokenManager.deleteToken();
                }

                Handler mainHandler = new Handler(context.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onDeliveryListener.onDelivery(deliveryList);
                    }
                });
            }

            @Override
            public void onFailure(Call<DeliveryResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
