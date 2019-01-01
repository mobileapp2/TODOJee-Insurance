package com.insurance.todojee.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.insurance.todojee.R;

public class WeekAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] web;
    String temp = "";

    public WeekAdapter(Context c, String[] web) {
        mContext = c;
        this.web = web;
    }

    @Override
    public int getCount() {
        return web.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.item_week, null);
            TextView textView = grid.findViewById(R.id.grid_text);
            textView.setText(web[position]);
        } else {
            grid = (View) convertView;
        }
        return grid;
    }
}