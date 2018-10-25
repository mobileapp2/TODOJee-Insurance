package com.insurance.todojee.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.format.DateTimeFormatter;

public class CalenderMonthWise_Activity extends Fragment implements OnDateSelectedListener, OnMonthChangedListener {

    private Context context;
    private MaterialCalendarView calendarView;
    private UserSessionManager session;
    private String user_id, date;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar_monthwise, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventListner();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        calendarView = rootView.findViewById(R.id.calendarView);
    }

    private void getSessionData() {
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            user_id = json.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefault() {
        CalendarDay date = CalendarDay.today();
        onDateSelected(calendarView, date, true);
        calendarView.setSelectedDate(date);
    }

    private void setEventListner() {
        calendarView.setOnDateChangedListener(this);
        calendarView.setOnMonthChangedListener(this);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

    }
}
