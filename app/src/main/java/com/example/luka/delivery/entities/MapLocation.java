package com.example.luka.delivery.entities;

import com.google.android.gms.maps.model.LatLng;

public class MapLocation {
    private LatLng latLng;

    public MapLocation() {}

    public MapLocation(double lat, double lng) {
        this.latLng = new LatLng(lat, lng);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
