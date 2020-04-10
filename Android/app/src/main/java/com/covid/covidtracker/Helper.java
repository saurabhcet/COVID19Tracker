package com.covid.covidtracker;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    public static boolean PinValidation(String pin) {
        if (null == pin || pin.length() == 0) {
            return false;
        }
        Pattern pinPattern = Pattern.compile("^[1-9][0-9]{5}$");
        Matcher pinMatcher = pinPattern.matcher(pin);
        return pinMatcher.matches();
    }

    public static int getStateIndex(Spinner spinner, String code) {
        int index = 0;
        for(int i= 0; i < spinner.getCount(); i++) {
            String stateCode = ((State)spinner.getItemAtPosition(i)).getId();
            if(stateCode.equals(code)) {
                index = i;
                break;
            }
        }
        return index;
    }
}