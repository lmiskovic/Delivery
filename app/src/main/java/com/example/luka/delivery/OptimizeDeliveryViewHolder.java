package com.example.luka.delivery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luka.delivery.itemTouchHelper.itemTouchHelperViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.luka.delivery.R.color.colorPrimarylighter;

public class OptimizeDeliveryViewHolder extends RecyclerView.ViewHolder implements
        itemTouchHelperViewHolder {

    @BindView(R.id.textDeliveryAdress) TextView textViewDeliveryAddress;
    @BindView(R.id.textCustomerName) TextView textViewCustomerName;
    @BindView(R.id.textNote) TextView textViewNote;
    @BindView(R.id.textContactPhone) TextView textViewContactPhoneNumber;
    @BindView(R.id.position) TextView textViewPosition;
    @BindView(R.id.imageReorder) ImageView imageReorder;

    public View view;
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
        itemView.setBackgroundColor(0);
    }
}