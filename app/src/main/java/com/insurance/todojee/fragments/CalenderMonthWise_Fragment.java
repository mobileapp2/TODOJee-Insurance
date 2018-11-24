package com.insurance.todojee.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.adapters.GetEventListAdapter;
import com.insurance.todojee.models.EventListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.RecyclerItemClickListener;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CalenderMonthWise_Fragment extends Fragment implements OnDateSelectedListener/*, OnMonthChangedListener*/ {

    public LinearLayout ll_parent;
    private Context context;
    private MaterialCalendarView calendarView;
    private UserSessionManager session;
    private String user_id, dateStr;
    private LinearLayoutManager layoutManager;
    private LinearLayout ll_eventlayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rv_eventlist;
    private int mYear, mMonth, mDay;
    private ArrayList<EventListPojo> eventList;

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
        ll_parent = getActivity().findViewById(R.id.ll_parent);
        calendarView = rootView.findViewById(R.id.calendarView);
        ll_eventlayout = rootView.findViewById(R.id.ll_eventlayout);
        rv_eventlist = rootView.findViewById(R.id.rv_eventlist);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(context);
        rv_eventlist.setLayoutManager(layoutManager);

        eventList = new ArrayList<>();
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
        ll_eventlayout.setVisibility(View.GONE);

        Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        CalendarDay Caldate = CalendarDay.today();

//        CalendarDay date = new CalendarDay(mYear, mMonth + 1, mDay);

//        onDateSelected(calendarView, Caldate, true);
        calendarView.setSelectedDate(Caldate);

        dateStr = new SimpleDateFormat("yyyy-MM-dd").format(Caldate.getDate());

        if (Utilities.isNetworkAvailable(context)) {
            new GetEventList().execute(user_id, dateStr);
            swipeRefreshLayout.setRefreshing(true);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_eventlayout.setVisibility(View.GONE);
        }
    }

    private void setEventListner() {
        calendarView.setOnDateChangedListener(this);
//        calendarView.setOnMonthChangedListener(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetEventList().execute(user_id, dateStr);
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_eventlayout.setVisibility(View.GONE);
                }
            }
        });

        rv_eventlist.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventListPojo eventListPojo = eventList.get(position);
                String[] choices = {"Paid", "Not Paid"};
                String[] statuses = {"Completed", "Dismissed"};
                final String[] status = {"Completed"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setTitle("Select Your Choice");
                builder.setCancelable(false);

                builder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        status[0] = statuses[item];
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Utilities.isInternetAvailable(context)) {
                            new ChangeEventStatus().execute(eventListPojo.getId(), dateStr, status[0]);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                        }
                    }
                });

                AlertDialog alertD = builder.create();
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();
            }
        }));
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        widget.setSelectedDate(date);

        dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date.getDate());

        if (Utilities.isNetworkAvailable(context)) {
            new GetEventListWithProgressDialog().execute(user_id, dateStr);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_eventlayout.setVisibility(View.GONE);
        }

    }

    public class GetEventList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "getEventStatus");
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
                rv_eventlist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    eventList = new ArrayList<>();
                    rv_eventlist.setAdapter(new GetEventListAdapter(context, eventList));
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
                                ll_eventlayout.setVisibility(View.GONE);
                            } else {
                                ll_eventlayout.setVisibility(View.VISIBLE);
                            }
                            rv_eventlist.setAdapter(new GetEventListAdapter(context, eventList));
                        }
                    } else {
                        ll_eventlayout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_eventlayout.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public class GetEventListWithProgressDialog extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please Wait . . .");
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "getEventStatus");
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
                rv_eventlist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    eventList = new ArrayList<>();
                    rv_eventlist.setAdapter(new GetEventListAdapter(context, eventList));
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
                                ll_eventlayout.setVisibility(View.GONE);
                            } else {
                                ll_eventlayout.setVisibility(View.VISIBLE);
                            }
                            rv_eventlist.setAdapter(new GetEventListAdapter(context, eventList));
                        }
                    } else {
                        ll_eventlayout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_eventlayout.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public class ChangeEventStatus extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait . . .");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "statusChanged");
            obj.addProperty("id", params[0]);
            obj.addProperty("date", params[1]);
            obj.addProperty("status", params[2]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.EVENTSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {

                        if (Utilities.isNetworkAvailable(context)) {
                            new GetEventListWithProgressDialog().execute(user_id, dateStr);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                            swipeRefreshLayout.setRefreshing(false);
                            ll_eventlayout.setVisibility(View.GONE);
                        }
                    } else {
                        Utilities.showAlertDialog(context, "Alert", message, false);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
