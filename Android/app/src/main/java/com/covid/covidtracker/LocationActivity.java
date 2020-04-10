package com.covid.covidtracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import static com.covid.covidtracker.R.id;
import static com.covid.covidtracker.R.id.locationView;
import static com.covid.covidtracker.R.layout;
import static com.covid.covidtracker.R.string;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = TrackerService.class.getSimpleName();
    private SharedPreferences prefs;
    private Spinner stateSpinner;
    private EditText pinCodeEditText;
    private Button locationButton;
    private State selectedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_location);

        prefs = getSharedPreferences(getString(string.prefs), MODE_PRIVATE);
        stateSpinner = findViewById(id.state);
        pinCodeEditText = findViewById(id.pin_code);
        locationButton = findViewById(id.registerButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation();
            }
        });

        setData();
        final String state = prefs.getString(getString(string.v_cstate), "");
        final String pin = prefs.getString(getString(R.string.v_cpin), "");
        if(state!="") {
            stateSpinner.setSelection(Helper.getStateIndex(stateSpinner, state));
        }
        pinCodeEditText.setText(pin);
    }

    private void setLocation() {
        boolean val = Helper.PinValidation(pinCodeEditText.getText().toString());
        if(val && selectedState.getId().length() == 2 && pinCodeEditText.length() == 6) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.v_cstate), selectedState.getId());
            editor.putString(getString(R.string.v_cpin), pinCodeEditText.getText().toString());
            editor.apply();
            finish();
        }
        else {
            Snackbar.make(findViewById(locationView), getString(string.location_failed), Snackbar.LENGTH_LONG).show();
        }
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
    }
}