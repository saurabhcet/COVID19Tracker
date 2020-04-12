package com.covid.covidtracker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class TrackerService extends Service implements LocationListener {

    private static final String TAG = TrackerService.class.getSimpleName();
    public static final String STATUS_INTENT = "status";
    public static final String CHANNEL_ID = "1";
    private static int NOTIFICATION_ID = 1;
    private static final int CONFIG_CACHE_EXPIRY = 600;  // 10 minutes.
    private DatabaseReference dbRef;
    private FirebaseRemoteConfig remoteConfig;
    private LinkedList<Map<String, Object>> personStatuses = new LinkedList<>();
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private PowerManager.WakeLock mWakelock;
    private SharedPreferences prefs;

    public TrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        prefs = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        authenticate(getString(R.string.test_email), getString(R.string.test_password));
    }

    @Override
    public void onDestroy() {
        // Set activity title to not tracking.
        setStatusMessage(R.string.not_tracking);
        // Stop the persistent notification.
        mNotificationManager.cancel(NOTIFICATION_ID);
        // Release the wakelock
        if (mWakelock != null) {
            mWakelock.release();
        }
        super.onDestroy();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(TAG, "onStatusChanged: ");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "onProviderEnabled: ");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "onProviderDisabled: ");
    }

    private void authenticate(String email, String password) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.i(TAG, "authenticate: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            fetchRemoteConfig();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userUid = user.getUid();
                            loadPreviousStatuses(userUid);
                        } else {
                            Toast.makeText(TrackerService.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            stopSelf();
                        }
                    }
                });
    }

    private void fetchRemoteConfig() {
        remoteConfig.fetch(CONFIG_CACHE_EXPIRY)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Remote config fetched");
                        remoteConfig.fetchAndActivate();
                    }
                });
    }

    /**
     * Loads previously stored statuses from Firebase, and once retrieved,
     * start location tracking.
     */
    private void loadPreviousStatuses(String userUid) {
        final String token = prefs.getString(getString(R.string.v_token), "");
        final String state = prefs.getString(getString(R.string.v_state), "");
        final String pin = prefs.getString(getString(R.string.v_pin), "");
        final long expiry = prefs.getLong(getString(R.string.v_expiry), 0);
        final int registrationCnt = prefs.getInt(getString(R.string.v_registered), 0);

        final String path = userUid + "/" + getString(R.string.firebase_path) + state + "/" + pin + "/" + token;

        FirebaseAnalytics.getInstance(this).setUserProperty("COVIDDetection", token);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbRef = database.getReference(path + "/" + getString(R.string.firebase_path_registration));

        Map<String, Object> registration = new HashMap<>();
        registration.put("token", token);
        registration.put("state", state);
        registration.put("pin", pin);
        registration.put("time", new Date().getTime());
        registration.put("expTime", expiry);
        dbRef.child(Integer.toString(registrationCnt)).setValue(registration);

        dbRef = database.getReference(path);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot != null) {
                    for (DataSnapshot userData : snapshot.getChildren()) {
                        if(userData.getKey() == getString(R.string.firebase_path_location)) {
                            for (DataSnapshot userLocations : userData.getChildren()) {
                                personStatuses.add(Integer.parseInt(userLocations.getKey()), (Map<String, Object>) userLocations.getValue());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // TODO: Handle gracefully
            }
        });

        LocationRequest request = new LocationRequest();
        //Specify how often your app should request the device’s location//
        request.setInterval(remoteConfig.getLong("LOCATION_REQUEST_INTERVAL"));
        request.setFastestInterval(remoteConfig.getLong("LOCATION_REQUEST_INTERVAL_FASTEST"));
        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //...then request location updates//
            setStatusMessage(R.string.tracking);

            // Hold a partial wake lock to keep CPU awake when the we're tracking location.
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                       mWakelock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:MyWakelockTag");
            mWakelock.acquire();

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //Get a reference to the database, so your app can perform read and write operations//
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        onLocationChanged(location);
                    }
                }
            }, null);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: ");

        //Save the location data to the database//
        Map<String, Object> personStatus = new HashMap<>();
        personStatus.put("latitude", location.getLatitude());
        personStatus.put("longitude", location.getLongitude());
        personStatus.put("altitude", location.getAltitude());
        personStatus.put("time", new Date().getTime());

        //final long expiry = prefs.getLong(getString(R.string.v_expiry), 0);
        //personStatus.put("expTime", expiry);

        if (locationIsAtStatus(location, 1) && locationIsAtStatus(location, 0)) {
            // If the most recent two statuses are approximately at the same
            // location as the new current location, rather than adding the new
            // location, we update the latest status with the current. Two statuses
            // are kept when the locations are the same, the earlier representing
            // the time the location was arrived at, and the latest representing the
            // current time.
            personStatuses.set(0, personStatus);
            // Only need to update 0th status, so we can save bandwidth.
            dbRef.child(getString(R.string.firebase_path_location)).child("0").setValue(personStatus);
        } else {
            // Maintain a fixed number of previous statuses.
            while (personStatuses.size() >= remoteConfig.getLong("MAX_STATUSES")) {
                personStatuses.removeLast();
            }
            personStatuses.addFirst(personStatus);
            // We push the entire list at once since each key/index changes, to
            // minimize network requests.
            dbRef.child(getString(R.string.firebase_path_location)).setValue(personStatuses);
        }

        if (BuildConfig.DEBUG) {
            //logStatusToStorage(personStatus);
        }

        NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        boolean connected = info != null && info.isConnectedOrConnecting();
        setStatusMessage(connected ? R.string.tracking : R.string.not_tracking);
    }

    private void buildNotification() {
        createNotificationChannel();
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.track)
                .setContentTitle("COVID19 Notification")
                .setContentText("Tracking Enabled")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                //.setOngoing(true);

        // notificationId is a unique int for each notification that you must define
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            mNotificationManager = getSystemService(NotificationManager.class);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Sets the current status message (tracking/not tracking).
     */
    private void setStatusMessage(int stringId) {
        mNotificationBuilder.setContentText(getString(stringId));
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());

        // Also display the status message in the activity.
        // Future Release
        Intent intent = new Intent(STATUS_INTENT);
        intent.putExtra(getString(R.string.status), stringId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Determines if the current location is approximately the same as the location
     * for a particular status. Used to check if we'll add a new status, or
     * update the most recent status of we're stationary.
     */
    private boolean locationIsAtStatus(Location location, int statusIndex) {
        if (personStatuses.size() <= statusIndex) {
            return false;
        }
        Map<String, Object> status = personStatuses.get(statusIndex);
        Location locationForStatus = new Location("");
        locationForStatus.setLatitude((double) status.get("latitude"));
        locationForStatus.setLongitude((double) status.get("longitude"));
        float distance = location.distanceTo(locationForStatus);
        Log.d(TAG, String.format("Distance from status %s is %sm", statusIndex, distance));
        return distance < remoteConfig.getLong("LOCATION_MIN_DISTANCE_CHANGED");
    }
}