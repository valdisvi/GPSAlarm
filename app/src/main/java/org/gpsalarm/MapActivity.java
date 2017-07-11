package org.gpsalarm;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener, LocationListener {
    static final int NOTIFICATION_ID = 899068621;
    static final int TARGET_ID = 0;
    static final int LOCATION_ID = 1;
    final int PERMISSION_FINE_LOCATIONS = 101;
    final int MIN_INTERVAL = 1000;
    final String TAG = "MapActivity";
    String ringtonePath;
    int maximumSpeed;
    static int interval = 0;              // interval between tracking requests
    boolean isTracking = false;    // is tracking going on
    private boolean userNotified = false; // is user notified about arrival
    private boolean checkedWiFi = false;  // is WiFi connection suggested
    // Google map elements
    GoogleMap googleMap;
    GoogleApiClient googleApiClient;
    LatLng addressGeo;
    String addressName;

    Marker marker;    // Marker of chosen or to be added location
    Circle circle;
    float alarmRadius;    //  Can be set through preferences.
    MediaPlayer mediaPlayer;
    LocationManager locationManager;
    LocationRequest locationRequest;  // variable for requesting location
    AlarmReceiver alarm;
    WifiManager wifiManager;
    InternalStorage internalStorage; //NOTE: points are saved in a file on sdcard
    ArrayList<LocationData> locationDataList;
    LocationData selectedLocationData; // location of selected target

    enum Estimate { // List of travel distance estimation values
        FAR, NEAR, DISABLED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate started");
        super.onCreate(savedInstanceState);
        setInternalStorage();

        // Read selected location from Extra of passed intent
        selectedLocationData = (LocationData) getIntent().getSerializableExtra(InternalStorage.SEL_LOC_DATA_KEY);
        if(selectedLocationData!=null)
            internalStorage.writeLocationData(selectedLocationData,TARGET_ID);

        checkGPS();
        checkAndConnect();

        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_map);
            initMap();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            setAlarmRadius(Integer.parseInt(prefs.getString("alarmRadius", "500")));
            maximumSpeed = Integer.parseInt(prefs.getString("maximumSpeed", "100"));
            ringtonePath = prefs.getString("alarmRingtone", DEFAULT_ALARM_ALERT_URI.toString());
            initSound();

            SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

                    if (key.equals("mapType")) {
                        changeMapType(prefs.getString("2", "2"));
                    }
                    if (key.equals("alarmRadius")) {
                        setAlarmRadius(Integer.parseInt(prefs.getString(key, "500")));
                        clearMarker();
                    }
                    if (key.equals("alarmRingtone")) {
                        ringtonePath = prefs.getString("alarmRingtone", DEFAULT_ALARM_ALERT_URI.toString());
                        initSound();
                    }

                }
            };
            prefs.registerOnSharedPreferenceChangeListener(prefListener);
            locationDataList = internalStorage.readLocationDataList(); // Retrieve the list from internal storage
        }

        // Google map searching by address name
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        /** TODO When place is selected from the list, the map should immediately display that location,
         * instead of having to press "Search location" button.
         * This is because places shown in autocomplete search are already valid, i.e., they are taken from Google Maps*/
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                addressGeo = place.getLatLng();
                addressName = place.getName().toString();
                Log.i("V", "longitude: " + place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Hide Start/Pause tracking button if target location is not selected
        if (selectedLocationData == null || !selectedLocationData.isReal())
            findViewById(R.id.startpause).setVisibility(View.GONE);
    }

    private void initMap() {
        MapFragment myMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        myMapFragment.getMapAsync(this);            // Previously getMap
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);      // Can return 3 different values

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to play services!", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final String TAG = "onMapReady";

        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (this.googleMap != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            changeMapType(prefs.getString("mapType", "2"));

            this.googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                }
            });

            this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() { //NOTE: Puts marker on map at held position
                @Override
                public void onMapLongClick(LatLng point) {
                    if (selectedLocationData != null) // If marker represents target location
                        return;                       // don't allow to move it
                    setMarker(point.latitude, point.longitude);
                }
            });

            this.googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) { //NOTE: Displays information about marker
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);
                    TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tv_snippet);
                    LatLng coordinates = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + String.format("%.4f", coordinates.latitude));
                    tvLng.setText("Longitude: " + String.format("%.4f", coordinates.longitude));
                    tvSnippet.setText(marker.getSnippet());
                    return v;
                }
            });

            this.googleMap.setOnInfoWindowClickListener(this);

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.googleMap.setMyLocationEnabled(true);
            googleApiClient = new GoogleApiClient.Builder(this)       // This code is for updating current location
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
            zoom(15, 90, 40);
            if (selectedLocationData != null && selectedLocationData.isReal()) {
                setMarker(selectedLocationData.getLatitude(), selectedLocationData.getLongitude());
                startLocationRequest(); // If location is passed from StartActivity, start checking locations
            }
            centerMap();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //NOTE: Checks device's API level, i.e. API21=Lollipop(5.0)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATIONS);
            }
        }
    }

    private void zoom(float zoom, float bearing, float tilt) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                        .zoom(zoom)                   // Sets the zoom
                        .bearing(bearing)                // Sets the orientation of the camera to east
                        .tilt(tilt)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void centerMap() {
        Location location = googleMap.getMyLocation();
        if (marker != null) { // Go to marker location
            LatLng myLocation = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
        } else if (location != null) { // Go to current location
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 12));
        } else { // Go to Knared, Sweden
            LatLng coordinates = new LatLng(56.54204, 13.36096);
            CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, 3);
            googleMap.moveCamera(camUpdate);
        }
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng coordinates = new LatLng(lat, lng);
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(coordinates, zoom);
        googleMap.moveCamera(camUpdate);
    }

    public void geoLocate(@SuppressWarnings("unused") View view) { //NOTE: Attempts to find location, if no suggestions from list are shown
        //It's possible to search by address or geographical coordinates
        checkGPS();
        if (addressName != null) {
            double lat = addressGeo.latitude;
            double lng = addressGeo.longitude;
            goToLocationZoom(lat, lng, 15);
            setMarker(lat, lng);
        } else {
            Toast.makeText(this, "No such location found. \nTry a different keyword.", Toast.LENGTH_LONG).show();
        }
    }

    void setMarker(double lat, double lng) {
        clearMarker();  // If marker has a reference, remove it.
        MarkerOptions options = new MarkerOptions()                 // This MarkerOptions object is needed to add a marker.
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.alarm_marker_40))      // Here it is possible to specify custom icon design.
                .position(new LatLng(lat, lng));
        marker = googleMap.addMarker(options);
        circle = drawCircle(new LatLng(lat, lng));
    }

    private Circle drawCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(alarmRadius)
                .fillColor(0x33FF0000)              // 33 for alpha (transparency)
                .strokeColor(Color.BLUE)
                .strokeWidth(3);
        return googleMap.addCircle(circleOptions);
    }

    private void clearMarker() {
        if (marker != null) {
            marker.remove(); // remove from map
            marker = null;
        }
        if (circle != null) {
            circle.remove();
            circle = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);         // More on this line: http://stackoverflow.com/questions/10303898/oncreateoptionsmenu-calling-super
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuItemSettings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            case R.id.menuItemHelp:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onInfoWindowClick(final Marker marker) { //NOTE: Allows to add name for set alarm
        View myView = (LayoutInflater.from(this)).inflate(R.layout.input_name, null);
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(myView);
        final EditText userInput = (EditText) myView.findViewById(R.id.etxtInputName);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(userInput.getWindowToken(), 0);
        alertBuilder.setCancelable(true)
                .setTitle("Save Alarm")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = userInput.getText().toString();

                        if (TextUtils.isEmpty(name) || TextUtils.getTrimmedLength(name) < 1) {
                            Toast.makeText(MapActivity.this, "Empty name not allowed. \nPlease try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (LocationData locationData : locationDataList) {
                            if (locationData.getName().equals(name)) {
                                Toast.makeText(MapActivity.this, "Duplicate name not allowed. \nPlease try again with a unique name.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        addLocationDataToList(name, marker);
                        MapActivity.this.marker.hideInfoWindow();
                    }
                });
        Dialog myDialog = alertBuilder.create();
        myDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationRequest != null)
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * Returns distance between locations in meters
     */
    double haversine(Location location1, Location location2) {
        return haversine(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude());
    }

    /**
     * Returns distance in meters between two points
     */
    double haversine(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    private void showPopup() {
        try {
            Toast.makeText(this, "You have arrived!", Toast.LENGTH_LONG).show();
            LayoutInflater inflater = (LayoutInflater) MapActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.screen_popup,
                    (ViewGroup) findViewById(R.id.popup_element));
            // Show popup only when activity thread is ready
            layout.post(new Runnable() {
                public void run() {
                    if (!isFinishing()) { // check that activity window is not closing
                        final PopupWindow popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
                        popupWindow.setOutsideTouchable(false);   // Set these twho to true, if want to be clickable outside window
                        popupWindow.setFocusable(false);
                        // TODO should check, if both handlers are necessary
                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                closePopUp(popupWindow);
                            }
                        });
                        final Button closePopUp = (Button) layout.findViewById(R.id.btn_close_popup);
                        closePopUp.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                closePopUp(popupWindow);
                            }
                        });
                    }
                }

                void closePopUp(PopupWindow popupWindow) {
                    mediaPlayer.release();
                    clearMarker();
                    popupWindow.dismiss();
                }
            });
        } catch (Exception e) {
            Log.e("showPopUp", "error:" + e.toString());
        }
    }

    public void changeMapType(String type) {
        switch (type) {
            case "1":
                googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case "2":
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "3":
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case "4":
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case "5":
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    public void setAlarmRadius(int newRadius) {
        alarmRadius = newRadius;
    }

    public void initSound() {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(ringtonePath));
    }

    public void addLocationDataToList(String name, Marker marker) {
        LocationData toBeAdded = new LocationData();
        toBeAdded.setName(name);
        toBeAdded.setLatitude(marker.getPosition().latitude);
        toBeAdded.setLongitude(marker.getPosition().longitude);
        locationDataList.add(toBeAdded);
        internalStorage.writeLocationDataList(locationDataList);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                        googleApiClient = new GoogleApiClient.Builder(this)       // This code is for updating current location
                                .addApi(LocationServices.API)
                                .addConnectionCallbacks(this)
                                .addOnConnectionFailedListener(this)
                                .build();
                        googleApiClient.connect();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "this app requires location permissions to be granted", Toast.LENGTH_LONG).show();
                    ActivityCompat.finishAffinity(MapActivity.this);
                    System.exit(0);
                }
                break;
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?\n" + "\"If no, programm will be closed.\"")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        ActivityCompat.finishAffinity(MapActivity.this);
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoWifi() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Wi-Fi seems to be disabled, do you want to enable it?\n" + "\"If wi-fi not available, please connect via mobile data\"")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        enableWiFi();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void startPauseTrack(@SuppressWarnings("unused") View view) {
        // Reinitialize alarm time
        alarm = new AlarmReceiver();
        if (isTracking) {
            stopLocationRequest();
            Toast.makeText(MapActivity.this, "Tracking paused.", Toast.LENGTH_SHORT).show();
            Log.i("startPauseTrack", "paused");
        } else {
            interval = MIN_INTERVAL; // reset to smallest interval, to start with precise coordinate calculation
            startLocationRequest();
            Toast.makeText(MapActivity.this, "Tracking restored.", Toast.LENGTH_SHORT).show();
            Log.i("startPauseTrack", "started");
        }
    }

    void startLocationRequest() { //NOTE: Start tracking location
        checkGPS();
        isTracking = true;
        userNotified = false;
        interval = MIN_INTERVAL;
        internalStorage.writeInterval(interval);
        // Manage wake up alerts
        alarm = new AlarmReceiver();
        alarm.setAlarm(this, interval);
        renewLocationRequest();
        addNotificationIcon();
        // Hide search options
        findViewById(R.id.button).setVisibility(View.GONE);
        findViewById(R.id.place_autocomplete_fragment).setVisibility(View.GONE);
        // Toggle tracking button view
        Button button = (Button) findViewById(R.id.startpause);
        button.setBackgroundColor(Color.RED);
        button.setText("Pause tracking");
        if (locationRequest != null) // TODO should check why it happens
            Log.v("startLocationRequest", "" +
                    "\ninterval:" + String.valueOf(locationRequest.getInterval()) +
                    "\nfastest interval:" + String.valueOf(locationRequest.getFastestInterval()));
    }

    void renewLocationRequest() {
        Log.v("renewLocationRequest", "started");
        setInternalStorage();
        interval = internalStorage.readInterval();
        locationRequest = new LocationRequest();
        switch (getEstimate()) {
            case DISABLED:
                locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                locationRequest.setNumUpdates(1); // Do just one update before going to sleep
                break;
            case FAR:
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest.setNumUpdates(1);
                break;
            case NEAR:
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(interval);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        //Set new schedules for alarm
        alarmRadius = ((int) (alarmRadius * 1.2) + 50); // FIXME just for testing
        maximumSpeed = ((int) (maximumSpeed * 1.2) + 10); // !!!
        if (getEstimate() != Estimate.DISABLED) { // Renew alarm schedule, if scheduler should be used
            AlarmReceiver alarmReceiver = new AlarmReceiver();
            alarmReceiver.setAlarm(this, interval);
            Log.v("renewLocationRequest", "alarm reshceduled after:" + interval);
        }
        hanldleLastLocation();
    }

    void stopLocationRequest() { //NOTE: Stop tracking location
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        locationRequest = null;
        isTracking = false;
        alarm.releaseWakeLock();
        alarm = null;
        Button button = (Button) findViewById(R.id.startpause);
        button.setBackgroundColor(Color.GREEN);
        button.setText("Start tracking");
        Log.i("stopLocationRequest", "stopped successfully");
    }

    public void enableWiFi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        Toast.makeText(getApplicationContext(), "Wi-fi connecting..", Toast.LENGTH_LONG).show();
    }

    private void checkGPS() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            buildAlertMessageNoGps();
    }

    public void checkAndConnect() {
        if (!checkedWiFi) {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            // test for connection
            if (cm != null) {
                if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())) {
                    buildAlertMessageNoWifi();
                }
            }
            checkedWiFi = true;
        }
    }

    public void addNotificationIcon() {
        Builder mBuilder = new Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_web)
                .setContentTitle("GPSAlarm")
                .setContentText("Programm Running")
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MIN);
        Intent resultIntent = new Intent(this,
                MapActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    // Invoked when location change event occurs
    public void onLocationChanged(Location location) {
        if (getEstimate() == Estimate.DISABLED) {
            locationManager.removeUpdates((android.location.LocationListener) this);
            Log.d("onLocationChanged", "Tracking disabled");
            return;
        }
        Location prevLocation = internalStorage.readLocation(LOCATION_ID);
        // Save new location into storage
        if (prevLocation == null // previous location is null
                || (haversine(prevLocation, location) > location.getAccuracy()) // there was real movement
                || (location.getAccuracy() < prevLocation.getAccuracy()))       // better accuracy was met
            internalStorage.writeLocation(location, LOCATION_ID);
        Log.v("onLocationChanged", "prevLocation:" + prevLocation + " location:" + location);
    }

    /**
     * Return enumerated value of estimated distance, based on status and expected traveling time
     */
    Estimate getEstimate() {
        if (selectedLocationData == null || !isTracking)
            return Estimate.DISABLED; // tracking disabled
        else if (interval > 120_000)  // more than 2 minutes for ongoing trackinig
            return Estimate.FAR;
        return Estimate.NEAR;         // less than 2 minutes for ongoing, or new request
    }

    void hanldleLastLocation() {
        Log.v("hanldleLastLocation", "started");
        Location target = internalStorage.readLocation(TARGET_ID);
        Location location = internalStorage.readLocation(LOCATION_ID);
        if (target == null || location == null) {
            Log.d("hanldleLastLocation", "target: " + target + " location:" + location);
            return;
        }
        double distance = 0;
        distance = haversine(location.getLatitude(), location.getLongitude(),
                target.getLatitude(), target.getLongitude());
        // Check if reached destination
        if (distance <= alarmRadius) {
            if (!userNotified) {
                //  lock is aquired in AlarmReceiver
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(new long[]{0, 300, 300, 300, 300, 600}, -1); // vibrate with pattern
                mediaPlayer.start();
                userNotified = true;
                stopLocationRequest();
                findViewById(R.id.startpause).setVisibility(View.GONE);
                showPopup();
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFICATION_ID);
            }
            Log.i("hanldleLastLocation", "destination reached");
        } else {
            // Else set interval for location, depending on distance
            interval = (int) (3600 * distance / maximumSpeed); // distance is in m, but speed in km
            if (interval < MIN_INTERVAL) interval = MIN_INTERVAL; // preserve minimal interval to 1s
            internalStorage.writeInterval(interval);
            if (getEstimate() == Estimate.NEAR) {
                Log.d("hanldleLastLocation", "wakeLock hold");
            } else {
                alarm.releaseWakeLock();
                Log.d("hanldleLastLocation", "wakeLock released");
            }
        }
        Log.d("hanldleLastLocation", "" +
                "\nmaxSpeed: " + String.valueOf(maximumSpeed) + "km/h" +
                "\nalarmRad: " + alarmRadius + "m" +
                "\ninterval: " + interval + "s" +
                "\ndistance: " + String.format("%.2f", distance) + "m" +
                "\naccuracy: " + location.getAccuracy() + "m" +
                "\nestimate: " + getEstimate());
    }

    void setInternalStorage() {
        if (internalStorage == null)
            internalStorage = new InternalStorage();
        if (internalStorage.getContext() == null)
            internalStorage.setContext(this);
    }
}
