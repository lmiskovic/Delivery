package com.example.luka.delivery;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.itemTouchHelper.OnStartDragListener;
import com.example.luka.delivery.itemTouchHelper.itemTouchCallback;
import com.example.luka.delivery.network.DeliveryGetter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
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
    ArrayList<LatLng> deliveriesLatLng;
    private ItemTouchHelper mItemTouchHelper;
    private GoogleMap mGoogleMap;
    private int visibleRelativeLayoutHeight;
    private LatLng startingPoint;
    private LatLng endPoint;
    private PolylineOptions polyline;

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

        bottomSheetMap.setVisibility(View.VISIBLE);

        deliveriesLatLng = new ArrayList<>();
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

        mGoogleMap.setMyLocationEnabled(false);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);

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

                updatePolyline(deliveryList);

                optimizeRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        optimizeRecyclerView.removeOnLayoutChangeListener(this);
                        mProgressDialog.dismiss();
                    }
                });

            }
        });

        ViewTreeObserver vto = bottomSheetMap.getViewTreeObserver();

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                visibleRelativeLayoutHeight = visibleArrows.getHeight();
                BottomSheetBehavior behavior = BottomSheetBehavior.from((View) bottomSheetMap);
                behavior.setPeekHeight(visibleRelativeLayoutHeight);

                behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheetMap, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {

                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheetMap, float slideOffset) {

                    }
                });
            }
        });
    }

    private void updateBounds(List<Delivery> deliveryList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < deliveryList.size(); i++) {
            builder.include(deliveryList.get(i).getMapLocation().getLatLng());
        }

        LatLngBounds bounds = builder.build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private void updatePolyline(List<Delivery> deliveryList) {

        for (int i = 0; i < deliveryList.size(); i++) {
            Log.i(TAG, deliveryList.get(i).getDeliveryAddress());
            mGoogleMap.addMarker(new MarkerOptions().position(deliveryList.get(i).getMapLocation().getLatLng()));
        }

        updateBounds(deliveryList);

        startingPoint = deliveryList.get(0).getMapLocation().getLatLng();
        endPoint = deliveryList.get(deliveryList.size() - 1).getMapLocation().getLatLng();

        for (int i = 1; i < deliveryList.size() - 1; i++) {
            deliveriesLatLng.add(deliveryList.get(i).getMapLocation().getLatLng());
        }

        for (int i = 0; i < deliveryList.size() - 1; i++) {

            Log.i("line_start:", deliveryList.get(i).getDeliveryAddress());
            Log.i("line_end:", deliveryList.get(i + 1).getDeliveryAddress());

            startingPoint = deliveryList.get(i).getMapLocation().getLatLng();
            endPoint = deliveryList.get(i + 1).getMapLocation().getLatLng();

            GoogleDirection.withServerKey("AIzaSyAY5I_s7St4sbEqQsUO8ZRwCADK5Kb6pKc")
                    .from(startingPoint)
                    .to(endPoint)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                polyline = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 10, Color.rgb(2, 119, 189));
                                mGoogleMap.addPolyline(polyline);

                            } else {
                                // Do something
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                        }
                    });

        }

    }
}
