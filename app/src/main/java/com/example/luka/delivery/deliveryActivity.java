package com.example.luka.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.network.DeliveryGetter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class deliveryActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    private static final String TAG = "deliveryActivity";
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_delivery);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading deliveries...");
        mProgressDialog.show();

        DeliveryGetter deliveryGetter = new DeliveryGetter(this);
        deliveryGetter.call(new onDeliveryListener() {
            @Override
            public void onDelivery(List<Delivery> deliveryList) {

                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                //creating recyclerview adapter
                deliveryAdapter adapter = new deliveryAdapter(getApplicationContext(), deliveryList);
                //setting adapter to recyclerview
                recyclerView.setAdapter(adapter);

                recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        recyclerView.removeOnLayoutChangeListener(this);
                        mProgressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(deliveryActivity.this,mapActivity.class));
    }
}
