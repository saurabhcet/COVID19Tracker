package com.covid.covidtracker;

public class CovidCategory {

    private String id;
    private String name;

    CovidCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //to display object as a string in spinner
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CovidCategory){
            CovidCategory c = (CovidCategory)obj;
            return c.getName() == name && c.getId() == id;
        }

        return false;
    }
}