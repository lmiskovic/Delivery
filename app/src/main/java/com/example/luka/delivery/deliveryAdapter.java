package com.example.luka.delivery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.luka.delivery.entities.Delivery;
import com.google.android.gms.maps.MapView;

import java.util.HashSet;
import java.util.List;

public class deliveryAdapter extends RecyclerView.Adapter<DeliveryViewHolder> {

    private Context context;

    private List<Delivery> deliveryList;

    private HashSet<MapView> mMapViews = new HashSet<>();

    public deliveryAdapter(Context context, List<Delivery> deliveryList) {
        this.context = context;
        this.deliveryList = deliveryList;
    }

    @Override
    public int getItemViewType(int position) {

        return position;
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

        final Delivery delivery = deliveryList.get(position);
        holder.textViewDeliveryAddress.setText(delivery.getDeliveryAddress());
        holder.textViewCustomerName.setText(delivery.getCustomerName());
        holder.textViewContactPhoneNumber.setText(delivery.getContactPhoneNumber());
        holder.textViewNote.setText(delivery.getNote());
        holder.setMapLocation(delivery.getMapLocation());

        holder.view.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //call map activity with delivery object
                context.startActivity(new Intent(context, mapActivity.class)
                        .putExtra("Delivery", delivery));
            }
        });
    }

    @Override
    public int getItemCount() {
        return deliveryList.size();
    }

    private void drawPolyline() {

    }
}