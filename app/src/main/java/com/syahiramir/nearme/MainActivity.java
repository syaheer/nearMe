package com.syahiramir.nearme;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syahiramir.nearme.adapter.BusinessesAdapter;
import com.syahiramir.nearme.data.FeedBusinesses;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import io.fabric.sdk.android.Fabric;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Syahir on 4/16/16.
 * An application to find nearby businesses using Yelp API v2. The app can also show mapview activity
 * and saves data to cache to be used when there is no internet connection.
 * <p/>
 * Originally made for Belly's coding challenge 2.
 */

public class MainActivity extends AppCompatActivity {

    // locations
    private static final int LOCATION_PERMISSION = 1;
    static final int LOCATION_SETTINGS_REQUEST = 2;
    private double longitude;
    private double latitude;

    // listView adapters and helpers
    private BusinessesAdapter businessesAdapter;
    private List<FeedBusinesses> feedBusinesses;
    private boolean flag_loading;
    private boolean listFinished = false;

    //offset for Yelp api
    private int offset = 0;

    //Chache classes and helpers
    private final JSONArray locationsArr = new JSONArray();
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mapButton);
        ListView businessesListView = (ListView) findViewById(R.id.businessesListView);
        feedBusinesses = new ArrayList<>();
        businessesAdapter = new BusinessesAdapter(this, feedBusinesses);
        if (businessesListView != null) {
            businessesListView.setAdapter(businessesAdapter);
        }

        getLocationPermission();

        if (isNetworkAvailable()) {

            // go to next page of results on listView scroll
            if (businessesListView != null) {
                businessesListView.setOnScrollListener(new AbsListView.OnScrollListener() {

                    public void onScrollStateChanged(AbsListView view, int scrollState) {


                    }

                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {

                        if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                            if (!flag_loading && !listFinished) {
                                flag_loading = true;
                                getBusinessesList();
                            }
                        }
                    }
                });
            }
        } else {
            if (coordinatorLayout != null) {
                Snackbar snackbar;
                snackbar = Snackbar
                        .make(coordinatorLayout, "No internet connection.", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            // getting data from cached list
            Gson gson = new Gson();
            String json = sharedpreferences.getString("businessList", null);
            Type type = new TypeToken<List<FeedBusinesses>>() {
            }.getType();
            List<FeedBusinesses> feedBusinesses2;
            feedBusinesses2 = gson.fromJson(json, type);
            businessesAdapter = new BusinessesAdapter(this, feedBusinesses2);
            if (businessesListView != null) {
                businessesListView.setAdapter(businessesAdapter);
            }
            businessesAdapter.notifyDataSetChanged();
        }

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, MapActivity.class);
                    i.putExtra("locations", locationsArr.toString());
                    startActivity(i);
                }
            });
        }
    }

    private void getBusinessesList() {
        //call Yelp api to get nearby businesses data and fill them into listView.
        YelpAPIFactory apiFactory = new YelpAPIFactory(getString(R.string.yelp_consumer_key), getString(R.string.yelp_consumer_secret), getString(R.string.yelp_token), getString(R.string.yelp_token_secret));
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();

        params.put("sort", "1");
        params.put("limit", "10");
        params.put("offset", Integer.toString(offset));

        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(latitude)
                .longitude(longitude).build();


        Call<SearchResponse> call = yelpAPI.search(coordinate, params);

        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Response<SearchResponse> response, Retrofit call) {
                SearchResponse searchResponse = response.body();
                ArrayList<Business> businesses = searchResponse.businesses();
                if (businesses.size() == 0) {
                    listFinished = true;
                }

                for (int i = 0; i < businesses.size(); i++) {

                    FeedBusinesses item = new FeedBusinesses();
                    item.setImageURL(businesses.get(i).imageUrl());
                    item.setName(businesses.get(i).name());
                    Double distanceInMile = businesses.get(i).distance() / 1609.344;
                    String distanceToShow;
                    if (distanceInMile < (Double) 0.1) {
                        Double distanceInFeet = businesses.get(i).distance() * 3.28;
                        distanceToShow = String.format(getString(R.string.feet_away), distanceInFeet);
                    } else {
                        distanceToShow = String.format(getString(R.string.miles_away), distanceInMile);
                    }
                    item.setDistance(distanceToShow);
                    item.setCategory(businesses.get(i).categories().get(0).name());
                    if (businesses.get(i).isClosed()) {
                        item.setIsClosed("CLOSED");
                    } else {
                        item.setIsClosed("OPEN");
                    }

                    item.setLatitude(businesses.get(i).location().coordinate().latitude());
                    item.setLongitude(businesses.get(i).location().coordinate().longitude());


                    try {
                        //add data to JSON
                        JSONObject locations = new JSONObject();
                        JSONObject storeObject = new JSONObject();
                        JSONArray latiLong = new JSONArray();
                        latiLong.put(businesses.get(i).location().coordinate().latitude());
                        latiLong.put(businesses.get(i).location().coordinate().longitude());
                        storeObject.put("name", businesses.get(i).name());
                        storeObject.put("coordinate", latiLong);
                        locations.put("business", storeObject);
                        locationsArr.put(locations);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    feedBusinesses.add(item);

                }
                //saving data to sharedPreferences as cache
                Gson gson = new Gson();

                // notify data changes to list adapter
                businessesAdapter.notifyDataSetChanged();

                String json = gson.toJson(feedBusinesses);
                editor = sharedpreferences.edit();
                editor.putString("businessList", json);
                editor.apply();

                offset = offset + businesses.size();
                flag_loading = false;
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("response", t.getMessage());

                if (getResources().getString(R.string.yelp_consumer_key).equals("")) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

                    // Setting Dialog Title
                    alertDialog.setTitle("API keys not found");

                    // Setting Dialog Message
                    alertDialog.setMessage("Please add your Yelp API keys and Google Maps API key to the strings.xml resource file");

                    // On pressing Settings button

                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });

                    alertDialog.show();
                }
            }
        };

        call.enqueue(callback);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    getLongitudeLatitude();
                }
            }
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

    //getting location permission
    private void getLocationPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }

        getLongitudeLatitude();
    }

    private void getLongitudeLatitude(){
        Location location = getLastKnownLocation();
        if (location == null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            // Setting Dialog Title
            alertDialog.setTitle("Turn on location services");

            // Setting Dialog Message
            alertDialog.setMessage("Please turn on location services or turn off airplane mode.");

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
            getBusinessesList();
        }
    }

    //detect if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
