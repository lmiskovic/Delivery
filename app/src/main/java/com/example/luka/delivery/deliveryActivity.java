package com.example.luka.delivery;

import android.content.Intent;
import android.support.annotation.BinderThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.luka.delivery.entities.DeliveryResponse;
import com.example.luka.delivery.network.ApiService;
import com.example.luka.delivery.network.RetrofitBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class deliveryActivity extends AppCompatActivity {

    ApiService service;
    TokenManager tokenManager;
    Call<DeliveryResponse> call;

    private static final String TAG = "deliveryActivity";

    @BindView(R.id.deliveriesList) TextView deliveriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance((getSharedPreferences("prefs", MODE_PRIVATE)));

        if(tokenManager.getToken()==null){
            startActivity(new Intent(deliveryActivity.this, loginActivity.class));
            finish();
        }

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        call = service.deliveries();
        call.enqueue(new Callback<DeliveryResponse>() {
            @Override
            public void onResponse(Call<DeliveryResponse> call, Response<DeliveryResponse> response) {
                Log.w(TAG, "onResponse: " + response );

                if(response.isSuccessful()){
                    String deliveryList = response.body().getData().get(0).getId() + "\n"
                            + response.body().getData().get(0).getCustomerName() + "\n"
                            + response.body().getData().get(0).getContactPhoneNumber() + "\n"
                            + response.body().getData().get(0).getDeliveryAddress() + "\n"
                            + response.body().getData().get(0).getNote() + "\n";
                    deliveriesList.setText(deliveryList);
                }else {
                    tokenManager.deleteToken();
                    startActivity(new Intent(deliveryActivity.this, loginActivity.class));
                    finish();

                }
            }

            @Override
            public void onFailure(Call<DeliveryResponse> call, Throwable t) {
                Log.w(TAG, "onFailure: " + t.getMessage() );
            }
        });

    }
}
