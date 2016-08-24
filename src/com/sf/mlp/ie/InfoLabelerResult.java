package com.sf.mlp.ie;

import com.sf.mlp.ie.ds.Range;

/**
 * Created by Joms on 6/9/2016.
 */
public class InfoLabelerResult {
    private final Range name;
    private final Range city;
    private final Range state;

    public InfoLabelerResult(Range name, Range city, Range state) {
        this.name = name;
        this.city = city;
        this.state = state;
    }

    public Range getName() {
        return name;
    }

    public Range getCity() {
        return city;
    }

    public Range getState() {
        return state;
    }
}

