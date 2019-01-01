package com.insurance.todojee.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.insurance.todojee.R;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter {
    static final int FIRST_DAY_OF_WEEK = 0;
    private Calendar selectedDate;
    private Context context;
    private Calendar month;
    public static String[] days;
    private ArrayList<String> campDateList;

    public CalendarAdapter(Context context, Calendar month) {
        this.month = month;
        this.context = context;
        selectedDate = (Calendar) month.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        this.campDateList = new ArrayList<String>();
        refreshDays();
    }

    public void setItems(ArrayList<String> campDateList) {

        for (int i = 0; i != campDateList.size(); i++) {
            if (campDateList.get(i).length() == 1) {
                campDateList.set(i, "0" + campDateList.get(i));
            }
        }
        this.campDateList = campDateList;
    }

    @Override
    public int getCount() {
        return days.length;
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
        View v = convertView;
        TextView dayView;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_calendar, null);

        }
        dayView = v.findViewById(R.id.date);

        // disable empty days from the beginning
        if (days[position].equals("")) {
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else {
            v.setBackgroundResource(R.drawable.header_background);
        }

        String date = days[position];

        if (date.length() == 1) {
            date = "0" + date;
        }
        dayView.setText(date);

//        if (date.length() > 0 && campDateList != null && campDateList.contains(date)) {
//            v.setBackgroundColor(context.getResources().getColor(R.color.UserAttendance_green));
//            dayView.setTextColor(context.getResources().getColor(R.color.white));
//        }

        return v;
    }

    public void refreshDays() {

        int lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDay = (int) month.get(Calendar.DAY_OF_WEEK);

        // figure size of the array
        if (firstDay == 1) {
            days = new String[lastDay + (FIRST_DAY_OF_WEEK * 6)];
        } else {
            days = new String[lastDay + firstDay - (FIRST_DAY_OF_WEEK + 1)];
        }

        int j = FIRST_DAY_OF_WEEK;

        // populate empty days before first real day
        if (firstDay > 1) {
            for (j = 0; j < firstDay - FIRST_DAY_OF_WEEK; j++) {
                days[j] = "";
            }
        } else {
            for (j = 0; j < FIRST_DAY_OF_WEEK * 6; j++) {
                days[j] = "";
            }
            j = FIRST_DAY_OF_WEEK * 6 + 1; // sunday => 1, monday => 7
        }

        // populate days
        int dayNumber = 1;
        for (int i = j - 1; i < days.length; i++) {
            days[i] = "" + dayNumber;
            dayNumber++;
        }
    }

    public static String[] getDate() {
        return days;
    }
}