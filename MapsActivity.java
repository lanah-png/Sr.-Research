package com.example.srresearchtake4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.srresearchtake4.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

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
    private FusedLocationProviderClient client;
    SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //kml stuff
        final Resources resources = this.getResources();

        isMarker = false;
        markerDragged = false;
        //button
        genButton = (ImageButton) findViewById(R.id.genButton);//get id of genButton
        markerButton = (ImageButton) findViewById(R.id.markerButton);//get id of genButton

        //initialize fused location
        client = LocationServices.getFusedLocationProviderClient(this);



        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMarker){
                    previousMarker.remove();
                }
                Toast.makeText(getApplicationContext(), "This button works!", Toast.LENGTH_LONG).show();//display the text of button1

                previousMarker = marker;

                InputStream inputStream = resources.openRawResource(R.raw.riverside);
                coords= readKML(inputStream);

                double latitude = coords.get(1);
                double longitude = coords.get(0);

                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(latitude, longitude);
                previousMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                marker = previousMarker;
                isMarker=true;
                // Showing the current location in Google Map
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom in the Google Map
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


            }
        });
        markerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(), "This button works!", Toast.LENGTH_LONG).show();//display the text of button1

                if(!markerDragged) {
                    final LatLng acadLocation = new LatLng(39.04278015504511, -77.55114546415827);
                    draggable_marker= mMap.addMarker(new MarkerOptions().position(acadLocation).title("Draggable Marker").draggable(true));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(acadLocation));
                    markerDragged =true;
                }
                else {
                    LatLng draggedCoords = draggable_marker.getPosition();
                    markerDragged =false;
                    Toast.makeText(getApplicationContext(), draggedCoords.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private static List<Double> readKML(InputStream fileKML) {
        String column = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(fileKML));
        List<Double> longi = new ArrayList<Double>();
        List<Double> lati = new ArrayList<Double>();
        try {
            int enditall=0;
            while( (column = br.readLine()) != null&&enditall!=1) {
                int coordin = column.indexOf("<coordinates>");

                if (coordin != -1) {
                    while( !column.equals("        </coordinates>")) {
                        column = br.readLine();
                        if (!column.equals("        </coordinates>")) {
                            String tmpCoordin = column;
                            tmpCoordin = tmpCoordin.replaceAll(" ", "");
                            tmpCoordin = tmpCoordin.replaceAll("\t", "");
                            tmpCoordin = tmpCoordin.replaceAll("<coordinates>", "");
                            tmpCoordin = tmpCoordin.replaceAll("</coordinates>", "");
                            tmpCoordin = tmpCoordin.replaceAll(",0", "");
                            String[] coo = tmpCoordin.split(",");
                            double longit= Double.parseDouble(coo[0]);
                            double latit= Double.parseDouble(coo[1]);
                            longi.add(longit);
                            lati.add(latit);
                        }
                    }
                    enditall=1;
                }



            }
            br.close();
            Random rand = new Random();
            int indexRand = rand.nextInt(lati.size());
            List<Double> coords = new ArrayList<Double>();
            coords.add(longi.get(indexRand));
            coords.add(lati.get(indexRand));
            return coords;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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


        // Add a marker in Sydney and move the camera
        LatLng riverside = new LatLng(39.09170554630121, -77.49002780741466);

        mMap.addMarker(new MarkerOptions().position(riverside).title("Marker in Riverside"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(riverside));
        LatLng lands = new LatLng(39.081797503788735, -77.49575298111547);

        mMap.addMarker(new MarkerOptions().position(lands).title("Marker in Lansdowne Town Center"));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED ) {
            enableUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                //shows user dialog why permission is necessary
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_LOCATION_REQUEST_CODE);

            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_LOCATION_REQUEST_CODE);
            }
        }



    }

    @SuppressWarnings("MissingPermission")
    private void enableUserLocation() {
        mMap.setMyLocationEnabled(true);
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //When success
                if (location != null){
                    //Sync map
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            //Initialize lat lng
                            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                            //Create marker options
                            MarkerOptions options = new MarkerOptions().position(latlng).title("I am there");
                            //Zoom map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,10));
                            //add marker on map
                            googleMap.addMarker(options);
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


    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                //show dialog that permission is not granted
            }
        }
    }





}
