package com.example.luka.delivery;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.DeliveryResponse;
import com.example.luka.delivery.entities.MapLocation;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.network.ApiService;
import com.example.luka.delivery.network.DeliveryGetter;
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

        DeliveryGetter deliveryGetter = new DeliveryGetter(this);
        deliveryGetter.call(new onDeliveryListener() {
            @Override
            public void onDelivery(List<Delivery> deliveryList) {
                //creating recyclerview adapter
                deliveryAdapter adapter = new deliveryAdapter(getApplicationContext(), deliveryList);
                //setting adapter to recyclerview
                recyclerView.setAdapter(adapter);            }
        });
    }
}
