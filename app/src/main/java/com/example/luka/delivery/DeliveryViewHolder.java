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


public class DeliveryViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    TextView textViewDeliveryAddress, textViewCustomerName, textViewNote, textViewContactPhoneNumber;
    public MapView mapView;
    private Context context;
    protected GoogleMap mGoogleMap;
    protected MapLocation mMapLocation;

    public DeliveryViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        textViewDeliveryAddress = itemView.findViewById(R.id.textDeliveryAdress);
        textViewCustomerName = itemView.findViewById(R.id.textCustomerName);
        textViewContactPhoneNumber = itemView.findViewById(R.id.textContactPhone);
        textViewNote = itemView.findViewById(R.id.textNote);
        mapView = itemView.findViewById(R.id.mapLite);

        mapView.onCreate(null);
        mapView.getMapAsync(this);

    }

    public void setMapLocation(MapLocation mapLocation) {
        mMapLocation = mapLocation;
        Log.i("setMapLocation", mapLocation.getLatLng().latitude + " " + mapLocation.getLatLng().longitude);
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