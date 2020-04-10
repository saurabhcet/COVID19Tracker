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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.covid.covidtracker.R.id;
import static com.covid.covidtracker.R.id.tagView;
import static com.covid.covidtracker.R.layout;
import static com.covid.covidtracker.R.string;

public class TagActivity extends AppCompatActivity {

    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST = 1;
    private static int MAX_USER = 3;
    private static int PIN_LEN = 6;
    private static int VID_LEN = 16;
    private static int STATE_CODE_LEN = 2;
    private static int C_CODE_LEN = 2;
    private static String[] PERMISSIONS_REQUIRED = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SharedPreferences prefs;
    private Snackbar snackbar;
    private Spinner covidCodeSpinner;
    private Spinner stateSpinner;
    private Spinner quarantinePeriodSpinner;
    private EditText pinCodeEditText;
    private EditText virtualTokenEditText;
    private CheckBox agreementCheck;
    private Button registerButton;
    private State selectedState;
    private QuarantinePeriod selectedQuarantinePeriod;
    private CovidCategory selectedCovidCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_tag);

        final long tmpToken = getVirtualToken();
        prefs = getSharedPreferences(getString(string.prefs), MODE_PRIVATE);
        stateSpinner = findViewById(id.state);
        covidCodeSpinner = findViewById(id.covid_code);
        quarantinePeriodSpinner = findViewById(id.quarantine_period);
        pinCodeEditText = findViewById(id.pin_code);
        virtualTokenEditText = findViewById(id.virtual_token);
        agreementCheck = findViewById(id.agreement);
        registerButton = findViewById(id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSuspect(tmpToken);
            }
        });

        setData();

        final String state = prefs.getString(getString(R.string.v_state), "");
        final String pin = prefs.getString(getString(R.string.v_pin), "");
        if(state!="") {
            stateSpinner.setSelection(Helper.getStateIndex(stateSpinner, state));
        }
        pinCodeEditText.setText(pin);
    }

    private void registerSuspect(long tmpTokenLng) {
        String tmpToken = Long.toString(tmpTokenLng);
        String editedToken = virtualTokenEditText.getText().toString();
        if(editedToken.length() == VID_LEN && !editedToken.equals(tmpToken)) {
            tmpToken = editedToken;
        }

        int registeredCnt = prefs.getInt(getString(string.v_registered), 0);
        if(registeredCnt >= MAX_USER) {
            Snackbar.make(findViewById(tagView), getString(string.tag_max), Snackbar.LENGTH_LONG).show();
            return;
        }

        // Store values.
        String token = selectedCovidCategory.getId() + '-' +
                        selectedState.getId() + '-' +
                        pinCodeEditText.getText().toString() + '-' +
                        tmpToken;

        boolean val = Helper.PinValidation(pinCodeEditText.getText().toString());
        if(val && agreementCheck.isChecked() &&
            tmpToken.length() == VID_LEN &&
            selectedCovidCategory.getId().length() == C_CODE_LEN &&
            selectedState.getId().length() == STATE_CODE_LEN &&
            pinCodeEditText.length() == PIN_LEN) {

            if(prefs.getString(getString(string.v_token), "") =="") {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.v_token), token);
                editor.putString(getString(R.string.v_state), selectedState.getId());
                editor.putString(getString(R.string.v_pin), pinCodeEditText.getText().toString());
                editor.putInt(getString(string.v_registered), 1);

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, selectedQuarantinePeriod.getId());
                long expiry = cal.getTime().getTime();
                editor.putLong(getString(R.string.v_expiry), expiry);
                editor.apply();
            }
            else if(prefs.getString(getString(string.v_token), "") !="") {
                updateRegistration(getString(R.string.test_email), getString(R.string.test_password), token);
            }

            String vToken = prefs.getString(getString(string.v_token), "");
            if (isServiceRunning(TrackerService.class)) {
                // If service already running, simply update UI.
                setTrackingStatus(string.tracking);
            } else if (vToken.length() > 0) {
                // Inputs have previously been stored, start validation.
                checkLocationPermission();
            } else {
                // App is first time running
            }
        }
        else {
            Snackbar.make(findViewById(tagView), getString(string.tag_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateRegistration(String email, String password, final String newToken) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.i(TAG, "authenticate: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor editor = prefs.edit();
                            int registrationCnt = prefs.getInt(getString(string.v_registered), 1) + 1;
                            editor.putInt(getString(string.v_registered), registrationCnt);
                            editor.apply();
                            final String token = prefs.getString(getString(R.string.v_token), "");
                            final String state = prefs.getString(getString(R.string.v_state), "");
                            final String pin = prefs.getString(getString(R.string.v_pin), "");
                            final String path = getString(R.string.firebase_path) + state + "/" + pin + "/" + token;


                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference(path+ "/" + getString(R.string.firebase_path_registration));

                            Map<String, Object> registration = new HashMap<>();
                            registration.put("token", newToken);
                            registration.put("state", selectedState.getId());
                            registration.put("pin", pinCodeEditText.getText().toString());
                            registration.put("time", new Date().getTime());
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DAY_OF_MONTH, selectedQuarantinePeriod.getId());
                            long expiry = cal.getTime().getTime();
                            registration.put("expTime", expiry);

                            myRef.child(Integer.toString(registrationCnt)).setValue(registration);
                            finish();
                        } else {
                            Toast.makeText(TagActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        boolean tracking = status == string.tracking;
        covidCodeSpinner.setEnabled(!tracking);
        stateSpinner.setEnabled(!tracking);
        pinCodeEditText.setEnabled(!tracking);
        registerButton.setVisibility(tracking ? View.INVISIBLE : View.VISIBLE);
        //((TextView) findViewById(tagView)).setText(getString(status));
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
                .make(findViewById(tagView), getString(string
                        .gps_required), Snackbar.LENGTH_INDEFINITE)
                .setAction(string.enable, new View.OnClickListener() {
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

    private void setData() {
        //fill data in state spinner
        ArrayAdapter<State> adapter = new ArrayAdapter<State>(this, android.R.layout.simple_spinner_dropdown_item, StaticData.getStates());
        stateSpinner.setAdapter(adapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = (State) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //fill data in Category spinner
        ArrayAdapter<CovidCategory> category = new ArrayAdapter<CovidCategory>(this, android.R.layout.simple_spinner_dropdown_item, StaticData.getCOVIDCategories());
        covidCodeSpinner.setAdapter(category);
        covidCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCovidCategory = (CovidCategory) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //fill data in Quarantine spinner
        ArrayAdapter<QuarantinePeriod> quarantine = new ArrayAdapter<QuarantinePeriod>(this, android.R.layout.simple_spinner_dropdown_item, StaticData.getQuarantinePeriods());
        quarantinePeriodSpinner.setAdapter(quarantine);
        quarantinePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedQuarantinePeriod = (QuarantinePeriod) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}