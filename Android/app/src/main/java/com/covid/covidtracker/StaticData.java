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
        faq.put("Q) How this App can help you?", "A) This App provides constant Notification of COVID suspect near to your area and at the same time spread awareness in community.");
        faq.put("Q) Will this App access my personal information?", "A) No, only location will be tracked if you register in suspected category.");
        faq.put("Q) I am not a COVID suspect, why would I use this App?", "A) You can use this App to be aware of any suspect is there near your area.");
        faq.put("Q) I am a COVID detected person, will this App going to store my personal information?", "A) No, only if you register as a COVID detected person your current location will be tracked.");
        faq.put("Q) I am general user, why this App is asking my region?", "A) We don't want to create panic, the heat-maps will be available to the area you live in.");
        faq.put("Q) Will my personal information be displayed on map?", "A) No personal information will be displayed on the map.");
        faq.put("Q) What is Virtual ID?", "A) VID is just a 16 digit number to create a unique identity.");
        faq.put("Q) What are the various COVID suspected categories ?", "A) 1) Detected 2) Suspected – Carrier 3) Suspected - Community Transmission 4) Suspected - Indirect Transmission 5) Recovered.");
        faq.put("Q) I am COVID suspected or detected person, how can I register myself?", "A) In the bottom-right of the App window a floating icon is available, click on it & register.");
        faq.put("Q) Will my Phone-book detail be revealed through this App?", "A) No Phone-book details will be revealed.");
        faq.put("Q) Is this Govt. authorised App, if not can I contribute?", "it is community developed App not Govt. approved App, just to create awareness & to keep you safe. Yes you can contribute to the App by reaching us through About us section.");
        faq.put("Q) If I am suspected person, till how many days my location be tracked?", "A) No, the location will be tracked as per advised quarantine days.");
        faq.put("Q) Can I register multiple person in this App?", "A) Yes, upto 3 person can be registered, but we would suggest you to register one person in every device.");
        faq.put("Q) Is this a commercial App?", "A) No, it is completely Ads free App.");

        return faq;
    }
}