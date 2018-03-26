package com.example.luka.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.itemTouchHelper.OnReorderListener;
import com.example.luka.delivery.itemTouchHelper.OnStartDragListener;
import com.example.luka.delivery.itemTouchHelper.itemTouchCallback;
import com.example.luka.delivery.network.DeliveryGetter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class optimizeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "optimizeActivity";

    @BindView(R.id.optimizeRecyclerView)
    RecyclerView optimizeRecyclerView;
    @BindView(R.id.bottom_sheet_map)
    LinearLayout bottomSheetMap;
    @BindView(R.id.visibleArrows)
    RelativeLayout visibleArrows;
    @BindView(R.id.optimizeActivityLayout)
    ConstraintLayout optimizeActivityLayout;
    @BindView(R.id.btnViewAll)
    Button btnViewAll;
    @BindView(R.id.arrow1)
    ImageView arrow1;
    @BindView(R.id.arrow2)
    ImageView arrow2;
    ProgressDialog mProgressDialog;
    SupportMapFragment mapFrag;
    ArrayList<LatLng> deliveriesLatLng;
    Location mLastLocation;
    List<PolylineOptions> polylineArray;
    List<Delivery> mDeliveryList;
    private ItemTouchHelper mItemTouchHelper;
    private GoogleMap mGoogleMap;
    private int visibleRelativeLayoutHeight;
    private LatLng startingPoint;
    private LatLng endPoint;
    private PolylineOptions polyline;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean sorted;

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

        Bundle bundle = getIntent().getExtras();

        double lat = bundle.getDouble("lat");
        double lng = bundle.getDouble("lng");

        mLastLocation = new Location("");

        mLastLocation.setLatitude(lat);
        mLastLocation.setLongitude(lng);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDeliveryList = new ArrayList<>();

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
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            Log.i("mLastLocation", String.valueOf(mLastLocation.getLatitude()) + " " + String.valueOf(mLastLocation.getLongitude()));
                        }
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

        mGoogleMap.setMyLocationEnabled(false);
        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

        final DeliveryGetter deliveryGetter = new DeliveryGetter(this);
        deliveryGetter.call(new onDeliveryListener() {

            @Override
            public void onDelivery(final List<Delivery> deliveryList) {

                saveDeliveryList(deliveryList);

                sorted = false;

                btnViewAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateBounds(deliveryList);
                    }
                });

                optimizeRecyclerView.setHasFixedSize(true);
                optimizeRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                //creating recyclerview adapter
                optimizeDeliveryAdapter adapter = new optimizeDeliveryAdapter(getApplicationContext(), new OnReorderListener() {
                    @Override
                    public void onListReordered(List<Delivery> deliveryList) {
                        updatePolyline(deliveryList);
                    }
                }, deliveryList, new OnStartDragListener() {

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

                arrow1.setBackgroundResource(R.drawable.ic_arrow_up);
                arrow2.setBackgroundResource(R.drawable.ic_arrow_up);

                visibleRelativeLayoutHeight = visibleArrows.getHeight();
                BottomSheetBehavior behavior = BottomSheetBehavior.from((View) bottomSheetMap);
                behavior.setPeekHeight(visibleRelativeLayoutHeight);

                ViewGroup.MarginLayoutParams marginLayoutParams =
                        (ViewGroup.MarginLayoutParams) optimizeRecyclerView.getLayoutParams();
                marginLayoutParams.setMargins(0, 10, 0, visibleRelativeLayoutHeight);

                //optimizeRecyclerView.setLayoutParams(marginLayoutParams);

                behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheetMap, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            arrow1.setBackgroundResource(R.drawable.ic_arrow_down);
                            arrow2.setBackgroundResource(R.drawable.ic_arrow_down);
                        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            arrow1.setBackgroundResource(R.drawable.ic_arrow_up);
                            arrow2.setBackgroundResource(R.drawable.ic_arrow_up);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheetMap, float slideOffset) {

                    }
                });
            }
        });
    }

    private void saveDeliveryList(List<Delivery> deliveryList) {
        mDeliveryList = deliveryList;
    }

    void updateBounds(List<Delivery> deliveryList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (int i = 0; i < deliveryList.size(); i++) {
            builder.include(deliveryList.get(i).getMapLocation().getLatLng());
        }

        LatLngBounds bounds = builder.build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    public void updatePolyline(List<Delivery> deliveryList) {
        polylineArray = new ArrayList<>();
        deliveriesLatLng.clear();
        mGoogleMap.clear();

        if (sorted == false) {
            Collections.sort(deliveryList, new sortLatLngArray(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            sorted = true;
        }


        for (int i = 0; i < deliveryList.size(); i++) {
            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(deliveryList.get(i).getMapLocation().getLatLng())
                    .title(String.valueOf(i + 1) + ". " + String.valueOf(deliveryList.get(i).getDeliveryAddress())));
            marker.showInfoWindow();
        }

        updateBounds(deliveryList);

        startingPoint = deliveryList.get(0).getMapLocation().getLatLng();
        Log.i("startingPoint", deliveryList.get(0).getDeliveryAddress());

        endPoint = deliveryList.get(deliveryList.size() - 1).getMapLocation().getLatLng();
        Log.i("endPoint", deliveryList.get(deliveryList.size() - 1).getDeliveryAddress());

        for (int i = 1; i < deliveryList.size() - 1; i++) {
            deliveriesLatLng.add(deliveryList.get(i).getMapLocation().getLatLng());
            Log.i("waypointAdded", deliveryList.get(i).getDeliveryAddress());
        }

        if (polylineArray != null) {
            polylineArray.clear();
        }

        for (int i = 0; i < deliveryList.size() - 1; i++) {
            LatLng startOfPolyline = deliveryList.get(i).getMapLocation().getLatLng();
            LatLng endOfPolyline = deliveryList.get(i + 1).getMapLocation().getLatLng();

            final int finalI = i;
            GoogleDirection.withServerKey("AIzaSyAY5I_s7St4sbEqQsUO8ZRwCADK5Kb6pKc")
                    .from(startOfPolyline)
                    .to(endOfPolyline)
                    .optimizeWaypoints(true)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                ArrayList<LatLng> directionPositionList = direction.getRouteList()
                                        .get(0).getLegList().get(0).getDirectionPoint();
                                polyline = DirectionConverter.createPolyline(getApplicationContext(),
                                        directionPositionList, 10,
                                        Color.rgb(2 + (finalI * 30), 119 + (finalI * 20), 255 - (finalI * 30)));
                                mGoogleMap.addPolyline(polyline);
                                polylineArray.add(polyline);
                                Log.i("polyline", "added");

                            } else {

                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                        }
                    });
        }
    }

    @OnClick(R.id.btn_proceed_to_maps)
    void startGoogleMapsNavigation(){
        String srcAdd = "&origin=" + startingPoint.latitude + "," + startingPoint.longitude;
        String desAdd = "&destination=" + endPoint.latitude + "," + endPoint.longitude;
        String wayPoints = "";

        for (int i = 0; i < deliveriesLatLng.size(); i++) {
            Log.i("deliveriesLatLng", String.valueOf(deliveriesLatLng.get(i).latitude + " " + String.valueOf(deliveriesLatLng.get(i).longitude)));
        }

        for (int i = 0; i < deliveriesLatLng.size(); i++) {
            Log.i("size", String.valueOf(deliveriesLatLng.size()));
            wayPoints = wayPoints + (wayPoints.equals("") ? "" : "%7C") + String.valueOf(deliveriesLatLng.get(i).latitude) + "," + String.valueOf(deliveriesLatLng.get(i).longitude);
        }
        wayPoints = "&waypoints=" + wayPoints;

        Log.i("srcanddes ", srcAdd + " " + desAdd + " ");
        Log.i("waypoints ", wayPoints);

        String link="https://www.google.com/maps/dir/?api=1&travelmode=driving"+srcAdd+desAdd+wayPoints;
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @OnClick(R.id.btn_proceed)
    void startNavigationInApp() {
        Intent intent;
        intent = new Intent(optimizeActivity.this, mapActivity.class);
        intent.putParcelableArrayListExtra("polylineArray", (ArrayList<? extends Parcelable>) polylineArray);
        intent.putParcelableArrayListExtra("orderedDeliveryList", (ArrayList<? extends Parcelable>) mDeliveryList);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
