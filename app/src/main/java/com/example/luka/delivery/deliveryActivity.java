package com.example.luka.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.DeliveryResponse;
import com.example.luka.delivery.entities.MapLocation;
import com.example.luka.delivery.network.ApiService;
import com.example.luka.delivery.network.RetrofitBuilder;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class deliveryActivity extends AppCompatActivity {

    ApiService service;
    TokenManager tokenManager;
    Call<DeliveryResponse> call;
    List<Delivery> deliveryList;

    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    private static final String TAG = "deliveryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        ButterKnife.bind(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tokenManager = TokenManager.getInstance((getSharedPreferences("prefs", MODE_PRIVATE)));

        if(tokenManager.getToken()==null){
            startActivity(new Intent(deliveryActivity.this, loginActivity.class));
            finish();
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        getDeliveries();
    }

    public void getDeliveries() {

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
                            Geocoder geocoder = new Geocoder(getApplicationContext());

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
                    startActivity(new Intent(deliveryActivity.this, loginActivity.class));
                    finish();

                }

                //creating recyclerview adapter
                deliveryAdapter adapter = new deliveryAdapter(getApplicationContext(), deliveryList);
                //setting adapter to recyclerview
                recyclerView.setAdapter(adapter);

                //add loading screenO

            }

            @Override
            public void onFailure(Call<DeliveryResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }


        });


    }
}
