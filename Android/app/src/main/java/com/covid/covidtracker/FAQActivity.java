package com.covid.covidtracker;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getFaqView());
        // Back button
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private View getFaqView() {
        HashMap<String, String> faqs = StaticData.getFAQ();
        AboutPage faq = new AboutPage(this)
                .isRTL(false)
                .setDescription("FAQ");
        for(Map.Entry<String, String> map: faqs.entrySet()) {
            //faq.addItem(new Element().setTitle(map.getKey()).setValue(map.getValue()));
            faq.addGroup(map.getKey());
            faq.addItem(new Element().setTitle(map.getValue()));
        }
        return faq.create();
    }
}