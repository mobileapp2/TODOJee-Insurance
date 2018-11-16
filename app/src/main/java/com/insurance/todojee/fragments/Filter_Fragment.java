package com.insurance.todojee.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.activities.FilteredInsuranceList_Activity;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.models.FamilyCodePojo;
import com.insurance.todojee.models.FilterOptionsListPojo;
import com.insurance.todojee.models.FrequencyListPojo;
import com.insurance.todojee.models.InsuranceTypeListPojo;
import com.insurance.todojee.models.PolicyStatusListPojo;
import com.insurance.todojee.models.PolicyTypeListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ConstantData;
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
    private Button btn_filter, btn_clear;
    private LinearLayoutManager layoutManager1, layoutManager2;
    private UserSessionManager session;
    private String user_id;
    private ArrayList<FilterOptionsListPojo> filterOptionsList;
    private FilterOptionListAdaper filterAdaper;
    private int globalPosition = 0;
    private String insurerType = "";

    public static ArrayList<FamilyCodePojo> familyCodeList, selectedFamilyCodeList;

    public static ArrayList<ClientMainListPojo> clientList;
    public static ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> selectedClientFamilyList;
    public static ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> selectedClientFirmList;

    public static ArrayList<InsuranceTypeListPojo> insuranceTypeList, selectedInsuranceTypeList;

    public static ArrayList<PolicyStatusListPojo> policyStatusList, selectedPolicyStatusList;

    public static ArrayList<FrequencyListPojo> frequencyList, selectedFrequencyList;

    public static ArrayList<PolicyTypeListPojo> policyTypeList;
    public static ArrayList<PolicyTypeListPojo.Policy_details> selectedPolicyTypeList;

    private ConstantData constantData;

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
        constantData = ConstantData.getInstance();
        rv_filteroption = rootView.findViewById(R.id.rv_filteroption);
        rv_filtervalue = rootView.findViewById(R.id.rv_filtervalue);
        btn_filter = rootView.findViewById(R.id.btn_filter);
        btn_clear = rootView.findViewById(R.id.btn_clear);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
        layoutManager1 = new LinearLayoutManager(context);
        layoutManager2 = new LinearLayoutManager(context);
        rv_filteroption.setLayoutManager(layoutManager1);
        rv_filtervalue.setLayoutManager(layoutManager2);

        filterAdaper = new FilterOptionListAdaper();
        filterOptionsList = new ArrayList<>();

        familyCodeList = new ArrayList<>();
        clientList = new ArrayList<>();
        policyStatusList = new ArrayList<>();
        frequencyList = new ArrayList<>();
        policyTypeList = new ArrayList<>();
        insuranceTypeList = new ArrayList<>();

        selectedFamilyCodeList = new ArrayList<>();
        selectedClientFamilyList = new ArrayList<>();
        selectedClientFirmList = new ArrayList<>();
        selectedInsuranceTypeList = new ArrayList<>();
        selectedPolicyStatusList = new ArrayList<>();
        selectedFrequencyList = new ArrayList<>();
        selectedPolicyTypeList = new ArrayList<>();
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
        String[] filterOptions = {"Family Code", "Insurance Type", "Family Insurer", "Firm Insurer", "Policy Type", "Frequency", "Policy Status"};
        int[] filterIcons = {R.drawable.icon_familycode, R.drawable.icon_insurancetype, R.drawable.icon_insurer, R.drawable.icon_insurancecompany,
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

        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (FamilyCodePojo familyCodeObj : familyCodeList) {
                    if (familyCodeObj.isChecked()) {
                        selectedFamilyCodeList.add(familyCodeObj);
                    }
                }

                for (int i = 0; i < clientList.size(); i++) {
                    for (int j = 0; j < clientList.get(i).getRelation_details().size(); j++) {
                        if (clientList.get(i).getRelation_details().get(j).isChecked()) {
                            selectedClientFamilyList.add(clientList.get(i).getRelation_details().get(j));
                        }
                    }
                }

                for (int i = 0; i < clientList.size(); i++) {
                    for (int j = 0; j < clientList.get(i).getFirm_details().size(); j++) {
                        if (clientList.get(i).getFirm_details().get(j).isChecked()) {
                            selectedClientFirmList.add(clientList.get(i).getFirm_details().get(j));
                        }
                    }
                }

                for (InsuranceTypeListPojo insuranceTypeObj : insuranceTypeList) {
                    if (insuranceTypeObj.isChecked()) {
                        selectedInsuranceTypeList.add(insuranceTypeObj);
                    }
                }

                for (PolicyStatusListPojo policyTypeObj : policyStatusList) {
                    if (policyTypeObj.isChecked()) {
                        selectedPolicyStatusList.add(policyTypeObj);
                    }
                }

                for (FrequencyListPojo frequencyObj : frequencyList) {
                    if (frequencyObj.isChecked()) {
                        selectedFrequencyList.add(frequencyObj);
                    }
                }

                for (int i = 0; i < policyTypeList.size(); i++) {
                    for (int j = 0; j < policyTypeList.get(i).getPolicy_details().size(); j++) {
                        if (policyTypeList.get(i).getPolicy_details().get(j).isChecked()) {
                            selectedPolicyTypeList.add(policyTypeList.get(i).getPolicy_details().get(j));
                        }
                    }
                }


                if (selectedFamilyCodeList.size() == 0 && selectedClientFamilyList.size() == 0 && selectedClientFirmList.size() == 0
                        && selectedInsuranceTypeList.size() == 0 && selectedFrequencyList.size() == 0 && selectedPolicyTypeList.size() == 0
                        && selectedPolicyStatusList.size() == 0) {
                    Utilities.showMessageString(context, "Please select atleast one type of filter");
                } else {
                    constantData.setFamilyCodeList(selectedFamilyCodeList);
                    constantData.setClientFamilyList(selectedClientFamilyList);
                    constantData.setClientFirmList(selectedClientFirmList);
                    constantData.setInsuranceTypeList(selectedInsuranceTypeList);
                    constantData.setFrequencyList(selectedFrequencyList);
                    constantData.setPolicyTypeList(selectedPolicyTypeList);
                    constantData.setPolicyStatusList(selectedPolicyStatusList);
                    startActivity(new Intent(context, FilteredInsuranceList_Activity.class));
                }

            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (int i = 0; i < familyCodeList.size(); i++) {
                    familyCodeList.get(i).setChecked(false);
                }
                for (int i = 0; i < clientList.size(); i++) {
                    for (int j = 0; j < clientList.get(i).getRelation_details().size(); j++)
                        clientList.get(i).getRelation_details().get(j).setChecked(false);
                }
                for (int i = 0; i < clientList.size(); i++) {
                    for (int j = 0; j < clientList.get(i).getFirm_details().size(); j++)
                        clientList.get(i).getFirm_details().get(j).setChecked(false);
                }
                for (int i = 0; i < policyStatusList.size(); i++) {
                    policyStatusList.get(i).setChecked(false);
                }
                for (int i = 0; i < frequencyList.size(); i++) {
                    frequencyList.get(i).setChecked(false);
                }
                for (int i = 0; i < insuranceTypeList.size(); i++) {
                    insuranceTypeList.get(i).setChecked(false);
                }
                for (int i = 0; i < policyTypeList.size(); i++) {
                    for (int j = 0; j < policyTypeList.get(i).getPolicy_details().size(); j++)
                        policyTypeList.get(i).getPolicy_details().get(j).setChecked(false);
                }

                loadFilterItemsList();
            }
        });

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

            if (insuranceTypeList.size() == 0) {
                String[] id = {"2", "1"};
                String[] insuranceType = {"Life Insurance", "General Insurance"};
                boolean[] isChecked = {false, false};

                for (int i = 0; i < insuranceType.length; i++) {
                    insuranceTypeList.add(new InsuranceTypeListPojo(id[i], insuranceType[i], isChecked[i]));
                }
                rv_filtervalue.setAdapter(new InsuranceTypeAdapter());
            } else {
                rv_filtervalue.setAdapter(new InsuranceTypeAdapter());
            }

        } else if (globalPosition == 2) {
            insurerType = "R";
            if (clientList.size() == 0) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetClientList().execute(user_id);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                rv_filtervalue.setAdapter(new ClientListAdapter());
            }

        } else if (globalPosition == 3) {
            insurerType = "F";
            if (clientList.size() == 0) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetClientList().execute(user_id);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                rv_filtervalue.setAdapter(new ClientListAdapter());
            }

        } else if (globalPosition == 4) {

            if (policyTypeList.size() == 0) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetPolicyTypeList().execute(user_id);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                rv_filtervalue.setAdapter(new ParentPolicyTypeListAdapter());
            }


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


    public class GetClientList extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;


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
            param.add(new ParamsPojo("type", "getAllLIClients"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.CLIENTAPI, param);
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
                    clientList = new ArrayList<ClientMainListPojo>();
                    rv_filtervalue.setAdapter(new ClientListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                ClientMainListPojo clientMainObj = new ClientMainListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                clientMainObj.setId(jsonObj.getString("id"));
                                clientMainObj.setFirst_name(jsonObj.getString("first_name"));

                                ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> familyDetailsList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("relation_details").length(); j++) {
                                    ClientMainListPojo.ClientFamilyDetailsPojo clientFamilyObj = new ClientMainListPojo.ClientFamilyDetailsPojo();
                                    clientFamilyObj.setFamily_details_id(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("family_details_id"));
                                    clientFamilyObj.setName(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("name"));
                                    familyDetailsList.add(clientFamilyObj);
                                }
                                clientMainObj.setRelation_details(familyDetailsList);

                                ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> firmDetailsList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("firm_details").length(); j++) {
                                    ClientMainListPojo.ClientFirmDetailsPojo clientFirmObj = new ClientMainListPojo.ClientFirmDetailsPojo();
                                    clientFirmObj.setFirm_id(jsonObj.getJSONArray("firm_details").getJSONObject(j).getString("firm_id"));
                                    clientFirmObj.setFirm_name(jsonObj.getJSONArray("firm_details").getJSONObject(j).getString("firm_name"));
                                    firmDetailsList.add(clientFirmObj);
                                }

                                clientMainObj.setFirm_details(firmDetailsList);

                                clientList.add(clientMainObj);
                            }
                            rv_filtervalue.setAdapter(new ClientListAdapter());

                        }
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.MyViewHolder> {

        private ClientListAdapter() {

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_parentpolicytype, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
//            if (clientList.get(position).isChecked()) {
//                holder.cb_check.setChecked(true);
//            }
//
//            holder.tv_itemname.setText(clientList.get(position).getFirst_name());
//
//            holder.cb_check.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (holder.cb_check.isChecked())
//                        clientList.get(position).setChecked(true);
//                    else
//                        clientList.get(position).setChecked(false);
//                }
//            });

            if (insurerType.equals("R")) {
                holder.tv_companyname.setText(clientList.get(position).getFirst_name());

                if (clientList.get(position).getRelation_details().size() != 0) {
                    holder.tv_nopolicytype.setVisibility(View.GONE);
                    holder.rv_policytypelist.setVisibility(View.VISIBLE);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                    holder.rv_policytypelist.setLayoutManager(layoutManager);
                    holder.rv_policytypelist.setAdapter(new FamilyInsurerListAdapter(position));
                } else {
                    holder.tv_nopolicytype.setVisibility(View.VISIBLE);
                    holder.rv_policytypelist.setVisibility(View.GONE);
                }
            } else if (insurerType.equals("F")) {
                holder.tv_companyname.setText(clientList.get(position).getFirst_name());

                if (clientList.get(position).getRelation_details().size() != 0) {
                    holder.tv_nopolicytype.setVisibility(View.GONE);
                    holder.rv_policytypelist.setVisibility(View.VISIBLE);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                    holder.rv_policytypelist.setLayoutManager(layoutManager);
                    holder.rv_policytypelist.setAdapter(new FirmInsurerListAdapter(position));
                } else {
                    holder.tv_nopolicytype.setVisibility(View.VISIBLE);
                    holder.rv_policytypelist.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return clientList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_companyname, tv_nopolicytype;
            private RecyclerView rv_policytypelist;


            public MyViewHolder(View view) {
                super(view);
                tv_companyname = view.findViewById(R.id.tv_companyname);
                tv_nopolicytype = view.findViewById(R.id.tv_nopolicytype);
                rv_policytypelist = view.findViewById(R.id.rv_policytypelist);
            }
        }

    }

    public class FamilyInsurerListAdapter extends RecyclerView.Adapter<FamilyInsurerListAdapter.MyViewHolder> {
        private int position;

        public FamilyInsurerListAdapter(int position) {
            this.position = position;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int pos) {
            if (clientList.get(position).getRelation_details().get(pos).isChecked()) {
                holder.cb_check.setChecked(true);
            }


            holder.tv_itemname.setText(clientList.get(position).getRelation_details().get(pos).getName());

            if (pos == clientList.get(position).getRelation_details().size() - 1) {
                holder.view1.setVisibility(View.GONE);
            }

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        clientList.get(position).getRelation_details().get(pos).setChecked(true);
                    else
                        clientList.get(position).getRelation_details().get(pos).setChecked(false);
                }
            });

        }

        @Override
        public int getItemCount() {
            return clientList.get(position).getRelation_details().size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private View view1;
            private CheckBox cb_check;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
                view1 = view.findViewById(R.id.view);
            }
        }
    }

    public class FirmInsurerListAdapter extends RecyclerView.Adapter<FirmInsurerListAdapter.MyViewHolder> {
        private int position;

        public FirmInsurerListAdapter(int position) {
            this.position = position;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int pos) {
            if (clientList.get(position).getFirm_details().get(pos).isChecked()) {
                holder.cb_check.setChecked(true);
            }


            holder.tv_itemname.setText(clientList.get(position).getFirm_details().get(pos).getFirm_name());

            if (pos == clientList.get(position).getFirm_details().size() - 1) {
                holder.view1.setVisibility(View.GONE);
            }

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        clientList.get(position).getFirm_details().get(pos).setChecked(true);
                    else
                        clientList.get(position).getFirm_details().get(pos).setChecked(false);
                }
            });

        }

        @Override
        public int getItemCount() {
            return clientList.get(position).getFirm_details().size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private View view1;
            private CheckBox cb_check;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
                view1 = view.findViewById(R.id.view);
            }
        }
    }


    public class InsuranceTypeAdapter extends RecyclerView.Adapter<InsuranceTypeAdapter.MyViewHolder> {

        private InsuranceTypeAdapter() {
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
            if (insuranceTypeList.get(position).isChecked()) {
                holder.cb_check.setChecked(true);
            }

            holder.tv_itemname.setText(insuranceTypeList.get(position).getInsuranceType());

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        insuranceTypeList.get(position).setChecked(true);
                    else
                        insuranceTypeList.get(position).setChecked(false);
                }
            });
        }

        @Override
        public int getItemCount() {
            return insuranceTypeList.size();
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


    public class GetPolicyTypeList extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;

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
            param.add(new ParamsPojo("type", "getAllLICTYpe"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("company_id", "-1"));
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
                    policyTypeList = new ArrayList<>();
                    rv_filtervalue.setAdapter(new ParentPolicyTypeListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                PolicyTypeListPojo policyTypeMainObj = new PolicyTypeListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                policyTypeMainObj.setId(jsonObj.getString("id"));
                                policyTypeMainObj.setCompany_name(jsonObj.getString("company_name"));
                                policyTypeMainObj.setInsurance_type(jsonObj.getString("insurance_type"));

                                ArrayList<PolicyTypeListPojo.Policy_details> policyTypesList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("policy_details").length(); j++) {

                                    if (!jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("type").equals("")) {
                                        PolicyTypeListPojo.Policy_details policyTypeObj = new PolicyTypeListPojo.Policy_details();
                                        policyTypeObj.setId(jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("id"));
                                        policyTypeObj.setType(jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("type"));
                                        policyTypeObj.setAlias(jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("alias"));
                                        policyTypesList.add(policyTypeObj);
                                    }
                                }
                                policyTypeMainObj.setPolicy_details(policyTypesList);
                                policyTypeList.add(policyTypeMainObj);


                            }
                            rv_filtervalue.setAdapter(new ParentPolicyTypeListAdapter());
                        }
                    } else {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ParentPolicyTypeListAdapter extends RecyclerView.Adapter<ParentPolicyTypeListAdapter.MyViewHolder> {

        private ParentPolicyTypeListAdapter() {
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_parentpolicytype, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            holder.tv_companyname.setText(policyTypeList.get(position).getCompany_name());

            if (policyTypeList.get(position).getPolicy_details().size() != 0) {
                holder.tv_nopolicytype.setVisibility(View.GONE);
                holder.rv_policytypelist.setVisibility(View.VISIBLE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                holder.rv_policytypelist.setLayoutManager(layoutManager);
//                holder.rv_policytypelist.setAdapter(new ChildPolicyTypeListAdapter(policyTypeList.get(position).getPolicy_details()));
                holder.rv_policytypelist.setAdapter(new ChildPolicyTypeListAdapter(position));
            } else {
                holder.tv_nopolicytype.setVisibility(View.VISIBLE);
                holder.rv_policytypelist.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return policyTypeList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_companyname, tv_nopolicytype;
            private RecyclerView rv_policytypelist;


            public MyViewHolder(View view) {
                super(view);
                tv_companyname = view.findViewById(R.id.tv_companyname);
                tv_nopolicytype = view.findViewById(R.id.tv_nopolicytype);
                rv_policytypelist = view.findViewById(R.id.rv_policytypelist);
            }
        }
    }

    public class ChildPolicyTypeListAdapter extends RecyclerView.Adapter<ChildPolicyTypeListAdapter.MyViewHolder> {
        private int position;

        public ChildPolicyTypeListAdapter(int position) {
            this.position = position;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int pos) {
            if (policyTypeList.get(position).getPolicy_details().get(pos).isChecked()) {
                holder.cb_check.setChecked(true);
            }


            holder.tv_itemname.setText(policyTypeList.get(position).getPolicy_details().get(pos).getType());

            if (pos == policyTypeList.get(position).getPolicy_details().size() - 1) {
                holder.view1.setVisibility(View.GONE);
            }

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.cb_check.isChecked())
                        policyTypeList.get(position).getPolicy_details().get(pos).setChecked(true);
                    else
                        policyTypeList.get(position).getPolicy_details().get(pos).setChecked(false);
                }
            });

        }

        @Override
        public int getItemCount() {
            return policyTypeList.get(position).getPolicy_details().size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private View view1;
            private CheckBox cb_check;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
                view1 = view.findViewById(R.id.view);
            }
        }
    }


    public class GetFrequenctList extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;

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
            return frequencyList.size();
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
        private ProgressDialog pd;

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


}
