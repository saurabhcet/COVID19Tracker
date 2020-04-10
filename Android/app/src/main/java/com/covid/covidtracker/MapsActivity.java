package com.covid.covidtracker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int MAX_USER = 3;
    private static final int PIN_LEN = 6;
    private static final int STATE_CODE_LEN = 2;
    private static final String MAP_TYPE = "HMAP";
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
                if(savedToken == "" || registeredCount < MAX_USER) {
                    Intent intent = new Intent(MapsActivity.this, TagActivity.class);
                    startActivity(intent);
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
		return super.onOptionsItemSelected(item);                
            case R.id.action_faq:
                startFaqActivity();
		return true;
            case R.id.action_about:
                startAboutActivity();
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

    private void startFaqActivity() {
       HashMap<String, String> faqs = StaticData.getFAQ();
        AboutPage faq = new AboutPage(this)
                                .isRTL(false)
                                .setDescription("FAQ");
       for(Map.Entry<String, String> map: faqs.entrySet()) {
           //faq.addItem(new Element().setTitle(map.getKey()).setValue(map.getValue()));
           faq.addGroup(map.getKey());
           faq.addItem(new Element().setTitle(map.getValue()));
        }
        View view = faq.create();
        setContentView(view);
    }

    private void startAboutActivity() {
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.ic_launcher_background)
                .setDescription("COVID19 Tracker")
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Connect with us")
                .addFacebook("facebook.com")
                .addTwitter("saurabhcetb")
                .addGitHub("saurabhcet/COVID19Tracker")
                .addPlayStore("com.covid.covidtracker")
                .addItem(createCopyright())
                .create();
        setContentView(aboutPage);
    }

    private Element createCopyright() {
        Element copyright = new Element();
        copyright.setTitle(String.format("Copyright %d ", Calendar.getInstance().get(Calendar.YEAR)));
        copyright.setIconDrawable(R.drawable.ic_launcher_background);
        copyright.setGravity(Gravity.CENTER);
        return copyright;
    }

    private void stopLocationService() {
       // stopService(new Intent(this, TrackerService.class));
    }

    private void showRegistered(String msg) {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.tagView), msg, Snackbar.LENGTH_LONG);
        // Changing message text color
        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();
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
                            final String state = prefs.getString(getString(R.string.v_state), "");
                            final String pin = prefs.getString(getString(R.string.v_pin), "");
                            if(state.length() == STATE_CODE_LEN && pin.length() == PIN_LEN) {
                                if(MAP_TYPE == "USER")
                                  showUserMarkers(state, pin);
                                else
                                  showHeatMaps(state, pin);
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Refresh map with super.onResume();
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
                //addHeatMap(dataSnapshot);
                ArrayList<LatLng> list = new ArrayList<LatLng>();
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
                                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                }
                            }
                        }
                    }
                }
                map.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ));
                addCircleToUserLocation();
            }

            @Override
            public void onCancelled(DatabaseError error) {
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
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                /* Data points defined as a mixture of WeightedLocation and LatLng objects */
                //addHeatMap(dataSnapshot);
                ArrayList<LatLng> mapData = new ArrayList<LatLng>();
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
                        0.70f
                };

                Gradient gradient = new Gradient(colors, startPoints);
                // Create a heat map tile provider, passing it the latlngs of users.
                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                        .data(mapData)
                        .gradient(gradient)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                TileOverlay mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

                map.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ));
                addCircleToUserLocation();
            }

            @Override
            public void onCancelled(DatabaseError error) {
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
            if (distance < 500) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.mapView),
                                title + " is " + Math.round(distance) + " m away", Snackbar.LENGTH_LONG);

                // Changing message text color
                snackbar.setActionTextColor(Color.RED);
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
        }
    }

    private void showUsersOld()   {
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