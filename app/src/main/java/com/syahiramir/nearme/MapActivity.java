package com.syahiramir.nearme;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Syahir on 4/16/16.
 * A class to show maps activity
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Google Map
    private GoogleMap gMap;

    private static final int LOCATION_PERMISSION = 1;
    static final int LOCATION_SETTINGS_REQUEST = 2;

    private Location location;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermission();
        // Loading map
        initializeMap();
    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    private void initializeMap() {
        if (gMap == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        Intent intent = getIntent();
        if (intent.hasExtra("locations")) { //list of businesses (pressing floating action button)
            //move camera to current location
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            gMap.animateCamera(cameraUpdate);
            try {
                JSONArray locationsArr = new JSONArray(intent.getExtras().getString("locations"));
                for (int i = 0; i < locationsArr.length(); i++) {
                    JSONObject obj = new JSONObject(locationsArr.getString(i));
                    JSONObject business = new JSONObject(obj.getString("business"));
                    JSONArray coordinate = new JSONArray(business.getString("coordinate"));
                    // create marker
                    MarkerOptions marker = new MarkerOptions().position(new LatLng(Double.parseDouble(coordinate.get(0).toString()), Double.parseDouble(coordinate.get(1).toString()))).title(business.getString("name"));
                    // adding marker
                    gMap.addMarker(marker);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (intent.hasExtra("longitude")) { // specific business (pressing a business from the list)
            LatLng latLng = new LatLng(intent.getDoubleExtra("latitude", 0), intent.getDoubleExtra("longitude", 0));
            // create marker
            MarkerOptions marker = new MarkerOptions().position(latLng).title(intent.getStringExtra("name"));
            // adding marker
            gMap.addMarker(marker);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            gMap.animateCamera(cameraUpdate);
        }
    }

    //getting location permission
    private void getLocationPermission() {

        Location location = getLastKnownLocation();
        if (location == null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            // Setting Dialog Title
            alertDialog.setTitle("Turn on location services");

            // Setting Dialog Message
            alertDialog.setMessage("Please turn on location services.");

            // On pressing Settings button

            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, LOCATION_SETTINGS_REQUEST);
                        }
                    });

            alertDialog.show();
        } else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            // user is back from location settings - refresh the app
            finish();
            startActivity(getIntent());
        }
    }

    //getting last known location

    private Location getLastKnownLocation() {
        LocationManager lm;
        lm = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION);
            }
            Location l = lm.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}