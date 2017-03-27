package org.gpsalarm;


import android.location.Location;

import java.io.Serializable;

class LocationData extends Location implements Serializable {

    private String name = "Location";

    public LocationData(String provider) {
        super(provider);
        setLongitude(100);
        setLatitude(100);
    }

    public LocationData() {
        this(null);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReal() {
        return getLongitude() <= 90.0 && getLatitude() <= 90.0;
    }
}
