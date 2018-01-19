package com.example.luka.delivery.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class MapLocation implements Parcelable {
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

    private MapLocation(Parcel in) {
        latLng = (LatLng) in.readValue(LatLng.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(latLng);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MapLocation> CREATOR = new Parcelable.Creator<MapLocation>() {
        @Override
        public MapLocation createFromParcel(Parcel in) {
            return new MapLocation(in);
        }

        @Override
        public MapLocation[] newArray(int size) {
            return new MapLocation[size];
        }
    };
}