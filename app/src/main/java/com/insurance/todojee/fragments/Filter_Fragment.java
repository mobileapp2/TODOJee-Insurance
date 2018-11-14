package com.insurance.todojee.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.models.FamilyCodePojo;
import com.insurance.todojee.models.FilterOptionsListPojo;
import com.insurance.todojee.models.FrequencyListPojo;
import com.insurance.todojee.models.PolicyStatusListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.RecyclerItemClickListener;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Filter_Fragment extends Fragment {

    public LinearLayout ll_parent;
    private Context context;
    private RecyclerView rv_filteroption, rv_filtervalue;
    private LinearLayoutManager layoutManager1, layoutManager2;
    private UserSessionManager session;
    private String user_id;
    private ArrayList<FilterOptionsListPojo> filterOptionsList;
    private FilterOptionListAdaper filterAdaper;
    private int globalPosition = 0;

    private ArrayList<FamilyCodePojo> familyCodeList;
    private ArrayList<PolicyStatusListPojo> policyStatusList;
    private ArrayList<FrequencyListPojo> frequencyList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filter, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);

        rv_filteroption = rootView.findViewById(R.id.rv_filteroption);
        rv_filtervalue = rootView.findViewById(R.id.rv_filtervalue);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
        layoutManager1 = new LinearLayoutManager(context);
        layoutManager2 = new LinearLayoutManager(context);
        rv_filteroption.setLayoutManager(layoutManager1);
        rv_filtervalue.setLayoutManager(layoutManager2);

        filterAdaper = new FilterOptionListAdaper();
        filterOptionsList = new ArrayList<>();

        familyCodeList = new ArrayList<>();
//        selectedfamilyCodeList = new ArrayList<>();
        policyStatusList = new ArrayList<>();
//        selectedPolicyStatusList = new ArrayList<>();
        frequencyList = new ArrayList<>();
