package com.example.luka.delivery;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.MapLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;


public class deliveryAdapter extends RecyclerView.Adapter<DeliveryViewHolder> {

    //this context we will use to inflate the layout
    private Context context;

    //we are storing all the products in a list
    private List<Delivery> deliveryList;

    protected HashSet<MapView> mMapViews = new HashSet<>();
    protected ArrayList<MapLocation> mMapLocations;

    //getting the context and product list with constructor
    public deliveryAdapter(Context context, List<Delivery> deliveryList) {
        this.context = context;
        this.deliveryList = deliveryList;
    }

    public void setMapLocations(ArrayList<MapLocation> mapLocations) {
        mMapLocations = mapLocations;
    }

    @Override
    public DeliveryViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.delivery_list_item, viewGroup, false);
        DeliveryViewHolder viewHolder = new DeliveryViewHolder(viewGroup.getContext(), view);

        mMapViews.add(viewHolder.mapView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DeliveryViewHolder holder, int position) {

        Delivery delivery = deliveryList.get(position);

        holder.textViewDeliveryAddress.setText(delivery.getDeliveryAddress());
        holder.textViewCustomerName.setText(delivery.getCustomerName());
        holder.textViewContactPhoneNumber.setText(delivery.getContactPhoneNumber());
        holder.textViewNote.setText(delivery.getNote());
        holder.setMapLocation(delivery.getMapLocation());
    }

    @Override
    public int getItemCount() {
        return deliveryList.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }
}