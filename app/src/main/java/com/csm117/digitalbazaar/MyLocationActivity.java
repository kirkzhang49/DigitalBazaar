package com.csm117.digitalbazaar;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

////////////////////////////////////
//Google Play Location sample code
///////////////////////////////////
//https://github.com/googlesamples/android-play-location
//referenced code from Basic Location Sample and Location Updates

public class MyLocationActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final String TAG = "Maps"; //final means constant - cannot change the value
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Tracks the status of the location updates request. Default value is true.
     */
    protected Boolean mRequestingLocationUpdates;
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    private double latitude;
    private double longitude;
    private double otherUserLatitude;
    private double otherUserLongitude;

    //---for debugging purposes---
    private static final String TAG2 = "Latitude";
    private static final String TAG3 = "Longitude";
    private String latitudeString;
    private String longitudeString;
    //-----------------------------

    private DatabaseReference LatRef;
    private DatabaseReference LongRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);

        String currentUserId = getIntent().getExtras().getString("userID");

        //moved to MainActivity so that map won't crash
//        String currentUserPathLat = "accounts/" + currentUserId + "/location/latitude";
//        FirebaseDatabase.getInstance()
//                .getReference(currentUserPathLat)
//                .setValue(0);
//        String currentUserPathLong = "accounts/" + currentUserId + "/location/longitude";
//        FirebaseDatabase.getInstance()
//                .getReference(currentUserPathLong)
//                .setValue(0);

        //get location data for other user
        String otherUserId = getIntent().getExtras().getString("otheruserID");
        String otherUserPathLat = "accounts/" + otherUserId + "/location/latitude";
        String otherUserPathLong = "accounts/" + otherUserId + "/location/longitude";
        LatRef = FirebaseDatabase.getInstance().getReference(otherUserPathLat);
        LongRef = FirebaseDatabase.getInstance().getReference(otherUserPathLong);
        // Attach a listener to read the data at our location reference
        LatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                otherUserLatitude = dataSnapshot.getValue(Double.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        LongRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                otherUserLongitude = dataSnapshot.getValue(Double.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        // Update values using data stored in the Bundle.
        mRequestingLocationUpdates = true;
        updateValuesFromBundle(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();

    }
    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            updateUI_init();
            updateFirebase();
        }
    }

    private void updateUI_init()
    {
        mMap.clear();
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        LatLng curLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(curLocation).title("Current Location"));
        LatLng otherUserLocation = new LatLng(otherUserLatitude, otherUserLongitude);
        mMap.addMarker(new MarkerOptions().position(otherUserLocation).title("Other User Location"));

        //---for debugging purposes---
        latitudeString = String.valueOf(otherUserLatitude);
        longitudeString = String.valueOf(otherUserLongitude);
        Log.d(TAG2, latitudeString);
        Log.d(TAG3, longitudeString);
        //-----------------------------

        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(20));
    }

    private void updateUI()
    {
        mMap.clear();
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        LatLng curLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(curLocation).title("Current Location"));
        LatLng otherUserLocation = new LatLng(otherUserLatitude, otherUserLongitude);
        mMap.addMarker(new MarkerOptions().position(otherUserLocation).title("Other User Location"));

        //---for debugging purposes---
        latitudeString = String.valueOf(otherUserLatitude);
        longitudeString = String.valueOf(otherUserLongitude);
        Log.d(TAG2, latitudeString);
        Log.d(TAG3, longitudeString);
        //-----------------------------
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(20));
    }

    private void updateFirebase()
    {
        latitude = mCurrentLocation.getLatitude();
        longitude = mCurrentLocation.getLongitude();
        // Create new chat thread for the two users. Store thread id in each user's account info
        String currentUserId = getIntent().getExtras().getString("userID");
        String currentUserPathLat = "accounts/" + currentUserId + "/location/latitude";
        FirebaseDatabase.getInstance()
                .getReference(currentUserPathLat)
                .setValue(latitude);
        String currentUserPathLong = "accounts/" + currentUserId + "/location/longitude";
        FirebaseDatabase.getInstance()
                .getReference(currentUserPathLong)
                .setValue(longitude);
    }



    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //////////////////////////////
        //relevant links//////////////
        //////////////////////////////
        //http://stackoverflow.com/questions/33327984/call-requires-permissions-that-may-be-rejected-by-user
        //http://stackoverflow.com/questions/32491960/android-check-permission-for-locationmanager/35756804
        //http://stackoverflow.com/questions/35003667/cannot-resolve-symbol-locationservice-when-trying-to-add-marshmallow-permissio/35003971
        //https://www.reddit.com/r/learnandroid/comments/4gqqh9/run_time_permissions_for_location/
        //need to check if user had granted location permissions
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION );
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            // This is the initial setup for the map
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        /**
         * Requests location updates from the FusedLocationApi.
         */
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
        updateFirebase();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we add a marker to our current location.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /////////////////////
        //relevant links/////
        /////////////////////
        //https://developers.google.com/maps/documentation/android-api/views#zoom
        // Add a marker in current location, move the camera, and zoom in
        LatLng curLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(curLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(20));
    }
    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }
}
