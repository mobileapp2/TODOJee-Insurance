package com.insurance.todojee.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.ClientMainListPojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Adapter Class
public class listClientsAdapter extends BaseAdapter implements Filterable {

    List<ClientMainListPojo> arrayList;
    List<ClientMainListPojo> mOriginalValues; // Original Values
    LayoutInflater inflater;
    Context context;
    private int selected = 0;


    public listClientsAdapter(Context context, List<ClientMainListPojo> arrayList) {
        this.arrayList = arrayList;
        inflater = LayoutInflater.from(context);
        context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView textView;
    }

    public ClientMainListPojo getSelected() {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).isChecked())
                return arrayList.get(i);
        }
        return null;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Log.i(TAG, "getView() enter");
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_listview_multiple, parent, false);
            holder.textView = (TextView) convertView.findViewById(R.id.alertTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ClientMainListPojo data = arrayList.get(position);

        holder.textView.setText(data.getFirst_name());
        holder.textView.setTypeface(null, Typeface.NORMAL);

        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selected = position;
                arrayList.get(position).setChecked(true);
                for (int i = 0; i < arrayList.size(); i++) {
                    if (i != position)
                        arrayList.get(i).setChecked(false);
                }
                notifyDataSetChanged();
            }
        });
        if (data.isChecked()) {
            holder.textView.setTypeface(null, Typeface.BOLD);
            //holder.textView.setTextColor(R.color.colorPrimary);

        }
        notifyDataSetChanged();


        return convertView;
    }


    @SuppressLint("DefaultLocale")
    @Override
    public Filter getFilter() {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                arrayList = (List<ClientMainListPojo>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<ClientMainListPojo> FilteredArrList = new ArrayList<>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        // Log.i(TAG, "Filter : " + mOriginalValues.get(i).getName() + " -> " + mOriginalValues.get(i).isSelected());
                        String data = mOriginalValues.get(i).getFirst_name();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(mOriginalValues.get(i));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
    }
}