//        selectedFrequenctList = new ArrayList<>();
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
        String[] filterOptions = {"Family Code", "Insurer", "Insurance Type", "Company", "Policy Type", "Frequency", "Policy Status"};
        int[] filterIcons = {R.drawable.icon_familycode, R.drawable.icon_insurer, R.drawable.icon_insurancetype, R.drawable.icon_insurancecompany,
                R.drawable.icon_policy, R.drawable.icon_frequency, R.drawable.icon_polictstatus};
        boolean[] checked = {true, false, false, false, false, false, false};

        for (int i = 0; i < filterOptions.length; i++) {
            filterOptionsList.add(new FilterOptionsListPojo(filterOptions[i], filterIcons[i], checked[i]));
        }

        rv_filteroption.setAdapter(filterAdaper);


        if (familyCodeList.size() == 0) {
            if (Utilities.isNetworkAvailable(context)) {
                new GetFamilyCodeList().execute(user_id);
            } else {
                Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            }
        }
    }

    private void setEventHandlers() {
        rv_filteroption.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                globalPosition = position;

                for (int i = 0; i < filterOptionsList.size(); i++) {

                    FilterOptionListAdaper.MyViewHolder holder =
                            (FilterOptionListAdaper.MyViewHolder) rv_filteroption.findViewHolderForAdapterPosition(i);

                    if (i == position) {
                        filterOptionsList.get(i).setChecked(true);
                        holder.ll_mainlayout.setBackgroundColor(getResources().getColor(R.color.white));
                        holder.imv_filtericon.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                        holder.tv_filteroption.setTextColor(getResources().getColor(R.color.colorPrimary));
                        loadFilterItemsList();
                    } else {
                        filterOptionsList.get(i).setChecked(false);
                        holder.ll_mainlayout.setBackgroundColor(getResources().getColor(R.color.lightgray));
                        holder.imv_filtericon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
                        holder.tv_filteroption.setTextColor(getResources().getColor(R.color.grey));
                    }
                }
            }
        }));
    }

    private void loadFilterItemsList() {
        if (globalPosition == 0) {
            if (familyCodeList.size() == 0) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetFamilyCodeList().execute(user_id);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                rv_filtervalue.setAdapter(new FamilyCodeListAdapter());
            }
        } else if (globalPosition == 1) {

        } else if (globalPosition == 2) {

        } else if (globalPosition == 3) {

        } else if (globalPosition == 4) {

        } else if (globalPosition == 5) {
            if (frequencyList.size() == 0) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetFrequenctList().execute();
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                rv_filtervalue.setAdapter(new FrequencyAdapter());
            }
        } else if (globalPosition == 6) {
            if (policyStatusList.size() == 0) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetPolicyStatusList().execute();
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                rv_filtervalue.setAdapter(new PolicyStatusAdapter());
            }
        }
    }

    public class FilterOptionListAdaper extends RecyclerView.Adapter<FilterOptionListAdaper.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteroptions, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.ll_mainlayout.setSelected(filterOptionsList.get(position).isChecked());

            if (holder.tv_filteroption.isSelected()) {
                holder.ll_mainlayout.setBackgroundColor(getResources().getColor(R.color.white));
                holder.imv_filtericon.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                holder.tv_filteroption.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.imv_filtericon.setImageDrawable(context.getResources().getDrawable(filterOptionsList.get(position).getFilterIcon()));
                holder.tv_filteroption.setText(filterOptionsList.get(position).getFilterName());
            } else {
                holder.ll_mainlayout.setBackgroundColor(getResources().getColor(R.color.lightgray));
                holder.imv_filtericon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
                holder.tv_filteroption.setTextColor(getResources().getColor(R.color.grey));
                holder.imv_filtericon.setImageDrawable(context.getResources().getDrawable(filterOptionsList.get(position).getFilterIcon()));
                holder.tv_filteroption.setText(filterOptionsList.get(position).getFilterName());
            }
        }

        @Override
        public int getItemCount() {
            return filterOptionsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_filteroption;
            private ImageView imv_filtericon;
            private LinearLayout ll_mainlayout;

            public MyViewHolder(View view) {
                super(view);
                tv_filteroption = view.findViewById(R.id.tv_filteroption);
                imv_filtericon = view.findViewById(R.id.imv_filtericon);
                ll_mainlayout = view.findViewById(R.id.ll_mainlayout);
            }
        }
    }

    public class GetFamilyCodeList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllFamilyCode"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.CLIENTAPI, param);
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    familyCodeList = new ArrayList<>();
                    rv_filtervalue.setAdapter(new FamilyCodeListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                FamilyCodePojo summary = new FamilyCodePojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                if (!jsonObj.getString("code").equals("")) {
                                    summary.setId(jsonObj.getString("id"));
                                    summary.setCode(jsonObj.getString("code"));
                                    familyCodeList.add(summary);
                                }
                            }
                            rv_filtervalue.setAdapter(new FamilyCodeListAdapter());
                        }
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class FamilyCodeListAdapter extends RecyclerView.Adapter<FamilyCodeListAdapter.MyViewHolder> {

        private FamilyCodeListAdapter() {

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            if (familyCodeList.get(position).isChecked()) {
                holder.cb_check.setChecked(true);
            }

            holder.tv_itemname.setText(familyCodeList.get(position).getCode());

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        familyCodeList.get(position).setChecked(true);
                    else
                        familyCodeList.get(position).setChecked(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return familyCodeList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private CheckBox cb_check;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
            }
        }

    }

    public class GetPolicyStatusList extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllPolicyStatus"));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
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
                    policyStatusList = new ArrayList<>();
                    rv_filtervalue.setAdapter(new PolicyStatusAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                PolicyStatusListPojo policyStatusMainObj = new PolicyStatusListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                policyStatusMainObj.setId(jsonObj.getString("id"));
                                policyStatusMainObj.setStatus(jsonObj.getString("status"));
                                policyStatusList.add(policyStatusMainObj);
                            }
                            rv_filtervalue.setAdapter(new PolicyStatusAdapter());
                        }
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class PolicyStatusAdapter extends RecyclerView.Adapter<PolicyStatusAdapter.MyViewHolder> {

        private PolicyStatusAdapter() {

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            if (policyStatusList.get(position).isChecked()) {
                holder.cb_check.setChecked(true);
            }

            holder.tv_itemname.setText(policyStatusList.get(position).getStatus());

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        policyStatusList.get(position).setChecked(true);
                    else
                        policyStatusList.get(position).setChecked(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return policyStatusList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private CheckBox cb_check;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
            }
        }

    }

    public class GetFrequenctList extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllFreq"));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
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
                    frequencyList = new ArrayList<>();
                    rv_filtervalue.setAdapter(new FrequencyAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                FrequencyListPojo frequencyMainObj = new FrequencyListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);

                                frequencyMainObj.setId(jsonObj.getString("id"));
                                frequencyMainObj.setFrequency(jsonObj.getString("frequency"));
                                frequencyList.add(frequencyMainObj);

                            }
                            rv_filtervalue.setAdapter(new FrequencyAdapter());
                        }
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class FrequencyAdapter extends RecyclerView.Adapter<FrequencyAdapter.MyViewHolder> {

        private FrequencyAdapter() {
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            if (frequencyList.get(position).isChecked()) {
                holder.cb_check.setChecked(true);
            }

            holder.tv_itemname.setText(frequencyList.get(position).getFrequency());

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        frequencyList.get(position).setChecked(true);
                    else
                        frequencyList.get(position).setChecked(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return policyStatusList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private CheckBox cb_check;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
            }
        }

    }


}
