package com.example.luka.delivery;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.itemTouchHelper.OnStartDragListener;
import com.example.luka.delivery.itemTouchHelper.itemTouchCallback;
import com.example.luka.delivery.network.DeliveryGetter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class optimizeActivity extends AppCompatActivity {

    @BindView(R.id.optimizeRecyclerView) RecyclerView optimizeRecyclerView;
    private static final String TAG = "optimizeActivity";
    ProgressDialog mProgressDialog;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_optimize);
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

                optimizeRecyclerView.setHasFixedSize(true);
                optimizeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                //creating recyclerview adapter
                optimizeDeliveryAdapter adapter = new optimizeDeliveryAdapter(getApplicationContext(), deliveryList, new OnStartDragListener() {

                    @Override
                    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                        mItemTouchHelper.startDrag(viewHolder);
                    }
                });
                //setting adapter to recyclerview
                optimizeRecyclerView.setAdapter(adapter);

                ItemTouchHelper.Callback callback = new itemTouchCallback(adapter);
                mItemTouchHelper = new ItemTouchHelper(callback);
                mItemTouchHelper.attachToRecyclerView(optimizeRecyclerView);

                optimizeRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        optimizeRecyclerView.removeOnLayoutChangeListener(this);
                        mProgressDialog.dismiss();
                    }
                });
            }
        });

    }

}
