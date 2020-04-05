package com.covid.covidtracker;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class TagActivity extends AppCompatActivity {

    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1;
    private static String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SharedPreferences prefs;
    private Snackbar snackbar;
    private EditText covidCodeEditText;
    private EditText stateEditText;
    private EditText pinCodeEditText;
    private EditText virtualTokenEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        final long tmpToken = getVirtualToken();
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSuspect(tmpToken);
            }
        });
    }

    private void registerSuspect(long tmpTokenLng) {
        covidCodeEditText = findViewById(R.id.covid_code);
        stateEditText = findViewById(R.id.state);
        pinCodeEditText = findViewById(R.id.pin_code);
        virtualTokenEditText = findViewById(R.id.virtual_token);

        String tmpToken = Long.toString(tmpTokenLng);
        String editedToken = virtualTokenEditText.getText().toString();
        if(editedToken.length() == 16 && !editedToken.equals(tmpToken)) {
            tmpToken = editedToken;
        }

        // Store values.
        String token = covidCodeEditText.getText().toString() + '-' +
                stateEditText.getText().toString() + '-' +
                pinCodeEditText.getText().toString() + '-' +
                tmpToken;
        if(tmpToken.length() == 16 && covidCodeEditText.length()==2 &&
                pinCodeEditText.length() == 6 && stateEditText.length() == 2) {
            prefs = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.v_token), token);
            editor.apply();
            //CLOSE THE FORM

            String vToken = prefs.getString(getString(R.string.v_token), "");
            if (isServiceRunning(TrackerService.class)) {
                // If service already running, simply update UI.
                setTrackingStatus(R.string.tracking);
            } else if (vToken.length() > 0) {
                // Inputs have previously been stored, start validation.
                checkLocationPermission();
            } else {
                // First time running - check for inputs pre-populated from build.
            }
        }
        else {
            Snackbar.make(findViewById(R.id.tagView), getString(R.string.tag_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setTrackingStatus(int status) {
        boolean tracking = status == R.string.tracking;
        covidCodeEditText.setEnabled(!tracking);
        stateEditText.setEnabled(!tracking);
        pinCodeEditText.setEnabled(!tracking);
        registerButton.setVisibility(tracking ? View.INVISIBLE : View.VISIBLE);
        ((TextView) findViewById(R.id.title)).setText(getString(status));
    }

    /**
     * Second validation check - ensures the app has location permissions, and
     * if not, requests them, otherwise runs the next check.
     */
    private void checkLocationPermission() {
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (locationPermission != PackageManager.PERMISSION_GRANTED
                || storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST);
        } else {
            checkGpsEnabled();
        }
    }

    /**
     * Third and final validation check - ensures GPS is enabled, and if not, prompts to
     * enable it, otherwise all checks pass so start the location tracking service.
     */
    private void checkGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsError();
        } else {
            resolveGpsError();
            startLocationService();
        }
    }

    private void startLocationService() {
        // Before we start the service, confirm that we have extra power usage privileges.
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        // Register the Person
        startService(new Intent(this, TrackerService.class));
        Toast.makeText(this, "Registration is successful", Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showGpsError() {
        snackbar = Snackbar
                .make(findViewById(R.id.tagView), getString(R.string
                        .gps_required), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.enable, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    private void resolveGpsError() {
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    private static long getVirtualToken() {
        /* return a random long of 16 length */
        long first14 = (long) (Math.random() * 100000000000000L);
        long random = 5200000000000000L + first14;
        return random;
    }
}