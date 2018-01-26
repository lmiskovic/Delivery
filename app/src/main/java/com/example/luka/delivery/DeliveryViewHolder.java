package com.example.luka.delivery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.luka.delivery.entities.MapLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeliveryViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    @BindView(R.id.textDeliveryAdress) TextView textViewDeliveryAddress;
    @BindView(R.id.textCustomerName) TextView textViewCustomerName;
    @BindView(R.id.textNote) TextView textViewNote;
    @BindView(R.id.textContactPhone) TextView textViewContactPhoneNumber;
    @BindView(R.id.mapLite) MapView mapView;

    private Context context;
    protected GoogleMap mGoogleMap;
    protected MapLocation mMapLocation;
    public View view;

    public DeliveryViewHolder(final Context context, View itemView) {
        super(itemView);
        this.context = context;
        this.view = itemView;

        ButterKnife.bind(this, itemView);

        mapView.setClickable(false); // add custom click events

        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    public void setMapLocation(MapLocation mapLocation) {
        mMapLocation = mapLocation;
        // If the map is ready, update its content.
        if (mGoogleMap != null) {
            updateMapContents();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        MapsInitializer.initialize(context);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // If we have map data, update the map content.
        if (mMapLocation != null) {
            updateMapContents();
        }
    }

    protected void updateMapContents() {
        // Since the mapView is re-used, need to remove pre-existing mapView features.
        mGoogleMap.clear();
        // Update the mapView feature data and camera position.
        mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation.getLatLng()));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation.getLatLng(), 14f);
        mGoogleMap.moveCamera(cameraUpdate);
    }

}