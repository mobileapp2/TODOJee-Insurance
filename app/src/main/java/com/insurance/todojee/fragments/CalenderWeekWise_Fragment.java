package com.insurance.todojee.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalenderWeekWise_Fragment extends Fragment {

    private Context context;
    private UserSessionManager session;
    private String user_id;
    private int mYear, mMonth, mDay;
    private ImageView imv_backweek, imv_nextweek;
    private TextView tv_daterange;
    private Date weekStartDate, weekEndDate;
    String weekStartDateStr, weekEndDateStr;
    SimpleDateFormat dateFormat;
    int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar_weekwise, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        imv_backweek = rootView.findViewById(R.id.imv_backweek);
        imv_nextweek = rootView.findViewById(R.id.imv_nextweek);
        tv_daterange = rootView.findViewById(R.id.tv_daterange);
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
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//        mYear = cal.get(Calendar.YEAR);
//        mMonth = cal.get(Calendar.MONTH);
//        mDay = cal.get(Calendar.DAY_OF_MONTH);

        dateFormat = new SimpleDateFormat("dd MMM yyyy");

        weekStartDate = getWeekStartDate(Calendar.getInstance());
        weekEndDate = getWeekEndDate(Calendar.getInstance());

        weekStartDateStr = dateFormat.format(weekStartDate);
        weekEndDateStr = dateFormat.format(weekEndDate);

        tv_daterange.setText(weekStartDateStr + " - " + weekEndDateStr);

    }

    private void setEventHandlers() {
        imv_backweek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekEndDateStr = dateFormat.format(weekStartDate.getTime() -  MILLIS_IN_DAY);
                try {
                    weekEndDate = dateFormat.parse(weekEndDateStr);
                    Calendar cal1 = Calendar.getInstance();
                    cal1 = toCalendar(weekEndDate);
                    Calendar cal2 = Calendar.getInstance();
                    cal2 = toCalendar(weekEndDate);

                    weekStartDate = getWeekStartDate(cal1);
                    weekEndDate = getWeekEndDate(cal2);

                    weekStartDateStr = dateFormat.format(weekStartDate);
                    weekEndDateStr = dateFormat.format(weekEndDate);

                    tv_daterange.setText(weekStartDateStr + " - " + weekEndDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        imv_nextweek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekStartDateStr = dateFormat.format(weekEndDate.getTime() +  MILLIS_IN_DAY);
                try {
                    weekStartDate = dateFormat.parse(weekStartDateStr);
                    Calendar cal1 = Calendar.getInstance();
                    cal1 = toCalendar(weekStartDate);
                    Calendar cal2 = Calendar.getInstance();
                    cal2 = toCalendar(weekStartDate);

                    weekStartDate = getWeekStartDate(cal1);
                    weekEndDate = getWeekEndDate(cal2);

                    weekStartDateStr = dateFormat.format(weekStartDate);
                    weekEndDateStr = dateFormat.format(weekEndDate);

                    tv_daterange.setText(weekStartDateStr + " - " + weekEndDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Date getWeekStartDate(Calendar calendar) {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, -1);
        }
        return calendar.getTime();
    }

    public static Date getWeekEndDate(Calendar calendar) {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calendar.add(Calendar.DATE, 1);
        }
//        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }


}
