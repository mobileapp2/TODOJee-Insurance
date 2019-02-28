package com.insurance.todojee.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.insurance.todojee.R;
import com.insurance.todojee.ccavenue.AvenuesParams;
import com.insurance.todojee.ccavenue.ServiceUtility;
import com.insurance.todojee.ccavenue.WebViewActivity;
import com.insurance.todojee.models.PremiumPlanModel;
import com.insurance.todojee.models.PremiumPlanPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectPremiumPlan_Activity extends Activity {

    public LinearLayout ll_parent;
    private Context context;
    private RecyclerView rv_planlist;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab_next;
    private LinearLayoutManager layoutManager;
    private UserSessionManager session;
    private String user_id;
    private ProgressDialog pd;
    private static int lastSelectedPosition = -1;
    private ArrayList<PremiumPlanModel> plansList;
    Integer randomNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_premiumplan);

        init();
        getSessionData();
        setDefault();
        setEventHandlers();
        setUpToolbar();
    }

    private void init() {
        context = SelectPremiumPlan_Activity.this;
        session = new UserSessionManager(context);
        pd = new ProgressDialog(context);
        fab_next = findViewById(R.id.fab_next);
        rv_planlist = findViewById(R.id.rv_planlist);
        ll_parent = findViewById(R.id.ll_parent);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(context);
        rv_planlist.setLayoutManager(layoutManager);
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
        if (Utilities.isNetworkAvailable(context)) {
            new GetPlanList().execute();
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }
    }

    private void setEventHandlers() {
        fab_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lastSelectedPosition == -1) {
                    Utilities.showAlertDialog(context, "Alert", "Please Select Any One Details", false);
                } else {
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra(AvenuesParams.ACCESS_CODE, ApplicationConstants.ACCESS_CODE);
                    intent.putExtra(AvenuesParams.MERCHANT_ID, ApplicationConstants.MERCHANT_ID);
                    intent.putExtra(AvenuesParams.ORDER_ID, randomNum.toString());
                    intent.putExtra(AvenuesParams.CURRENCY, ApplicationConstants.CURRENCY);
                    intent.putExtra(AvenuesParams.AMOUNT, plansList.get(lastSelectedPosition).getAmount());
                    intent.putExtra(AvenuesParams.REDIRECT_URL, ApplicationConstants.REDIRECT_URL);
                    intent.putExtra(AvenuesParams.CANCEL_URL, ApplicationConstants.CANCEL_URL);
                    intent.putExtra(AvenuesParams.RSA_KEY_URL, ApplicationConstants.RSA_KEY_URL);


                    intent.putExtra("type", plansList.get(lastSelectedPosition).getPlan());
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("plan_id", plansList.get(lastSelectedPosition).getId());
                    intent.putExtra("space", plansList.get(lastSelectedPosition).getSpace());
                    intent.putExtra("sms", plansList.get(lastSelectedPosition).getSms());
                    intent.putExtra("whatsApp_msg", plansList.get(lastSelectedPosition).getWhtasApp_msg());
                    intent.putExtra("expire_date", plansList.get(lastSelectedPosition).getEnd_date());

                    startActivity(intent);
                }

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetPlanList().execute();
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }


    public class GetPlanList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Please wait . . . ");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "givePlanDetails"));

            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.PLANLISTAPI, param);
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            swipeRefreshLayout.setRefreshing(false);
            String type = "", message = "";
            try {
                if (!result.equals("")) {
                    plansList = new ArrayList<>();
                    PremiumPlanPojo pojoDetails = new Gson().fromJson(result, PremiumPlanPojo.class);
                    type = pojoDetails.getType();
                    message = pojoDetails.getMessage();
                    if (type.equalsIgnoreCase("success")) {
                        plansList = pojoDetails.getResult();
                        if (plansList.size() > 0) {
                            rv_planlist.setAdapter(new PremiumPlansListAdapter(context, plansList));

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static class PremiumPlansListAdapter extends RecyclerView.Adapter<PremiumPlansListAdapter.MyViewHolder> {

        private static List<PremiumPlanModel> resultArrayList;
        private final UserSessionManager session;
        private Context context;
        private String name;

        public PremiumPlansListAdapter(Context context, List<PremiumPlanModel> resultArrayList) {
            this.context = context;
            this.resultArrayList = resultArrayList;
            session = new UserSessionManager(context);
            try {
                JSONArray user_info = new JSONArray(session.getUserDetails().get(
                        ApplicationConstants.KEY_LOGIN_INFO));
                JSONObject json = user_info.getJSONObject(0);
                name = json.getString("name");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_premiumplan, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.tv_planname.setText(resultArrayList.get(position).getPlan());
            holder.tv_amount.setText("₹ " + resultArrayList.get(position).getAmount());
            holder.tv_description.setText("Plan Desc : " + resultArrayList.get(position).getPlan_description());
            holder.tv_validity.setText("Validity : " + resultArrayList.get(position).getValidity());
            holder.tv_space.setText("Space : " + resultArrayList.get(position).getSpace());
            holder.tv_clients.setText("Clients : " + resultArrayList.get(position).getCustomers());
            holder.tv_whatsapp.setText("WhatsApp SMS : " + resultArrayList.get(position).getWhtasApp_msg());
            holder.tv_sms.setText("Text SMS : " + resultArrayList.get(position).getSms());
            holder.tv_policies.setText("Policies : " + resultArrayList.get(position).getPolicies());


            holder.rb_selectone.setChecked(lastSelectedPosition == position);


        }

        @Override
        public int getItemCount() {
            return resultArrayList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_planname, tv_amount, tv_description, tv_validity, tv_space, tv_clients, tv_whatsapp, tv_sms, tv_policies;
            private RadioButton rb_selectone;

            public MyViewHolder(View view) {
                super(view);

                tv_planname = view.findViewById(R.id.tv_planname);
                tv_amount = view.findViewById(R.id.tv_amount);
                tv_description = view.findViewById(R.id.tv_description);
                tv_validity = view.findViewById(R.id.tv_validity);
                tv_space = view.findViewById(R.id.tv_space);
                tv_clients = view.findViewById(R.id.tv_clients);
                tv_whatsapp = view.findViewById(R.id.tv_whatsapp);
                tv_sms = view.findViewById(R.id.tv_sms);
                tv_policies = view.findViewById(R.id.tv_policies);
                rb_selectone = view.findViewById(R.id.rb_selectone);

                rb_selectone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastSelectedPosition = getAdapterPosition();
                        notifyDataSetChanged();
                    }
                });


            }
        }
    }


    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Premium Plans");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //generating new order number for every transaction
        randomNum = ServiceUtility.randInt(0, 9999999);
    }


}
