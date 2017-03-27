package org.gpsalarm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Prilozhenie startuet s etoj stranici, esli estj soxranennie tochki, inache perexodit v MapsActivity.
 */

public class StartActivity extends AppCompatActivity {
    static Context context;
    final static String FILENAME = "GPSAlarm";
    static MarkerData selectedMarkerData;
    static ArrayList<MarkerData> markerDataList = new ArrayList<>();


    class CustomAdapter extends ArrayAdapter<MarkerData> {
        public CustomAdapter(Context context, ArrayList<MarkerData> markerDataArrayList) {
            super(context, R.layout.row_layout, markerDataArrayList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater myInflater = LayoutInflater.from(getContext());
            View theView = myInflater.inflate(R.layout.row_layout, parent, false); // Last two arguments are significant if we inflate this into a parent.
            String cline = getItem(position).getName();
            TextView myTextView = (TextView) theView.findViewById(R.id.customTextView);
            myTextView.setText(cline);
            return theView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        try {
            markerDataList = (ArrayList<MarkerData>) InternalStorage.readObject(this, FILENAME);
        } catch (Exception e) {
            Log.e("StartActivity", "onCreate:" + e);
        }

        if (markerDataList.size() == 0) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_start);
            final ArrayAdapter myAdapter = new CustomAdapter(this, markerDataList);
            ListView listView = (ListView) findViewById(R.id.listView);

            Collections.sort(markerDataList, new Comparator<MarkerData>() {

                /* This comparator sorts MarkerData objects alphabetically. */
                @Override
                public int compare(MarkerData a1, MarkerData a2) {
                    // String implements Comparable
                    return (a1.getName()).compareTo(a2.getName());
                }
            });

            listView.setAdapter(myAdapter);

            final ListView myListView2 = listView;
            myListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {        // Dobavlenij kod 21.02,2017
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedMarkerData = (MarkerData) myAdapter.getItem(i);
                    Toast.makeText(StartActivity.this, "Alarm '" + selectedMarkerData.getName() + "' is set", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(StartActivity.this, MapsActivity.class);
                    Log.i("StartActivity", selectedMarkerData.getName() + " is selected");
                    startActivity(myIntent);
                }
            });

            myListView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(StartActivity.this);
                    alert.setMessage("Are you sure you want to delete this?");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            markerDataList.remove(i);
                            myAdapter.notifyDataSetChanged();
                            saveMarkerDataList();
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

    }


    @Override
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
                context.startActivity(intent);
                return true;
            case R.id.menuItemHelp:
                intent = new Intent(this, HelpActivity.class);
                context.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void toMap(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    // TODO should be either read/write or save/load
    static void saveMarkerDataList() {
        try {
            InternalStorage.writeObject(context, FILENAME, markerDataList);
        } catch (IOException e) {
            Toast.makeText(context, "Failed to save alarm", Toast.LENGTH_SHORT).show();
            Log.e("IOException", e.getMessage());
        }
    }

}
