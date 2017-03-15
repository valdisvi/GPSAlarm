package a1stgroup.gpsalarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


class MyCustomizedAdapter extends ArrayAdapter<MarkerData> {

    public MyCustomizedAdapter(Context context, ArrayList<MarkerData> markerDataArrayList) {
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
