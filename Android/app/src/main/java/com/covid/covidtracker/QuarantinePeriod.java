package com.covid.covidtracker;

public class QuarantinePeriod {

    private int id;
    private String name;

    QuarantinePeriod(int id, String name) {
        this.id = id;
        this.name = name;
    }

    int getId() {
        return id;
    }

    public void setId(int id) {
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
        if(obj instanceof QuarantinePeriod){
            QuarantinePeriod c = (QuarantinePeriod)obj;
            return c.getName() == name && c.getId() == id;
        }

        return false;
    }
}