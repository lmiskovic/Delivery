package com.example.luka.delivery.itemTouchHelper;

import android.support.v7.widget.RecyclerView;

public interface itemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);

}
