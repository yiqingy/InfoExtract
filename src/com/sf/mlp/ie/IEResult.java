package com.sf.mlp.ie;

/**
 * Created by Joms on 6/9/2016.
 */
//TODO: Handling currency exchange

public class IEResult {
    private final String name;
    private final String city;
    private final String state;

    public IEResult() {
        this.name = "";
        this.city = "";
        this.state = "";
    }

    public IEResult(String name, String city, String state) {
        this.name = name==null ? "":name.trim();
        this.city = city==null ? "":city.trim();
        this.state = state==null ? "":state.trim();
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }
}
