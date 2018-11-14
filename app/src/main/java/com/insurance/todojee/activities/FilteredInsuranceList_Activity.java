package com.insurance.todojee.activities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.models.FamilyCodePojo;
import com.insurance.todojee.models.FrequencyListPojo;
import com.insurance.todojee.models.InsuranceTypeListPojo;
import com.insurance.todojee.models.LifeGeneralInsuranceMainListPojo;
import com.insurance.todojee.models.PolicyStatusListPojo;
import com.insurance.todojee.models.PolicyTypeListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ConstantData;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FilteredInsuranceList_Activity extends Activity {

    public LinearLayout ll_parent;
    private Context context;
    private RecyclerView rv_insurancelist;
    private LinearLayout ll_nothingtoshow;
    private LinearLayoutManager layoutManager;
    private UserSessionManager session;
    private String user_id;

    private ArrayList<LifeGeneralInsuranceMainListPojo> insuranceList, filteredInsuranceList;

    private ArrayList<FamilyCodePojo> familyCodeList, selectedFamilyCodeList;
    private ArrayList<ClientMainListPojo> clientList, selectedClientList;
    private ArrayList<InsuranceTypeListPojo> insuranceTypeList, selectedInsuranceTypeList;
    private ArrayList<PolicyStatusListPojo> policyStatusList, selectedPolicyStatusList;
    private ArrayList<FrequencyListPojo> frequencyList, selectedFrequencyList;
    private ArrayList<PolicyTypeListPojo> policyTypeList, selectedPolicyTypeList;

    private ConstantData constantData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_policy_list);

        init();
        getSessionData();
        setDefault();
        setUpToolbar();

    }

    private void init() {
        context = FilteredInsuranceList_Activity.this;
        session = new UserSessionManager(context);
        constantData = ConstantData.getInstance();
        ll_nothingtoshow = findViewById(R.id.ll_nothingtoshow);
        rv_insurancelist = findViewById(R.id.rv_insurancelist);
        ll_parent = findViewById(R.id.ll_parent);
        layoutManager = new LinearLayoutManager(context);
        rv_insurancelist.setLayoutManager(layoutManager);

        insuranceList = new ArrayList<>();
        filteredInsuranceList = new ArrayList<>();

        familyCodeList = new ArrayList<>();
        clientList = new ArrayList<>();
        insuranceTypeList = new ArrayList<>();
        policyStatusList = new ArrayList<>();
        frequencyList = new ArrayList<>();
        policyTypeList = new ArrayList<>();

        selectedFamilyCodeList = new ArrayList<>();
        selectedClientList = new ArrayList<>();
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

        familyCodeList = constantData.getFamilyCodeList();
        clientList = constantData.getClientList();
        insuranceTypeList = constantData.getInsuranceTypeList();
        policyStatusList = constantData.getPolicyStatusList();
        frequencyList = constantData.getFrequencyList();
        policyTypeList = constantData.getPolicyTypeList();

        for (FamilyCodePojo familyCodeObj : familyCodeList) {
            if (familyCodeObj.isChecked()) {
                selectedFamilyCodeList.add(familyCodeObj);
            }
        }

        for (ClientMainListPojo clientObj : clientList) {
            if (clientObj.isChecked()) {
                selectedClientList.add(clientObj);
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
            PolicyTypeListPojo policyTypeObj = new PolicyTypeListPojo();
            policyTypeObj.setId(policyTypeList.get(i).getId());
            policyTypeObj.setCompany_name(policyTypeList.get(i).getCompany_name());
            for (int j = 0; j < policyTypeList.get(i).getPolicy_details().size(); j++) {
                if (policyTypeList.get(i).getPolicy_details().get(j).isChecked()) {
                    policyTypeObj.setPolicy_details(policyTypeList.get(i).getPolicy_details());
                }
                selectedPolicyTypeList.add(policyTypeObj);
            }
        }


        if (Utilities.isNetworkAvailable(context)) {
            new GetInsuranceList().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_insurancelist.setVisibility(View.GONE);
        }
    }

    public class GetInsuranceList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_insurancelist.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllLIC"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                rv_insurancelist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    insuranceList = new ArrayList<LifeGeneralInsuranceMainListPojo>();
                    rv_insurancelist.setAdapter(new InsuranceListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                JSONObject jsonObj = jsonarr.getJSONObject(i);

                                LifeGeneralInsuranceMainListPojo lifeInsuranceMainObj = new LifeGeneralInsuranceMainListPojo();

                                lifeInsuranceMainObj.setId(jsonObj.getString("id"));
                                lifeInsuranceMainObj.setInsurance_type_id(jsonObj.getString("insurance_type"));
                                lifeInsuranceMainObj.setInsurance_company_id(jsonObj.getString("insurance_company"));
                                lifeInsuranceMainObj.setInsurance_company_alias(jsonObj.getString("company_alias"));
                                lifeInsuranceMainObj.setInsurance_company_name(jsonObj.getString("company_name"));
                                lifeInsuranceMainObj.setClient_id(jsonObj.getString("name"));
                                lifeInsuranceMainObj.setClient_name(jsonObj.getString("client_name"));
                                lifeInsuranceMainObj.setInsurer_type_id(jsonObj.getString("insurer_name_type"));
                                lifeInsuranceMainObj.setInsurer_id(jsonObj.getString("insurer_name_id"));
                                lifeInsuranceMainObj.setInsurer_family_name(jsonObj.getString("insurer_relation_name"));
                                lifeInsuranceMainObj.setInsurer_firm_name(jsonObj.getString("insurer_firm_name"));
                                lifeInsuranceMainObj.setPolicy_no(jsonObj.getString("lic_police_no"));
                                lifeInsuranceMainObj.setPolicy_type_id(jsonObj.getString("police_type"));
                                lifeInsuranceMainObj.setPolicy_type(jsonObj.getString("policy_type"));
                                lifeInsuranceMainObj.setStart_date(jsonObj.getString("start_date"));
                                lifeInsuranceMainObj.setEnd_date(jsonObj.getString("end_date"));
                                lifeInsuranceMainObj.setFrequency_id(jsonObj.getString("frequency_id"));
                                lifeInsuranceMainObj.setFrequency(jsonObj.getString("frequency"));
                                lifeInsuranceMainObj.setSum_insured(jsonObj.getString("sum_insured"));
                                lifeInsuranceMainObj.setPremium_amount(jsonObj.getString("premium_amount"));
                                lifeInsuranceMainObj.setPolicy_status_id(jsonObj.getString("policy_status"));
                                lifeInsuranceMainObj.setPolicy_status(jsonObj.getString("policyStatus"));
                                lifeInsuranceMainObj.setFamily_code_id(jsonObj.getString("family_code_id"));
                                lifeInsuranceMainObj.setLink(jsonObj.getString("link"));
                                lifeInsuranceMainObj.setRemark(jsonObj.getString("remark"));
                                lifeInsuranceMainObj.setDescription(jsonObj.getString("description"));

                                ArrayList<LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo> maturityDatesList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("maturity_date").length(); j++) {
                                    LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo maturityDateObj = new LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo();
                                    maturityDateObj.setMaturity_date(jsonObj.getJSONArray("maturity_date").getJSONObject(j).getString("maturity_date"));
                                    maturityDateObj.setRemark(jsonObj.getJSONArray("maturity_date").getJSONObject(j).getString("remark"));
                                    maturityDatesList.add(maturityDateObj);
                                }
                                lifeInsuranceMainObj.setMaturity_date(maturityDatesList);


                                ArrayList<LifeGeneralInsuranceMainListPojo.DocumentListPojo> documentsList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("document").length(); j++) {
                                    LifeGeneralInsuranceMainListPojo.DocumentListPojo documentObj = new LifeGeneralInsuranceMainListPojo.DocumentListPojo();
                                    documentObj.setDocument(jsonObj.getJSONArray("document").getJSONObject(j).getString("document"));
                                    documentsList.add(documentObj);
                                }
                                lifeInsuranceMainObj.setDocument(documentsList);

                                insuranceList.add(lifeInsuranceMainObj);
                            }


                            for (FamilyCodePojo familyCodeObj : selectedFamilyCodeList) {
                                for (LifeGeneralInsuranceMainListPojo insuranceObj : insuranceList) {
                                    if (insuranceObj.getFamily_code_id().equals(familyCodeObj.getId())) {
                                        filteredInsuranceList.add(insuranceObj);
                                    }
                                }
                            }

                            if (filteredInsuranceList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_insurancelist.setVisibility(View.GONE);
                            } else {
                                rv_insurancelist.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                                rv_insurancelist.setAdapter(new InsuranceListAdapter());
                            }

                        }
                    } else if (type.equalsIgnoreCase("failure")) {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_insurancelist.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_insurancelist.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public class InsuranceListAdapter extends RecyclerView.Adapter<InsuranceListAdapter.MyViewHolder> {

        public InsuranceListAdapter() {

        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_insurance, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            LifeGeneralInsuranceMainListPojo insuranceDetails = new LifeGeneralInsuranceMainListPojo();
            insuranceDetails = filteredInsuranceList.get(position);
            final LifeGeneralInsuranceMainListPojo finalIifeInsuranceDetails = insuranceDetails;
            String insurerName = "";
            String InsurerTypeId = insuranceDetails.getInsurer_type_id();
            if (InsurerTypeId.equals("F")) {
                insurerName = (insuranceDetails.getInsurer_firm_name().trim().equals("")) ? "-" : insuranceDetails.getInsurer_firm_name();
            } else if (InsurerTypeId.equals("R")) {
                insurerName = (insuranceDetails.getInsurer_family_name().trim().equals("")) ? "-" : insuranceDetails.getInsurer_family_name();
            }

            String policyNo = (insuranceDetails.getPolicy_no().equals("")) ? "-" : insuranceDetails.getPolicy_no();
            String insuranceCompanyAlias = (insuranceDetails.getInsurance_company_alias().equals("")) ? "-" : insuranceDetails.getInsurance_company_alias();
            String startDate = (insuranceDetails.getStart_date().equals("")) ? "-" : insuranceDetails.getStart_date();
            String endDate = (insuranceDetails.getEnd_date().equals("")) ? "-" : insuranceDetails.getEnd_date();
            String frequency = (insuranceDetails.getFrequency().equals("")) ? "-" : insuranceDetails.getFrequency();

            holder.tv_name.setText(insurerName + " | " + policyNo + " | " + insuranceCompanyAlias);
            holder.tv_startenddate.setText(startDate + " | " + endDate + " | " + frequency);

//            holder.ll_mainlayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(context, ViewGeneralInsurance_Activity.class);
//                    intent.putExtra("generalInsuranceDetails", finalIifeInsuranceDetails);
//                    context.startActivity(intent);
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return insuranceList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_name, tv_startenddate;
            private LinearLayout ll_mainlayout;

            public MyViewHolder(View view) {
                super(view);
                tv_name = view.findViewById(R.id.tv_name);
                tv_startenddate = view.findViewById(R.id.tv_startenddate);
                ll_mainlayout = view.findViewById(R.id.ll_mainlayout);
            }
        }
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Filtered Insurance Details");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        constantData.setFamilyCodeList(new ArrayList<>());
        constantData.setClientList(new ArrayList<>());
        constantData.setInsuranceTypeList(new ArrayList<>());
        constantData.setFrequencyList(new ArrayList<>());
        constantData.setPolicyTypeList(new ArrayList<>());
        constantData.setPolicyStatusList(new ArrayList<>());
    }
}
