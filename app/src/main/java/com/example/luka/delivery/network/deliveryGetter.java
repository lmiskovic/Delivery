package com.example.luka.delivery.network;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.luka.delivery.TokenManager;
import com.example.luka.delivery.entities.AccessToken;
import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.DeliveryResponse;
import com.example.luka.delivery.entities.MapLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class deliveryGetter {

    private ApiService service;
    private TokenManager tokenManager;
    private Call<DeliveryResponse> call;
    private List<Delivery> deliveryList;
    private String TAG = "deliveryGetter";
    private Context context;
    private AccessToken accessToken;
    public deliveryGetter(){

    }

    public deliveryGetter (Context context, AccessToken accessToken){
        this.context = context;
        tokenManager.saveToken(accessToken);
    }

    public List<Delivery> getDeliveries(){

        if(tokenManager.getToken() == null){
            Log.e(TAG,"token = null!");
            return null;
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);


        deliveryList = new ArrayList<>();

        call = service.deliveries();
        call.enqueue(new Callback<DeliveryResponse>() {
            @Override
            public void onResponse(Call<DeliveryResponse> call, Response<DeliveryResponse> response) {

                Log.w(TAG, "onResponse: " + response );

                if(response.isSuccessful()){
                    for (int i = 0; i < response.body().getData().size(); i++) {
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

                        if(deliveryList.get(i).getMapLocation()==null){
                            Geocoder geocoder = new Geocoder(context);

                            List<Address> addresses = null;
                            try {
                                addresses = geocoder.getFromLocationName(deliveryList.get(i).getDeliveryAddress(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(addresses.size() > 0) {
                                double latitude= addresses.get(0).getLatitude();
                                double longitude= addresses.get(0).getLongitude();
                                deliveryList.get(i).setMapLocation(new MapLocation(latitude, longitude));
                            }
                        }

                        Log.i(TAG, deliveryList.get(i).getId() + "\n"
                                + deliveryList.get(i).getCreated_at() + "\n"
                                + deliveryList.get(i).getUpdated_at() + "\n"
                                + deliveryList.get(i).getUser_id() + "\n"
                                + deliveryList.get(i).getDeliveryAddress() + "\n"
                                + deliveryList.get(i).getCustomerName() + "\n"
                                + deliveryList.get(i).getContactPhoneNumber() + "\n"
                                + deliveryList.get(i).getNote() + "\n"
                                + deliveryList.get(i).getMapLocation().getLatLng().latitude + " "
                                + deliveryList.get(i).getMapLocation().getLatLng().longitude + "\n");
                    }
                    /*String deliveryList = response.body().getData().get(0).getId() + "\n"
                            + response.body().getData().get(0).getCustomerName() + "\n"
                            + response.body().getData().get(0).getContactPhoneNumber() + "\n"
                            + response.body().getData().get(0).getDeliveryAddress() + "\n"
                            + response.body().getData().get(0).getNote() + "\n";*/
                    //deliveriesList.setText(deliveryList);
                }else {
                    tokenManager.deleteToken();
                    //startActivity(new Intent(context, loginActivity.class));
                }
            }

            @Override
            public void onFailure(Call<DeliveryResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });
        return deliveryList;
    }

}
