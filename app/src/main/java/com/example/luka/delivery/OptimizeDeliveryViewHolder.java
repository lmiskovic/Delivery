package com.example.luka.delivery;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luka.delivery.itemTouchHelper.itemTouchHelperViewHolder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OptimizeDeliveryViewHolder extends RecyclerView.ViewHolder implements
        itemTouchHelperViewHolder, OnMapReadyCallback {

    public View view;
    protected GoogleMap mGoogleMap;
    @BindView(R.id.textDeliveryAdress) TextView textViewDeliveryAddress;
    @BindView(R.id.textCustomerName) TextView textViewCustomerName;
    @BindView(R.id.textNote) TextView textViewNote;
    @BindView(R.id.textContactPhone) TextView textViewContactPhoneNumber;
    @BindView(R.id.position) TextView textViewPosition;
    @BindView(R.id.imageReorder) ImageView imageReorder;
    Context context;
    public OptimizeDeliveryViewHolder(Context context, View itemView) {
        super(itemView);
        this.view = itemView;
        this.context = context;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onItemSelected() {
        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimarylighter));
    }

    @Override
    public void onItemClear() {
        textViewPosition.setText(String.valueOf(getLayoutPosition() + 1));
        itemView.setBackgroundColor(0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        MapsInitializer.initialize(context);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

    }
}