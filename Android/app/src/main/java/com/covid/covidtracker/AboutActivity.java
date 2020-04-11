package com.covid.covidtracker;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getAboutView());
        // Back button
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private View getAboutView() {
        return new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.covid_bg)
                .setDescription("COVID19 Tracker")
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Connect with us")
                .addFacebook("Covid19-Tracker-108720140799062")
                .addTwitter("saurabhcetb")
                .addGitHub("saurabhcet/COVID19Tracker")
                .addPlayStore("com.covid.covidtracker")
                .addItem(createCopyright())
                .create();
    }

    private Element createCopyright() {
        Element copyright = new Element();
        copyright.setTitle(String.format("Copyright %d ", Calendar.getInstance().get(Calendar.YEAR)));
        copyright.setIconDrawable(R.drawable.covid_bg);
        copyright.setGravity(Gravity.CENTER);
        return copyright;
    }
}