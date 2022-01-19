package com.example.mapproj;

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

import com.example.mapproj.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    Marker marker;
    Marker previousMarker;
    List<Double> coords;
    private boolean permissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //kml stuff
        final Resources resources = this.getResources();

        //button
        genButton = (ImageButton) findViewById(R.id.genButton);//get id of genButton

        genButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "This button works!", Toast.LENGTH_LONG).show();//display the text of button1

                previousMarker = marker;

                InputStream inputStream = resources.openRawResource(R.raw.directions);
                coords= readKML(inputStream);

                double latitude = coords.get(1);
                double longitude = coords.get(0);

                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(latitude, longitude);
                previousMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                marker = previousMarker;
                // Showing the current location in Google Map
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom in the Google Map
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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
                        column= br.readLine();
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
        LatLng academies = new LatLng(39.042733, -77.549793);

        mMap.addMarker(new MarkerOptions().position(academies).title("Marker in Academies"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(academies));

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