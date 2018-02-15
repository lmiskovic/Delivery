package com.example.luka.delivery;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.luka.delivery.entities.AccessToken;
import com.example.luka.delivery.entities.ApiError;
import com.example.luka.delivery.entities.Delivery;
import com.example.luka.delivery.entities.onDeliveryListener;
import com.example.luka.delivery.network.ApiService;
import com.example.luka.delivery.network.DeliveryGetter;
import com.example.luka.delivery.network.RetrofitBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class mapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "mapActivity";

    @BindView(R.id.sheetTextDeliveryAdress)
    TextView sheetTextViewDeliveryAddress;
    @BindView(R.id.sheetTextCustomerName)
    TextView sheetTextViewCustomerName;
    @BindView(R.id.sheetTextNote)
    TextView sheetTextViewNote;
    @BindView(R.id.sheetTextContactPhone)
    TextView sheetTextViewContactPhoneNumber;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.visibleRelativeLayout)
    RelativeLayout visibleRelativeLayout;
    @BindView(R.id.DrawerLayout)
    DrawerLayout drawer;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    FusedLocationProviderClient mFusedLocationClient;
    PolylineOptions polyline;
    ProgressDialog mProgressDialog;
    TokenManager tokenManager;
    ApiService service;
    Call<AccessToken> call;
    SharedPreferences sharedPreferences;
    List<PolylineOptions> polylineArray;
    ArrayList<LatLng> directionPositionList;
    private Delivery currentDelivery;
    private boolean sorted;
    private int visibleRelativeLayoutHeight;
    private int sheetHeight;
    private boolean bottomSheetCollapsed;
    private List<Delivery> deliveryList;
    private Polyline polylineFinal;
    private boolean boundsUpdated;
    private boolean markersAdded;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                mLastLocation = location;
                Log.i("mlastlocation", String.valueOf(mLastLocation.getLatitude()) + " " + String.valueOf(mLastLocation.getLongitude()));
                if (currentDelivery != null || deliveryList != null) {
                    if (!boundsUpdated) {
                        updateMapBoundsCurrent(mLastLocation, currentDelivery.getMapLocation().getLatLng());
                        boundsUpdated = true;
                    }
                    drawPolyline();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        mProgressDialog = new ProgressDialog(this);

        tokenManager = TokenManager.getInstance(sharedPreferences);

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        bottomSheetCollapsed = true;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        //Intent intent = getIntent();
        //String email = intent.getStringExtra("usernameMail");
        //Log.w(TAG, "mapActivity email: " + email.toString());

        navigationView.setNavigationItemSelectedListener(this);

        currentDelivery = getIntent().getParcelableExtra("Delivery");

        polylineArray = getIntent().getParcelableArrayListExtra("polylineArray");

        directionPositionList = new ArrayList<>();

        deliveryList = new ArrayList<>();
        deliveryList = getIntent().getParcelableArrayListExtra("orderedDeliveryList");

        boundsUpdated = false;
        //View hView = navigationView.getHeaderView(0);

        //TextView nav_user = hView.findViewById(R.id.nav_username);

        //nav_user.setText(email);

        if (currentDelivery != null) {
            populateBottomSheet(currentDelivery);
        }
    }

    private void populateBottomSheet(Delivery currentDelivery) {
        sheetTextViewDeliveryAddress.setText(currentDelivery.getDeliveryAddress());
        sheetTextViewCustomerName.setText(currentDelivery.getCustomerName());
        sheetTextViewContactPhoneNumber.setText(currentDelivery.getContactPhoneNumber());
        sheetTextViewNote.setText(currentDelivery.getNote());

        ViewTreeObserver vto = bottomSheet.getViewTreeObserver();
        bottomSheet.setVisibility(View.VISIBLE);

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                visibleRelativeLayoutHeight = visibleRelativeLayout.getHeight();
                sheetHeight = bottomSheet.getHeight();
                BottomSheetBehavior behavior = BottomSheetBehavior.from((View) bottomSheet);
                behavior.setPeekHeight(visibleRelativeLayoutHeight);

                if (bottomSheetCollapsed)
                    mGoogleMap.setPadding(0, 0, 0, visibleRelativeLayoutHeight);

                behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            bottomSheetCollapsed = false;
                            mGoogleMap.setPadding(0, 0, 0, sheetHeight);
                        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            bottomSheetCollapsed = true;
                            mGoogleMap.setPadding(0, 0, 0, visibleRelativeLayoutHeight);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (bottomSheetCollapsed) {
            mGoogleMap.setPadding(0, 0, 0, visibleRelativeLayoutHeight);
        }

        if (currentDelivery == null && deliveryList == null) {

            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading deliveries...");
            mProgressDialog.show();

            DeliveryGetter deliveryGetter = new DeliveryGetter(this);
            deliveryGetter.call(new onDeliveryListener() {
                @Override
                public void onDelivery(List<Delivery> deliveryList) {
                    addMarkers(deliveryList);
                    updateBoundsAll(deliveryList);
                    mProgressDialog.dismiss();
                }
            });

        }

        if (polylineArray != null && deliveryList != null) {

            sorted = true;

            for (int i = 0; i < polylineArray.size(); i++) {
                mGoogleMap.addPolyline(polylineArray.get(i));
            }

            //create bottomsheet with next delivery and options

            currentDelivery = deliveryList.get(0);
            populateBottomSheet(deliveryList.get(0));
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void replaceCurrentDelivery(Delivery delivery) {

    }

    @OnClick(R.id.buttonDelivered)
    void setDelivered() {
        Log.i(TAG, "clickedForDelivery " + currentDelivery.getDeliveryAddress());

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        call = service.setDelivered(currentDelivery.getId());

        Log.i(TAG, String.valueOf(call.request()));

        call.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, final Response<AccessToken> response) {
                Log.i(TAG, "onResponse " + String.valueOf(response.code()));

                if (response.code() == 204) {
                    Log.i(TAG, "error " + String.valueOf(response.code()));

                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mapActivity.this, "Status change: failed", Toast.LENGTH_LONG).show();
                        }
                    };
                    mainHandler.post(runnable);

                } else if (response.isSuccessful()) {
                    deliveryList.remove(0);
                    currentDelivery = deliveryList.get(0);

                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            updatePolylines(polylineArray);
                            populateBottomSheet(currentDelivery);
                        }
                    };
                    mainHandler.post(runnable);
                    //TODO: COLLAPSE BOTTOMSHEET
                    polylineArray.remove(0);

                } else {
                    ApiError apiError = Utils.converErrors((response.errorBody()));
                    Toast.makeText(mapActivity.this, apiError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.i(TAG, "onFailure " + "onFailure");
            }
        });

    }

    private void updatePolylines(List<PolylineOptions> polylineArray) {
        mGoogleMap.clear();
        addMarkers(deliveryList);
        for (int i = 0; i < polylineArray.size(); i++) {
            mGoogleMap.addPolyline(polylineArray.get(i));
        }
    }

    @OnClick(R.id.buttonDeclined)
    void setDeclined() {
        Log.i(TAG, "clickedForDelivery " + currentDelivery.getDeliveryAddress());
        //contact api and mark delivery as declined
    }

    @OnClick(R.id.buttonNextDay)
    void setForNextDay() {
        Log.i(TAG, "clickedForDelivery " + currentDelivery.getDeliveryAddress());
        //contact api and mark delivery as postponed
    }

    private void addMarkers(List<Delivery> deliveries) {

        for (Delivery delivery : deliveries) {
            mGoogleMap.addMarker(new MarkerOptions().position(delivery.getMapLocation().getLatLng()));
        }

        markersAdded = true;
    }

    private void drawPolyline() {
        LatLng destination;

        if (deliveryList != null) {
            destination = deliveryList.get(0).getMapLocation().getLatLng();
        } else {
            destination = currentDelivery.getMapLocation().getLatLng();
        }

        GoogleDirection.withServerKey("AIzaSyAY5I_s7St4sbEqQsUO8ZRwCADK5Kb6pKc")
                .from(Utils.toLatLng(mLastLocation))
                .to(destination)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {

                        if (direction.isOK()) {
                            if (polylineFinal != null) {
                                polylineFinal.remove();
                            }
                            if (!markersAdded && deliveryList != null) {
                                addMarkers(deliveryList);
                            }
                            directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                            polyline = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 10, Color.rgb(2, 119, 189));
                            polylineFinal = mGoogleMap.addPolyline(polyline);
                            polylineFinal.setPoints(directionPositionList);

                        } else {

                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                    }
                });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

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


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }


    }

    public void updateBoundsAll(List<Delivery> deliveryList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (deliveryList.size() > 0) {
            for (int i = 0; i < deliveryList.size(); i++) {
                builder.include(deliveryList.get(i).getMapLocation().getLatLng());
            }
        } else if (mLastLocation != null) {
            builder.include(Utils.toLatLng(mLastLocation));
        } else {
            builder.include(new LatLng(44.4287103, 14.1629229));
        }

        LatLngBounds bounds = builder.build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    public void updateMapBoundsCurrent(Location mLastLocation, LatLng currentDeliveryLatLng) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(Utils.toLatLng(mLastLocation));
        builder.include(currentDeliveryLatLng);
        LatLngBounds bounds = builder.build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user asynchronously
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(mapActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.my_profile) {
            // Handle the camera action
        } else if (id == R.id.deliveries) {
            startActivity(new Intent(mapActivity.this, deliveryActivity.class));
        } else if (id == R.id.route) {
            Intent intent = new Intent(mapActivity.this, optimizeActivity.class);
            Bundle b = new Bundle();

            if (mLastLocation != null) {
                b.putDouble("lat", mLastLocation.getLatitude());
                b.putDouble("lng", mLastLocation.getLongitude());
            }

            intent.putExtras(b);

            startActivity(intent);
        } else if (id == R.id.about) {
            startActivity(new Intent(mapActivity.this, aboutActivity.class));
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.btn_logout)
    void logout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.logout_message)
                .setTitle(R.string.logout);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
                call = service.logout(tokenManager.getToken());
                call.enqueue(new Callback<AccessToken>() {
                    @Override
                    public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                        getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply();
                        startActivity(new Intent(mapActivity.this, loginActivity.class));
                    }

                    @Override
                    public void onFailure(Call<AccessToken> call, Throwable t) {

                    }
                });
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();

        mProgressDialog.dismiss();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
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
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}