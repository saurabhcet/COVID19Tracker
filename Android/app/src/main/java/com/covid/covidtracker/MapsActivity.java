package com.covid.covidtracker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int MAX_USER = 3;
    private static final int PIN_LEN = 6;
    private static final int STATE_CODE_LEN = 2;
    private GoogleMap map;
    private Location deviceLocation;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String savedToken = prefs.getString(getString(R.string.v_token), "");
                final int registeredCount = prefs.getInt(getString(R.string.v_registered), 0);
                if(savedToken.isEmpty() || registeredCount < MAX_USER) {
                    startActivity(new Intent(MapsActivity.this, TagActivity.class));
                    showRegistered("Registered successfully");
                }
                else {
                    showRegistered("Already registered");
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        prefs = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        final String state = prefs.getString(getString(R.string.v_cstate), "");
        final String pin = prefs.getString(getString(R.string.v_cpin), "");
        if(state.length() == 0 || pin.length() == 0) {
            showLocationActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

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
        map = googleMap;
        final FirebaseAuth authResult = FirebaseAuth.getInstance();
        authResult.signInWithEmailAndPassword(getString(R.string.test_email), getString(R.string.test_password))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        //Log.i(TAG, "authenticate: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            final String state = prefs.getString(getString(R.string.v_state), "");
                            final String pin = prefs.getString(getString(R.string.v_pin), "");
                            if(state.length() == STATE_CODE_LEN && pin.length() == PIN_LEN) {
                                    //showUserMarkers(state, pin);
                                    showHeatMaps(state, pin);
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu) ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_location:
                showLocationActivity();
		        return true;
            case R.id.action_faq:
                startActivity(new Intent(this, FAQActivity.class));
		        return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
		        return true;
            //case R.id.action_credit: // Future development
            //  startCreditActivity();
            //  break;
            default:
		        return super.onOptionsItemSelected(item);
        }         
    }

    private void showLocationActivity() {
        startActivity(new Intent(this, LocationActivity.class));
    }

    private void showRegistered(String msg) {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.tagView), msg, Snackbar.LENGTH_LONG);
        // Changing message text color
        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();
    }

    private void showUserMarkers(String state, String pin) {
       // final String token = prefs.getString(getString(R.string.v_token), "");
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String path = getString(R.string.firebase_path) + state + "/" + pin + "/";
        DatabaseReference myRef = database.getReference(path);
        getDeviceLocation();

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                /* Data points defined as a mixture of WeightedLocation and LatLng objects */

                for(DataSnapshot userData: dataSnapshot.getChildren()) {
                    for(DataSnapshot snapshot: userData.getChildren()){
                        String key = snapshot.getKey();
                        String registration = getString(R.string.firebase_path_registration);
                        String location = getString(R.string.firebase_path_location);
                        if(key.equals(registration)) {
                            double latitude = Double.parseDouble(userData.child(location).child("0").child("latitude").getValue().toString());
                            double longitude = Double.parseDouble(userData.child(location).child("0").child("longitude").getValue().toString());

                            int notificationCnt = 0;
                            for (DataSnapshot registrations : snapshot.getChildren()) {

                                //Discard expired users data
                                long expTime = Long.parseLong(registrations.child("expTime").getValue().toString());
                                long curDate = new Date().getTime();
                                if (expTime > curDate) {

                                    String pin = registrations.child("pin").getValue().toString();

                                    //Send notification once
                                    if(++notificationCnt ==1) {
                                        sendNotificationToUser(pin, latitude, longitude);
                                    }

                                    // Add a marker and move the camera
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    map.addMarker(
                                            new MarkerOptions()
                                                    .position(latLng)
                                                    .title(pin));
                                    //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                }
                            }
                        }
                    }
                }
                addCircleToUserLocation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void showHeatMaps(String state, String pin) {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String path = getString(R.string.firebase_path) + state + "/" + pin + "/";
        DatabaseReference myRef = database.getReference(path);
        getDeviceLocation();

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                /* Data points defined as a mixture of WeightedLocation and LatLng objects */
                //addHeatMap(dataSnapshot);
                ArrayList<LatLng> mapData = new ArrayList<>();
                for(DataSnapshot userData: dataSnapshot.getChildren()) {
                    for(DataSnapshot snapshot: userData.getChildren()){
                        String key = snapshot.getKey();
                        String registration = getString(R.string.firebase_path_registration);
                        String location = getString(R.string.firebase_path_location);
                        if(key.equals(registration)) {
                            double latitude = Double.parseDouble(userData.child(location).child("0").child("latitude").getValue().toString());
                            double longitude = Double.parseDouble(userData.child(location).child("0").child("longitude").getValue().toString());


                            // Send notification to user and add data points
                            int notificationCnt = 0;
                            for (DataSnapshot registrations : snapshot.getChildren()) {

                                //Discard expired users data
                                long expTime = Long.parseLong(registrations.child("expTime").getValue().toString());
                                long curDate = new Date().getTime();
                                if (expTime > curDate) {
                                    String pin = registrations.child("pin").getValue().toString();

                                    //Send notification once
                                    if(++notificationCnt == 1) {
                                        sendNotificationToUser(pin, latitude, longitude);
                                    }

                                    // Add a marker and move the camera
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    mapData.add(latLng);
                                }
                            }
                        }
                    }
                }
                // Create the gradient.
                final int[] colors = {
                        Color.rgb(255, 0, 0)
                };

                float[] startPoints = {
                        0.80f
                };

                Gradient gradient = new Gradient(colors, startPoints);
                // Create a heat map tile provider, passing it the latlngs of users.
                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                        .data(mapData)
                        .gradient(gradient)
                        .build();
                mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
                // Add a tile overlay to the map, using the heat map tile provider.
                TileOverlay mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                addCircleToUserLocation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private BitmapDescriptor getMarkerIcon(String code) {
        BitmapDescriptor icon;
        // switch statement for covid type
        switch (code) {
            case "CR":
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                break;
            case "CO":
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                break;
            case "CY":
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                break;
            case "CG":
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                break;
            default:
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                break;
        }
        return icon;
    }

    private void sendNotificationToUser(String title, double latitude, double longitude) {
        if(deviceLocation != null) {
            Location locationB = new Location("Location B");
            locationB.setLatitude(latitude);
            locationB.setLongitude(longitude);
            float distance = deviceLocation.distanceTo(locationB);
            if (distance < 500 && findViewById(R.id.mapView) != null) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.mapView),
                                "A suspected person from Pin Code: " + title + " is " + Math.round(distance) + " m away", Snackbar.LENGTH_LONG);

                // Changing message text color
                snackbar.setActionTextColor(Color.RED);
                snackbar.setAction("Action", null);
                snackbar.show();
            }
        }
    }

    private void addCircleToUserLocation() {
        if(deviceLocation != null) {
            //SELF LOCATION
            //GET NOTIFICATION
            map.addCircle(new CircleOptions()
                    .center(new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude()))
                    .radius(3000)
                    .strokeColor(Color.rgb(93, 173, 226))
                    .fillColor(Color.rgb(174, 214, 241)));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude()), 6));
        }
        else {
            // default location 19.10, 72.88
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(19.10, 72.88), 6));
        }
    }

    private void getDeviceLocation() {
        LocationRequest request = new LocationRequest();
        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //...then request location updates//
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    deviceLocation = locationResult.getLastLocation();
                }
            }, null);
        }
        else {
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.mapView), getString(R.string.location_suspect_notification), Snackbar.LENGTH_LONG);
            // Changing message text color
            snackbar.setActionTextColor(Color.GREEN);
            snackbar.show();
        }
    }
}