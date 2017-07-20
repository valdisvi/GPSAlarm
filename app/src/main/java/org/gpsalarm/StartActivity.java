package org.gpsalarm;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/* TODO
    1) Set AlarmManager reinitialization time
    2) onCreate - initialize GPS services and perform checks
    3)
 */

public class StartActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    final String TAG = "StartActivity";
    private boolean checkedWiFi = false;
    private int statusForAlarm;

    LocationData selectedLocationData;
    ArrayList<LocationData> locationDataList = new ArrayList<>();
    InternalStorage internalStorage;

    GoogleApiClient m_googleApiClient;

    private LocationRequest mLocationRequest;

    LocationManager locationManager;
    WifiManager wifiManager;




    @Override
    public void onConnected(@Nullable Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.i(TAG, "No permissions");

            //return;
        }
        else{
            Location location = LocationServices.FusedLocationApi.getLastLocation(m_googleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(m_googleApiClient, mLocationRequest, this);
                Log.i(TAG, "Location is null.");
            }
            else {
                handleNewLocation(location);
                Log.i(TAG, "Location is handled.");
            }

        }

        Log.i(TAG, "Location services connected.");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        Log.i(TAG, "Connection to Location services failed");

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);
        Log.i(TAG, "Location changed");

    }

    private void handleNewLocation(Location location) {

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        Log.i(TAG, "Location is: "+location.toString());
    }

    // This class is used to provide alphabetic sorting for LocationData list
    class CustomAdapter extends ArrayAdapter<LocationData> {
        public CustomAdapter(Context context, ArrayList<LocationData> locationDataArrayList) {
            super(context, R.layout.row_layout, locationDataArrayList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater myInflater = LayoutInflater.from(getContext());
            // Last two arguments are significant if we inflate this into a parent.
            View theView = myInflater.inflate(R.layout.row_layout, parent, false);
            String cline = getItem(position).getName();
            TextView myTextView = (TextView) theView.findViewById(R.id.customTextView);
            myTextView.setText(cline);
            return theView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(StartActivity) started");
        super.onCreate(savedInstanceState);
        internalStorage = new InternalStorage();
        internalStorage.setContext(this);

        if(!googleServicesAvailable()){
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getApplicationContext());
            }
            builder.setTitle("Services missing")
                    .setMessage("Google Play Services missing from this device. Install it and try again!")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(22);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            System.exit(22);
        }
        checkGPS();
        checkAndConnect();

        if(getIntent().getBooleanExtra("Exit", false)) finish();

        m_googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(20 * 1000)        // 20 seconds, in milliseconds
                .setFastestInterval(10 * 1000); // 10 second, in milliseconds


        locationDataList = internalStorage.readLocationDataList();

        Log.i(TAG, "onCreate, locationDataList" + locationDataList);


            setContentView(R.layout.activity_start);
            final ArrayAdapter myAdapter = new CustomAdapter(this, locationDataList);
            ListView listView = (ListView) findViewById(R.id.listView);

            Collections.sort(locationDataList, new Comparator<LocationData>() {

                /* This comparator sorts LocationData objects alphabetically. */
                @Override
                public int compare(LocationData a1, LocationData a2) {
                    // String implements Comparable
                    return (a1.getName()).compareTo(a2.getName());
                }
            });

            listView.setAdapter(myAdapter);

            final ListView myListView2 = listView;
            myListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {        // Dobavlenij kod 21.02.2017
                @Override //NOTE: open map, which will show saved point with drawn radius
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedLocationData = (LocationData) myAdapter.getItem(i);
                    Toast.makeText(StartActivity.this, "Alarm '" + selectedLocationData.getName() + "' is set", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(StartActivity.this, MapActivity.class);
                    myIntent.putExtra(InternalStorage.SEL_LOC_DATA_KEY, selectedLocationData);
                    Log.i("StartActivity", selectedLocationData.getName() + " is selected");
                    startActivity(myIntent);
                }
            });

            myListView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override //NOTE: delete saved point, if selection is long-pressed
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(StartActivity.this); //NOTE: build confirmation AlertDialog
                    alert.setMessage("Are you sure you want to delete this?");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            locationDataList.remove(i);
                            myAdapter.notifyDataSetChanged();
                            internalStorage.writeLocationDataList(locationDataList);
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                    return true;
                }
            });


    }


    @Override //NOTE: Options menu (top-right corner of the screen)
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);         // More on this line: http://stackoverflow.com/questions/10303898/oncreateoptionsmenu-calling-super
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menuItemSettings:
                intent = new Intent(this, PreferencesActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.menuItemHelp:
                intent = new Intent(this, HelpActivity.class);
                this.startActivity(intent);
                return true;
            case R.id.menuItemExit:
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void toMap(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "onResume(StartActivity) called");
        if (!m_googleApiClient.isConnected()) {
             m_googleApiClient.connect();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "onPause(StartActivity) called");
        if (m_googleApiClient.isConnected()) {

            LocationServices.FusedLocationApi.removeLocationUpdates(m_googleApiClient, this);

            m_googleApiClient.disconnect();


        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "onStop(StartActivity) called");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopService(new Intent(this, AlarmSoundService.class));
        stopService(new Intent(this, AlarmNotificationService.class));
        Log.i(TAG, "onDestroy(StartActivity) called");
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
            Toast.makeText(this, "Can't connect to play services! Exiting...", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    public void checkGPS() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            buildAlertMessageNoGps();
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
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void enableWiFi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        Toast.makeText(getApplicationContext(), "Wi-fi connecting..", Toast.LENGTH_LONG).show();
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

    private void triggerAlarmManager(){
        //pass command to LocationManager
        
    }

    public int getStatusForAlarm(){
        return statusForAlarm;
    }
}
