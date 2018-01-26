package com.example.luka.delivery;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.itemTouchHelper.OnStartDragListener;
import com.example.luka.delivery.itemTouchHelper.itemTouchHelperAdapter;

import java.util.Collections;
import java.util.List;

public class optimizeDeliveryAdapter extends RecyclerView.Adapter<OptimizeDeliveryViewHolder>
        implements itemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;
    Context context;
    private List<Delivery> deliveryList;

    public optimizeDeliveryAdapter(Context context, List<Delivery> deliveryList, OnStartDragListener dragStartListener) {
        this.deliveryList = deliveryList;
        this.context = context;
        mDragStartListener = dragStartListener;
    }

    @Override
    public OptimizeDeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.optimize_list_item, parent, false);
        OptimizeDeliveryViewHolder OptimizeDeliveryViewHolder = new OptimizeDeliveryViewHolder(context, view);
        return OptimizeDeliveryViewHolder;
    }

    @Override
    public void onBindViewHolder(final OptimizeDeliveryViewHolder holder, int position) {
        holder.textViewDeliveryAddress.setText(deliveryList.get(position).getDeliveryAddress());
        holder.textViewCustomerName.setText(deliveryList.get(position).getCustomerName());
        holder.textViewContactPhoneNumber.setText(deliveryList.get(position).getContactPhoneNumber());
        holder.textViewNote.setText(deliveryList.get(position).getNote());
        holder.textViewPosition.setText(String.valueOf(position + 1));
        holder.imageReorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        deliveryList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onDrop() {
        notifyDataSetChanged();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Delivery prev = deliveryList.remove(fromPosition);
        deliveryList.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
        Collections.swap(deliveryList, fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        return deliveryList.size();
    }

}