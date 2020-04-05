package com.covid.covidtracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = TrackerService.class.getSimpleName();
    private GoogleMap map;
    private Location deviceLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int MY_REQUEST_CODE = 0;
                Intent intent = new Intent(MapsActivity.this, TagActivity.class);
                startActivityForResult(intent, MY_REQUEST_CODE);
                if(MY_REQUEST_CODE == -1)
                    showRegistered();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showRegistered() {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.tagView), "Registered successfully", Snackbar.LENGTH_LONG);
        // Changing message text color
        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();
    }

    private void stopLocationService() {
       // stopService(new Intent(this, TrackerService.class));
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
                        Log.i(TAG, "authenticate: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            //fetchRemoteConfig();
                            FirebaseUser user = authResult.getCurrentUser();
                            String uid = user.getUid();
                            showUsers();
                        } else {
                            Toast.makeText(MapsActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showUsers()   {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("locations");
        getDeviceLocation();
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot ss: dataSnapshot.getChildren()) {
                    String[] key = ss.getKey().split("-");
                    String covid = key[0];
                    String state = key[1];
                    String pin = key[2];
                    String title = key[3].substring(0,4) + "-XXXX-XXXX-" + key[3].substring(12,16);
                    String lat = ss.child("0").child("latitude").getValue().toString();
                    String lng = ss.child("0").child("longitude").getValue().toString();
                    double latitude = Double.parseDouble(lat);
                    double longitude = Double.parseDouble(lng);

                    BitmapDescriptor icon;
                    // switch statement for covid type
                    switch (covid) {
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
                    String snippet =  state + ": " +  pin;
                    if(deviceLocation != null) {
                        Location locationB = new Location("Location B");
                        locationB.setLatitude(latitude);
                        locationB.setLongitude(longitude);
                        float distance = deviceLocation.distanceTo(locationB);
                        if (distance < 500) {
                            Snackbar snackbar = Snackbar
                                    .make(findViewById(R.id.mapView),
                                            title + " is " + Math.round(distance) + " m away", Snackbar.LENGTH_LONG);

                            // Changing message text color
                            snackbar.setActionTextColor(Color.RED);
                            snackbar.show();
                        }
                        snippet = snippet + ", " + Math.round(distance) + " m away";
                    }

                    // Add a marker and move the camera
                    LatLng latLng = new LatLng(latitude, longitude);
                    map.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title(title)
                                    .snippet(snippet)
                                    .icon(icon));
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                }
                map.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ));
                if(deviceLocation != null) {
                    //SELF LOCATION
                    //GET NOTIFICATION
                    map.addCircle(new CircleOptions()
                            .center(new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude()))
                            .radius(10000)
                            .strokeColor(Color.rgb(93, 173, 226))
                            .fillColor(Color.rgb(174, 214, 241)));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
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