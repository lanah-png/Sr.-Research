package com.example.srresearchtake4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.srresearchtake4.databinding.ActivityMapsBinding;
import com.example.srresearchtake4.directionhelpers.FetchURL;
import com.example.srresearchtake4.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
        TaskLoadedCallback,
        OnMyLocationClickListener,
        GoogleMap.OnMarkerDragListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
//        GoogleMap.OnMapLongClickListener

    public static final String TAG = "MapsActivity";
    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ImageButton genButton;
    ImageButton markerButton;
    Marker marker;
    Marker previousMarker;
    boolean isMarker;
    List<Double> coords;
    Marker draggable_marker;
    public boolean markerDragged;
    private boolean permissionDenied = false;
    private Button alertButton;
    private TextView alertView;
    Button directionsButton;
    MarkerOptions userLoc, nextLoc;
    LatLng draggedCoords;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private float GEOFENCE_RADIUS = 100;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private FusedLocationProviderClient client;
    SupportMapFragment mapFragment;
    public ArrayList<LatLng> randomCoords;
    LocationRequest locationRequest;

    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //initialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);

        //alert button
        alertButton = (Button) findViewById(R.id.alertButton);
        alertView = (TextView) findViewById(R.id.alertView);
        //directions stuff
        directionsButton = findViewById(R.id.directionsButton);


        //kml stuff
        final Resources resources = this.getResources();

        isMarker = false;
        markerDragged = false;
        //button
        genButton = (ImageButton) findViewById(R.id.genButton);//get id of genButton
        markerButton = (ImageButton) findViewById(R.id.markerButton);//get id of genButton

        //geofence setup
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        genButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (!markerDragged) {
                    Toast.makeText(getApplicationContext(), "Please drag the draggable marker to the max distance you're willing to cover while exploring!", Toast.LENGTH_LONG).show();//display the text of button1

                } else {
//                    draggedCoords= draggable_marker.getPosition();

//                    new FetchURL(MapsActivity.this).execute(getUrl(userLoc.getPosition(), draggedCoords, "walking"), "walking");
                    if (isMarker) {
                        previousMarker.remove();
                        mMap.clear();
                    }
                    if (randomCoords != null) {
                        LatLng randomCoord = generateRandomCoords(randomCoords);
                        previousMarker = mMap.addMarker(new MarkerOptions().position(randomCoord));
                        marker = previousMarker;
                        isMarker = true;
                        //ple
                        addCircle(randomCoord, GEOFENCE_RADIUS);
                        addGeofence(randomCoord, GEOFENCE_RADIUS);
                        // Showing the current location in Google Map
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(randomCoord));

                        // Zoom in the Google Map
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    }
                }
            }
        });
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "This button works!", Toast.LENGTH_LONG).show();//display the text of button1
                Toast.makeText(getApplicationContext(), "Drag the draggable marker to distance you want to explore!", Toast.LENGTH_LONG).show();
                if (!markerDragged) {
                    final LatLng acadLocation = new LatLng(39.04278015504511, -77.55114546415827);
                    nextLoc = new MarkerOptions().position(acadLocation).title("Draggable Marker").draggable(true);
                    draggable_marker = mMap.addMarker(nextLoc);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(acadLocation));
                    markerDragged = true;
                } else {
                    draggable_marker.remove();
                    final LatLng acadLocation = new LatLng(39.04278015504511, -77.55114546415827);
                    nextLoc = new MarkerOptions().position(acadLocation).title("Draggable Marker").draggable(true);
                    draggable_marker = mMap.addMarker(nextLoc);
                }
            }
        });
        directionsButton.setOnClickListener(view -> {


        });
//picture
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        registerReceiver(broadcastReceiver, new IntentFilter("updatetext"));




    }

    //coord generation
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        randomCoords = (ArrayList<LatLng>) values[0];
    }

    public LatLng generateRandomCoords(ArrayList<LatLng> coords) {
        Random rand = new Random();
        int indexRand = rand.nextInt(coords.size());
        return coords.get(indexRand);
    }

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setBuildingsEnabled(false);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        Log.d("mylog", "Added Markers");

        // Add a marker in Sydney and move the camera
//        LatLng riverside = new LatLng(39.09170554630121, -77.49002780741466);
//
//        mMap.addMarker(new MarkerOptions().position(riverside).title("Marker in Riverside"));

////        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLoc.getPosition()));
//        LatLng lands = new LatLng(39.081797503788735, -77.49575298111547);
//
//        mMap.addMarker(new MarkerOptions().position(lands).title("Marker in Lansdowne Town Center"));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //shows user dialog why permission is necessary
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
                mMap.setOnMarkerDragListener(this);

            }
        }


            mMap.setOnMarkerDragListener(this);

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
            if (mMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    private void setUserLocationMarker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (userLocationMarker == null) {
            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mochapic));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float).5,(float).5);
            userLocationMarker = mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        } else {
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            //use the previously created marker
        }

        if (userLocationAccuracyCircle == null) {
            CircleOptions circleOptions= new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 252, 186, 203));
            circleOptions.fillColor(Color.argb(32, 252, 148, 175));
            circleOptions.radius(location.getAccuracy());
            userLocationAccuracyCircle = mMap.addCircle(circleOptions);
        } else {
            userLocationAccuracyCircle.setCenter(latLng);
            userLocationAccuracyCircle.setRadius(location.getAccuracy());
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        client.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }  //request permissions

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @SuppressWarnings("MissingPermission")
    private void enableUserLocation() {
        mMap.setMyLocationEnabled(true);
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //When success
                if (location != null) {
                    //Sync map
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            //Initialize lat lng
                            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                            //Create marker options
                            userLoc = new MarkerOptions().position(latlng).title("I am there");
                            //Zoom map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14));
                            //add marker on map
                            googleMap.addMarker(userLoc);
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        draggedCoords = marker.getPosition();
        new FetchURL(MapsActivity.this).execute(getUrl(userLoc.getPosition(), draggedCoords, "walking"), "walking");

    }

//    public void onMapLongClick(@NonNull LatLng latLng) {
//        mMap.clear();
//        addMarker(latLng);
//        addCircle(latLng, GEOFENCE_RADIUS);
////      addGeofence(latLng, GEOFENCE_RADIUS);
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("gf", "onSuccess: Geofence Added...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d("gf", "onFailure: " + errorMessage);
                    }
                });
    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 99, 109, 206));
        circleOptions.fillColor(Color.argb(64, 118, 252, 138));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMap.clear();


            Toast.makeText(context, "beep", Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

}
