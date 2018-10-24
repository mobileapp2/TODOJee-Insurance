package com.insurance.todojee.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.adapters.CalendarAdapter;
import com.insurance.todojee.adapters.WeekAdapter;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CalenderDay_Fragment extends Fragment {

    private Context context;
    private TextView tv_previous, tv_title, tv_next;
    private GridView gdv_week, gdv_cal;
    private String CurrentDate, PastDate, user_id, currentDate, year, month, monthName;
    private WeekAdapter weekAdapter;
    private CalendarAdapter calAdapter;
    private String[] weekday = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private Calendar calMonth;
    private UserSessionManager session;
    private ProgressDialog pd;
    private ArrayList<String> campDateList;
    //    private ArrayList<CampDatesPojo> campList;
    private Handler handler;
    private RecyclerView.LayoutManager layoutManager;
    private int mYear, mMonth, mDay, selectedMonthId, selectedYearId;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar_daywise, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventListner();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        pd = new ProgressDialog(context, R.style.CustomDialogTheme);
        tv_previous = rootView.findViewById(R.id.tv_previous);
        tv_title = rootView.findViewById(R.id.tv_title);
        tv_next = rootView.findViewById(R.id.tv_next);
        gdv_week = rootView.findViewById(R.id.gdv_week);
        gdv_cal = rootView.findViewById(R.id.gdv_cal);
        calMonth = Calendar.getInstance();
        handler = new Handler();
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
        Calendar cal = Calendar.getInstance();
        CurrentDate = (Utilities.dfDate2).format(cal.getTime());
        cal.add(Calendar.MONTH, -1);
        PastDate = (Utilities.dfDate2).format(cal.getTime());

        weekAdapter = new WeekAdapter(context, weekday);
        gdv_week.setAdapter(weekAdapter);
        calAdapter = new CalendarAdapter(context, calMonth);
        tv_title.setText(android.text.format.DateFormat.format("MMMM yyyy", calMonth));

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        currentDate = df.format(c.getTime());
        calMonth = Calendar.getInstance();

        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        monthName = new DateFormatSymbols().getMonths()[mMonth];
        selectedMonthId = mMonth + 1;
        selectedYearId = mYear;

        campDateList = new ArrayList<>();
        String[] monthYear = (android.text.format.DateFormat.format("MM yyyy", calMonth)).toString().split(" ");
        month = monthYear[0];
        year = monthYear[1];

        refreshCalendar();

//        if (Utilities.isNetworkAvailable(context)) {
//            new GetCampAvailabilityStatus().execute(year, month, Labcode, desig_id, user_id);
//        } else {
//            Utilities.showMessage(R.string.msg_nointernetconnection, context);
//        }

    }

    private void setEventListner() {
        tv_previous.setOnClickListener(v -> {
            if (calMonth.get(Calendar.MONTH) == calMonth.getActualMinimum(Calendar.MONTH)) {
                calMonth.set((calMonth.get(Calendar.YEAR) - 1), calMonth.getActualMaximum(Calendar.MONTH), 1);
            } else {
                calMonth.set(Calendar.MONTH, calMonth.get(Calendar.MONTH) - 1);
            }
            refreshCalendar();
        });

        tv_next.setOnClickListener(v -> {
            if (calMonth.get(Calendar.MONTH) == calMonth.getActualMaximum(Calendar.MONTH)) {
                calMonth.set((calMonth.get(Calendar.YEAR) + 1), calMonth.getActualMinimum(Calendar.MONTH), 1);
            } else {
                calMonth.set(Calendar.MONTH, calMonth.get(Calendar.MONTH) + 1);
            }
            refreshCalendar();
        });

        gdv_cal.setOnItemClickListener((parent, view, position, id) -> {

//            String[] dateAdapter = CalendarAdapter.getDate();
//
//            for (int i = 0; i < campDateList.size(); i++) {
//                if (campDateList.equals(dateAdapter[position])) {
//                    Labcode = campList.get(i).getLabCode();
//                }
//            }
//
//
//            TextView date = view.findViewById(R.id.date);
//
//            if (date != null && !date.getText().equals("")) {
//                String day = date.getText().toString();
//                if (day.length() == 1) { day = "0" + day; }
//
//                String[] monthYear = (android.text.format.DateFormat.format("MM yyyy", calMonth)).toString().split(" ");
//                String dateselected = monthYear[1] + "/" + monthYear[0] + "/" + day;
//
//                if (Utilities.isNetworkAvailable(context)) {
//                    new GetCampOnLabCodeAndDate().execute(Labcode, dateselected);
//                } else {
//                    Utilities.showMessage(R.string.msg_nointernetconnection, context);
//                }
//            }
        });

    }

    public void refreshCalendar() {
        calAdapter.refreshDays();
        String[] monthYear = (android.text.format.DateFormat.format("MM yyyy", calMonth)).toString().split(" ");
        month = monthYear[0];
        year = monthYear[1];

//        if (Utilities.isNetworkAvailable(context)) {
//            new GetCampAvailabilityStatus().execute(year, month, Labcode, desig_id, user_id);
//        } else {
//            Utilities.showMessage(R.string.msg_nointernetconnection, context);
//        }

        calAdapter.notifyDataSetChanged();
        handler.post(calendarUpdater); // generate some random calendar items

        tv_title.setText(android.text.format.DateFormat.format("MMMM yyyy", calMonth));

        calAdapter = new CalendarAdapter(context, calMonth);
        weekAdapter = new WeekAdapter(context, weekday);
        gdv_week.setAdapter(weekAdapter);
        gdv_cal.setAdapter(calAdapter);
        handler = new Handler();
        handler.post(calendarUpdater);
        tv_title.setText(android.text.format.DateFormat.format("MMMM yyyy", calMonth));
        setEventListner();
    }

    public Runnable calendarUpdater = new Runnable() {
        @Override
        public void run() {
            calAdapter.setItems(campDateList);
            calAdapter.notifyDataSetChanged();
        }
    };


}
