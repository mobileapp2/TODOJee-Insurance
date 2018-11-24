package com.insurance.todojee.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.CalenderWeekWise_Fragment;
import com.insurance.todojee.models.WeekWiseEventListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.RecyclerItemClickListener;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONObject;

import java.util.List;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

public class GetWeekWiseEventListAdapter extends RecyclerView.Adapter<GetWeekWiseEventListAdapter.MyViewHolder> {

    private List<WeekWiseEventListPojo> resultArrayList;
    private Context context;
    private String user_id, weekStartDateStr;

    public GetWeekWiseEventListAdapter(Context context, List<WeekWiseEventListPojo> resultArrayList, String user_id, String weekStartDateStr) {
        this.context = context;
        this.resultArrayList = resultArrayList;
        this.user_id = user_id;
        this.weekStartDateStr = weekStartDateStr;

//        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row_weekwiseevents, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        WeekWiseEventListPojo eventMainDetails = new WeekWiseEventListPojo();
        eventMainDetails = resultArrayList.get(position);
        WeekWiseEventListPojo finalEventMainDetails = eventMainDetails;

        if (eventMainDetails.getEventListPojos().size() != 0) {
            holder.card_view.setVisibility(View.VISIBLE);
            holder.ll_mainlayout.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            holder.rv_eventlist.setLayoutManager(layoutManager);
            holder.tv_date.setText(changeDateFormat("MM/dd/yyyy", "dd MMM yyyy EEEE", eventMainDetails.getDate()));
            holder.rv_eventlist.setAdapter(new GetWeekWiseChildEventListAdapter(context, eventMainDetails.getEventListPojos()));
        } else {
            holder.card_view.setVisibility(View.GONE);
            holder.ll_mainlayout.setVisibility(View.GONE);
        }

        holder.rv_eventlist.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                WeekWiseEventListPojo.EventListPojo eventListPojo = finalEventMainDetails.getEventListPojos().get(position);
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
                            new ChangeEventStatus().execute(eventListPojo.getId(),
                                    changeDateFormat("MM/dd/yyyy", "yyyy-MM-dd", eventListPojo.getDate()),
                                    status[0]);
                        } else {
                            Utilities.showMessageString(context, "Please Check Internet Connection");
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
    public int getItemCount() {
        return resultArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_date;
        private RecyclerView rv_eventlist;
        private CardView card_view;
        private LinearLayout ll_mainlayout;

        public MyViewHolder(View view) {
            super(view);
            tv_date = view.findViewById(R.id.tv_date);
            rv_eventlist = view.findViewById(R.id.rv_eventlist);
            card_view = view.findViewById(R.id.card_view);
            ll_mainlayout = view.findViewById(R.id.ll_mainlayout);
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
                        new CalenderWeekWise_Fragment.GetEventListWithProgressDialog().execute(user_id, weekStartDateStr);
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
