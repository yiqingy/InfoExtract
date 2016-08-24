package com.sf.mlp.ie.ds;

/**
 * Created by Joms on 4/10/2016.
 */

public class USCity {
    private final String city;
    private final String county;
    private final String state;

    public USCity(String city, String county, String state) {
        this.city = city.toUpperCase();
        this.county = county.toUpperCase();
        this.state = state.toUpperCase();
    }

    public USCity(String city, String state) {
        this.city = city.toUpperCase();
        this.county = null;
        this.state = state.toUpperCase();
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

}
