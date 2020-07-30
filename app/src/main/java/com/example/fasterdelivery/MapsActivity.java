package com.example.fasterdelivery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fasterdelivery.Database.DatabaseHelper;
import com.example.fasterdelivery.Module.DirectionFinder;
import com.example.fasterdelivery.Module.DirectionFinderListener;
import com.example.fasterdelivery.Module.Route;
import com.example.fasterdelivery.Route.FetchURL;
import com.example.fasterdelivery.Route.TaskLoadedCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, LocationListener, DirectionFinderListener, TaskLoadedCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 100;
    private long FASTEST_INTERVAL = 100;
    private LatLng latLng;
    String address;
    String client_number;

    private Polyline currentPolyline;
    MarkerOptions marker;

    private Marker mMarker;
    public static final String PREFS_NAME = "MySettingsFile";

    Spinner sp;
    List<String> adres = new ArrayList<>();
    List<String> numer = new ArrayList<>();
    ArrayAdapter<String> adapter;
    List<Address> addressList;

    DatabaseHelper db;

    SwitchCompat aSwitch;

    String text;

    LatLng origin;
    String destination;
    List<Route> routes;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences smstext_settings = getSharedPreferences(PREFS_NAME, 0);
        text = smstext_settings.getString("Text", text);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        sp = findViewById(R.id.spinner);

        adapter = new ArrayAdapter<>(this, R.layout.custom_spinner, adres);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                Toast.makeText(MapsActivity.this, "Routing!", Toast.LENGTH_SHORT).show();

                String place = adapterView.getItemAtPosition(position).toString();

                destination = place;
                address = "" + place;

                client_number = numer.get(position);


                Geocoder geocoder = new Geocoder(getApplication());


                try {
                    addressList = geocoder.getFromLocationName(address, 150);

                    if (addressList != null) {
                        for (int i = 0; i < addressList.size(); i++) {

                            mMap.clear();

                            Address userAddress = addressList.get(i);
                            LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                            marker = new MarkerOptions().position(latLng).title(address);
                            mMap.addMarker(marker);

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        listAddresses();

        aSwitch = findViewById(R.id.follow);

    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;

            if (manager != null) {
                networkInfo = manager.getActiveNetworkInfo();
            }
            return networkInfo != null && networkInfo.isConnected();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map));

            if (!success) {
                Log.e("MapsActivity", "Style parsing failed");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Can`t find style. Error: ", e);
        }

        if (latLng != null) {

            if (mMarker != null) {
                mMarker.remove();
            }
            mMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.carmarker)));

            if (isNetworkAvailable()) {
                new FetchURL(MapsActivity.this).execute(getUrl(mMarker.getPosition(), marker.getPosition(), "driving"), "driving");

                // Distance


                origin = mMarker.getPosition();
                try {
                    new DirectionFinder(this, origin, destination).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            } else {
                Toast.makeText(this, "Network Not Available", Toast.LENGTH_SHORT).show();
            }

            if (aSwitch.isChecked()) {

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(googleMap.getCameraPosition().target)
                        .zoom(googleMap.getCameraPosition().zoom)
                        .build()));
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();

        }
    }

    public void listAddresses() {
        db = new DatabaseHelper(this);

        Cursor date = db.selectName();
        while (date.moveToNext()) {
            String name = date.getString(1);
            String number = date.getString(2);

            adres.add(name);
            numer.add(number);

        }
        sp.setAdapter(adapter);
    }

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
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onDirectionFinderStart() {
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {

            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

        }
    }


}

