package com.insurance.todojee.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.adapters.GetWeekWiseEventListAdapter;
import com.insurance.todojee.models.EventListPojo;
import com.insurance.todojee.models.WeekWiseEventListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

public class CalenderWeekWise_Fragment extends Fragment {

    private static Context context;
    public LinearLayout ll_parent;
    public static LinearLayout ll_nothingtoshow;
    private UserSessionManager session;
    private static String user_id;
    private int mYear, mMonth, mDay;
    private ImageView imv_backweek, imv_nextweek;
    private TextView tv_daterange;
    private LinearLayoutManager layoutManager;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static RecyclerView rv_eventlist;

    private Date weekStartDate, weekEndDate;
    private static String weekStartDateStr;
    private String weekEndDateStr;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateFormat2;
    private static SimpleDateFormat dateFormat3;
    private int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private static ArrayList<EventListPojo> eventList;
    private static ArrayList<Date> dateList;

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
        ll_parent = getActivity().findViewById(R.id.ll_parent);
        imv_backweek = rootView.findViewById(R.id.imv_backweek);
        imv_nextweek = rootView.findViewById(R.id.imv_nextweek);
        tv_daterange = rootView.findViewById(R.id.tv_daterange);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);

        rv_eventlist = rootView.findViewById(R.id.rv_eventlist);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(context);
        rv_eventlist.setLayoutManager(layoutManager);

        eventList = new ArrayList<>();
        dateList = new ArrayList<>();
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

        dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat3 = new SimpleDateFormat("MM/dd/yyyy");

        weekStartDate = getWeekStartDate(Calendar.getInstance());
        weekEndDate = getWeekEndDate(Calendar.getInstance());

        dateList = getDaysBetweenDates(weekStartDate, weekEndDate);

        weekStartDateStr = dateFormat.format(weekStartDate);
        weekEndDateStr = dateFormat.format(weekEndDate);

        tv_daterange.setText(weekStartDateStr + " - " + weekEndDateStr);

        weekStartDateStr = dateFormat2.format(weekStartDate);

        if (Utilities.isNetworkAvailable(context)) {
            new GetEventList().execute(user_id, weekStartDateStr);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
    }

    private void setEventHandlers() {
        imv_backweek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekEndDateStr = dateFormat.format(weekStartDate.getTime() - MILLIS_IN_DAY);
                try {
                    weekEndDate = dateFormat.parse(weekEndDateStr);
                    Calendar cal1 = Calendar.getInstance();
                    cal1 = toCalendar(weekEndDate);
                    Calendar cal2 = Calendar.getInstance();
                    cal2 = toCalendar(weekEndDate);

                    weekStartDate = getWeekStartDate(cal1);
                    weekEndDate = getWeekEndDate(cal2);

                    dateList = getDaysBetweenDates(weekStartDate, weekEndDate);

                    weekStartDateStr = dateFormat.format(weekStartDate);
                    weekEndDateStr = dateFormat.format(weekEndDate);

                    tv_daterange.setText(weekStartDateStr + " - " + weekEndDateStr);

                    weekStartDateStr = dateFormat2.format(weekStartDate);

                    if (Utilities.isNetworkAvailable(context)) {
                        new GetEventListWithProgressDialog().execute(user_id, weekStartDateStr);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                        swipeRefreshLayout.setRefreshing(false);
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        imv_nextweek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekStartDateStr = dateFormat.format(weekEndDate.getTime() + MILLIS_IN_DAY);
                try {
                    weekStartDate = dateFormat.parse(weekStartDateStr);
                    Calendar cal1 = Calendar.getInstance();
                    cal1 = toCalendar(weekStartDate);
                    Calendar cal2 = Calendar.getInstance();
                    cal2 = toCalendar(weekStartDate);

                    weekStartDate = getWeekStartDate(cal1);
                    weekEndDate = getWeekEndDate(cal2);

                    dateList = getDaysBetweenDates(weekStartDate, weekEndDate);

                    weekStartDateStr = dateFormat.format(weekStartDate);
                    weekEndDateStr = dateFormat.format(weekEndDate);

                    tv_daterange.setText(weekStartDateStr + " - " + weekEndDateStr);

                    weekStartDateStr = dateFormat2.format(weekStartDate);

                    if (Utilities.isNetworkAvailable(context)) {
                        new GetEventListWithProgressDialog().execute(user_id, weekStartDateStr);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                        swipeRefreshLayout.setRefreshing(false);
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetEventList().execute(user_id, weekStartDateStr);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setVisibility(View.GONE);
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

    public class GetEventList extends AsyncTask<String, Void, String> {
        ArrayList<WeekWiseEventListPojo> weekWiseEventList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "getWeeklyEventStatus");
            obj.addProperty("user_id", params[0]);
            obj.addProperty("date", params[1]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.EVENTSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    eventList = new ArrayList<>();
                    weekWiseEventList = new ArrayList<>();
                    rv_eventlist.setAdapter(new GetWeekWiseEventListAdapter(context, weekWiseEventList, user_id, weekStartDateStr));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                EventListPojo eventMainObj = new EventListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                eventMainObj.setId(jsonObj.getString("id"));
                                eventMainObj.setDescription(jsonObj.getString("description"));
                                eventMainObj.setStatus(jsonObj.getString("status"));
                                eventMainObj.setDate(jsonObj.getString("date"));
                                eventList.add(eventMainObj);
                            }
                            if (eventList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setVisibility(View.GONE);
                            } else {
                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);

                                weekWiseEventList = new ArrayList<>();
                                ArrayList<WeekWiseEventListPojo.EventListPojo> childEventList;
                                for (int i = 0; i < dateList.size(); i++) {
                                    WeekWiseEventListPojo weekWiseeventMainObj = new WeekWiseEventListPojo();
                                    childEventList = new ArrayList<>();
                                    String compareDateStr = dateFormat3.format(dateList.get(i));
                                    weekWiseeventMainObj.setDate(compareDateStr);
                                    for (EventListPojo eventObj : eventList) {
                                        if (compareDateStr.contains(changeDateFormat("MM/dd/yyyy", "MM/dd/yyyy", eventObj.getDate()))) {
                                            WeekWiseEventListPojo.EventListPojo eventChildObj = new WeekWiseEventListPojo.EventListPojo();
                                            eventChildObj.setId(eventObj.getId());
                                            eventChildObj.setDescription(eventObj.getDescription());
                                            eventChildObj.setStatus(eventObj.getStatus());
                                            eventChildObj.setDate(eventObj.getDate());
                                            childEventList.add(eventChildObj);
                                        }
                                    }
                                    weekWiseeventMainObj.setEventListPojos(childEventList);
                                    weekWiseEventList.add(weekWiseeventMainObj);
                                }
                            }

                            rv_eventlist.setAdapter(new GetWeekWiseEventListAdapter(context, weekWiseEventList, user_id, weekStartDateStr));
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public static class GetEventListWithProgressDialog extends AsyncTask<String, Void, String> {
        ProgressDialog pd;
        ArrayList<WeekWiseEventListPojo> weekWiseEventList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please Wait . . .");
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "getWeeklyEventStatus");
            obj.addProperty("user_id", params[0]);
            obj.addProperty("date", params[1]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.EVENTSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                pd.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    eventList = new ArrayList<>();
                    weekWiseEventList = new ArrayList<>();
                    rv_eventlist.setAdapter(new GetWeekWiseEventListAdapter(context, weekWiseEventList, user_id, weekStartDateStr));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                EventListPojo eventMainObj = new EventListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                eventMainObj.setId(jsonObj.getString("id"));
                                eventMainObj.setDescription(jsonObj.getString("description"));
                                eventMainObj.setStatus(jsonObj.getString("status"));
                                eventMainObj.setDate(jsonObj.getString("date"));
                                eventList.add(eventMainObj);
                            }
                            if (eventList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                swipeRefreshLayout.setVisibility(View.GONE);
                            } else {
                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);

                                weekWiseEventList = new ArrayList<>();
                                ArrayList<WeekWiseEventListPojo.EventListPojo> childEventList;
                                for (int i = 0; i < dateList.size(); i++) {
                                    WeekWiseEventListPojo weekWiseeventMainObj = new WeekWiseEventListPojo();
                                    childEventList = new ArrayList<>();
                                    String compareDateStr = dateFormat3.format(dateList.get(i));
                                    weekWiseeventMainObj.setDate(compareDateStr);
                                    for (EventListPojo eventObj : eventList) {
                                        if (compareDateStr.contains(changeDateFormat("MM/dd/yyyy", "MM/dd/yyyy", eventObj.getDate()))) {
                                            WeekWiseEventListPojo.EventListPojo eventChildObj = new WeekWiseEventListPojo.EventListPojo();
                                            eventChildObj.setId(eventObj.getId());
                                            eventChildObj.setDescription(eventObj.getDescription());
                                            eventChildObj.setStatus(eventObj.getStatus());
                                            eventChildObj.setDate(eventObj.getDate());
                                            childEventList.add(eventChildObj);
                                        }
                                    }
                                    weekWiseeventMainObj.setEventListPojos(childEventList);
                                    weekWiseEventList.add(weekWiseeventMainObj);
                                }
                            }

                            rv_eventlist.setAdapter(new GetWeekWiseEventListAdapter(context, weekWiseEventList, user_id, weekStartDateStr));
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Date> getDaysBetweenDates(Date startdate, Date enddate) {
        ArrayList<Date> dates = new ArrayList<Date>();
        Date finalEndDate = null;
        String endDateStr = dateFormat.format(enddate.getTime() + MILLIS_IN_DAY);
        try {
            finalEndDate = dateFormat.parse(endDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Date weekStartDate = dateFormat.parse(weekStartDateStr);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startdate);

        while (calendar.getTime().before(finalEndDate)) {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }

}
