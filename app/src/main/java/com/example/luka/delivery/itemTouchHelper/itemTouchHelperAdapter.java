package com.example.luka.delivery.itemTouchHelper;

public interface itemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
    void onDrop();
}
