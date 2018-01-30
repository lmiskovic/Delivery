package com.example.luka.delivery;

import com.example.luka.delivery.entities.Delivery;
import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

public class sortLatLngArray implements Comparator<Delivery> {
    LatLng currentLoc;

    public sortLatLngArray(LatLng current) {
        currentLoc = current;
    }

    @Override
    public int compare(final Delivery place1, final Delivery place2) {
        double lat1 = place1.getMapLocation().getLatLng().latitude;
        double lng1 = place1.getMapLocation().getLatLng().longitude;
        double lat2 = place2.getMapLocation().getLatLng().latitude;
        double lng2 = place2.getMapLocation().getLatLng().longitude;

        double distanceToPlace1 = distance(currentLoc.latitude, currentLoc.longitude, lat1, lng1);
        double distanceToPlace2 = distance(currentLoc.latitude, currentLoc.longitude, lat2, lng2);
        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;
        double angle = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2) +
                        Math.cos(fromLat) * Math.cos(toLat) *
                                Math.pow(Math.sin(deltaLon / 2), 2)));
        return radius * angle;
    }
}