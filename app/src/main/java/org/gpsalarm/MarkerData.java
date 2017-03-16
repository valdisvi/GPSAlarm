package org.gpsalarm;


import java.io.Serializable;

class MarkerData implements Serializable {

    private String name = "Location";
    private double latitude = 100.1;
    private double longitude;

    public MarkerData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isReal() {
        return latitude < 100.0;
    }
}
