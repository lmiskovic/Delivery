package com.example.luka.delivery;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.itemTouchHelper.OnStartDragListener;
import com.example.luka.delivery.itemTouchHelper.itemTouchCallback;
import com.example.luka.delivery.network.DeliveryGetter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class optimizeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "optimizeActivity";

    @BindView(R.id.optimizeRecyclerView)
    RecyclerView optimizeRecyclerView;
    @BindView(R.id.bottom_sheet_map)
    LinearLayout bottomSheetMap;
    @BindView(R.id.visibleArrows)
    RelativeLayout visibleArrows;
    ProgressDialog mProgressDialog;
    SupportMapFragment mapFrag;
    private ItemTouchHelper mItemTouchHelper;
    private GoogleMap mGoogleMap;
    private int visibleRelativeLayoutHeight;
    private int sheetHeight;
    private boolean bottomSheetCollapsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_optimize);
        ButterKnife.bind(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading deliveries...");
        mProgressDialog.show();

        mapFrag = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.mapPreview);
        mapFrag.getMapAsync(this);

        bottomSheetCollapsed = true;
        bottomSheetMap.setVisibility(View.VISIBLE);

        DeliveryGetter deliveryGetter = new DeliveryGetter(this);
        deliveryGetter.call(new onDeliveryListener() {
            @Override
            public void onDelivery(List<Delivery> deliveryList) {

                optimizeRecyclerView.setHasFixedSize(true);
                optimizeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                //creating recyclerview adapter
                optimizeDeliveryAdapter adapter = new optimizeDeliveryAdapter(getApplicationContext(), deliveryList, new OnStartDragListener() {

                    @Override
                    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                        mItemTouchHelper.startDrag(viewHolder);
                    }
                });
                //setting adapter to recyclerview
                optimizeRecyclerView.setAdapter(adapter);

                ItemTouchHelper.Callback callback = new itemTouchCallback(adapter);
                mItemTouchHelper = new ItemTouchHelper(callback);
                mItemTouchHelper.attachToRecyclerView(optimizeRecyclerView);

                optimizeRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        optimizeRecyclerView.removeOnLayoutChangeListener(this);
                        mProgressDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);

        ViewTreeObserver vto = bottomSheetMap.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                visibleRelativeLayoutHeight = visibleArrows.getHeight();
                sheetHeight = bottomSheetMap.getHeight();
                BottomSheetBehavior behavior = BottomSheetBehavior.from((View) bottomSheetMap);
                behavior.setPeekHeight(visibleRelativeLayoutHeight);

                behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheetMap, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            bottomSheetCollapsed = false;
                        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetCollapsed = true;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheetMap, float slideOffset) {

                    }
                });
            }
        });

    }
}
