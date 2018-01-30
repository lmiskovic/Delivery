package com.example.luka.delivery.itemTouchHelper;

import com.example.luka.delivery.entities.Delivery;

import java.util.List;

/**
 * Created by Luka on 29.1.2018..
 */

public interface OnReorderListener {
    void onListReordered(List<Delivery> deliveryList);
}
