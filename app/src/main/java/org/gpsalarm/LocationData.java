package org.gpsalarm;

import android.location.Location;

import java.io.Serializable;
// This is proxy class to allow save important fields of not serializable Location objects.
// It should be directly used only in InternalStorage class
class LocationData implements Serializable {
    private static final long serialVersionUID = 9201049974853118119L;
    final String TAG = "LocationData";
    String name = "Location";
    double longitude;
    double latitude;
    float accuracy;

    LocationData(String name) {
        this.name = name;
        longitude = 100;
        latitude = 100;
    }

    LocationData() {
        this("LocationData");
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    float getAccuracy() {
        return accuracy;
    }

    boolean isReal() {
        return longitude <= 180 && latitude <= 90 && longitude >= -180 && latitude >= -90;
    }

    void set(Location location) {
        name = "Location";
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public String toString() {
        return name + ": " + latitude + ", " + longitude;
    }

}
