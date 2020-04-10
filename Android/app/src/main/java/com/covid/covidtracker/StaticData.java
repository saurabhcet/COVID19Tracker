package com.covid.covidtracker;

import java.util.ArrayList;
import java.util.HashMap;

public class StaticData {

    public static ArrayList<State> getStates() {
        ArrayList<State> states = new ArrayList<>();
        //Add states

        states.add(new State("AN", "Andaman and Nicobar Islands"));
        states.add(new State("AP", "Andhra Pradesh"));
        states.add(new State("AR", "Arunachal Pradesh"));
        states.add(new State("AS", "Assam"));
        states.add(new State("BR", "Bihar"));
        states.add(new State("CH", "Chandigarh"));
        states.add(new State("CT", "Chhattisgarh"));
        states.add(new State("DN", "Dadra and Nagar Haveli"));
        states.add(new State("DD", "Daman and Diu"));
        states.add(new State("DL", "Delhi"));
        states.add(new State("GA", "Goa"));
        states.add(new State("GJ", "Gujarat"));
        states.add(new State("HR", "Haryana"));
        states.add(new State("HP", "Himachal Pradesh"));
        states.add(new State("JK", "Jammu and Kashmir"));
        states.add(new State("JH", "Jharkhand"));
        states.add(new State("KA", "Karnataka"));
        states.add(new State("KL", "Kerala"));
        states.add(new State("LD", "Lakshadweep"));
        states.add(new State("MP", "Madhya Pradesh"));
        states.add(new State("MH", "Maharashtra"));
        states.add(new State("MN", "Manipur"));
        states.add(new State("ML", "Meghalaya"));
        states.add(new State("MZ", "Mizoram"));
        states.add(new State("NL", "Nagaland"));
        states.add(new State("OR", "Odisha"));
        states.add(new State("PY", "Puducherry"));
        states.add(new State("PB", "Punjab"));
        states.add(new State("RJ", "Rajasthan"));
        states.add(new State("SK", "Sikkim"));
        states.add(new State("TN", "Tamil Nadu"));
        states.add(new State("TG", "Telangana"));
        states.add(new State("TR", "Tripura"));
        states.add(new State("UP", "Uttar Pradesh"));
        states.add(new State("UT", "Uttarakhand"));
        states.add(new State("WB", "West Bengal"));

        return states;
    }

    public static ArrayList<CovidCategory> getCOVIDCategories() {
        ArrayList<CovidCategory> categories = new ArrayList<>();
        //Add COVID19 Categories
        categories.add(new CovidCategory("CR", "COVID Detected"));
        categories.add(new CovidCategory("CO", "COVID Suspected – Carrier"));
        categories.add(new CovidCategory("CY", "COVID Suspected - Community Transmission"));
        categories.add(new CovidCategory("CB", "COVID Suspected - Indirect Transmission"));
        categories.add(new CovidCategory("CG", "Recovered"));

        return categories;
    }

    public static ArrayList<QuarantinePeriod> getQuarantinePeriods() {
        ArrayList<QuarantinePeriod> periods = new ArrayList<>();
        //Add all Quarantine Period

        periods.add(new QuarantinePeriod(7, "Quarantine suggested for 7 days"));
        periods.add(new QuarantinePeriod(14, "Quarantine suggested for 14 days"));
        periods.add(new QuarantinePeriod(28, "Quarantine suggested for 28 days"));

        return periods;
    }

    public static HashMap<String, String> getFAQ() {
        HashMap<String, String> faq = new HashMap<>();
        faq.put("<Q> How this App can help you ?", "Constant Notification of COVID Suspect near your area and to spread awareness in community");
        faq.put("<Q> Is this App access my personal data ?", "No, only location if you register in any suspected category.");
        faq.put("<Q> I am not COVID suspected person why would I use this App?", "");
        faq.put("<Q> I am a COVID suspected person, is this App going to store my personal information ?", "No, only if you register as a COVID detected your location will be shared if asked for Govt. authorities.");
        faq.put("<Q> When I am trying to register as a suspected person, why this App asking my location ?", "");
        faq.put("<Q> I am general user, why the App is asking me region ?", "We don't want to spread panic, maps data will be available to area you live in.");
        faq.put("<Q> Is my personal information displayed on the map ?", "No.");
        faq.put("<Q> What is Virtual ID ?", "VID is a 16 digit number to uniquely tag COVID suspected members.");
        faq.put("<Q> What are the meaning of various COVID suspected categories ?", "");
        faq.put("<Q> I am COVID suspected or detected person, how can i register myself ?", "In the right-bottom of App main screen you will see a floating button, click on that and register.");
        faq.put("<Q> Is my personal information(ie. contacts) be revealed through this App ?", "No");
        faq.put("<Q> Is this Govt. approved App, if not can I contribute on the development ?", "No, it is a community developed application just to keep you safe and to spread awareness.");
        faq.put("<Q> If I am a COVID suspected person till how many days my location will be tracked ?", "As per your advised quarantine days.");
        faq.put("<Q> Can I register multiple person in this App ?", "Yes upto 3 person max, but we would suggest you to individually to every device.");
        faq.put("<Q> Is this a commercial App ?", "No, and its completely Ads free.");

        return faq;
    }
}