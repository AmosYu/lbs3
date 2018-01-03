package com.ctl.lbs.utils;

import java.io.Serializable;

/**
 * Created by CTL on 2017/10/24.
 */

public class LocationPoint implements Serializable {
    private double lat;
    private double lon;
    private String address;
    private String user_address;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUser_address() {
        return user_address;
    }

    public void setUser_address(String user_address) {
        this.user_address = user_address;
    }
}
