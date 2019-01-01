package com.insurance.todojee.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.insurance.todojee.fragments.Filter_Fragment;
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

    private ArrayList<LifeGeneralInsuranceMainListPojo> insuranceList, noneRepeatInsuranceList;

    private ArrayList<FamilyCodePojo> selectedFamilyCodeList;
    private ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> selectedClientFamilyList;
    private ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> selectedClientFirmList;
    private ArrayList<InsuranceTypeListPojo> selectedInsuranceTypeList;
    private ArrayList<PolicyStatusListPojo> selectedPolicyStatusList;
    private ArrayList<FrequencyListPojo> selectedFrequencyList;
    private ArrayList<PolicyTypeListPojo.Policy_details> selectedPolicyTypeList;

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

        selectedFamilyCodeList = constantData.getFamilyCodeList();
        selectedClientFamilyList = constantData.getClientFamilyList();
        selectedClientFirmList = constantData.getClientFirmList();
        selectedInsuranceTypeList = constantData.getInsuranceTypeList();
        selectedPolicyStatusList = constantData.getPolicyStatusList();
        selectedFrequencyList = constantData.getFrequencyList();
        selectedPolicyTypeList = constantData.getPolicyTypeList();

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

                                LifeGeneralInsuranceMainListPojo insuranceMainObj = new LifeGeneralInsuranceMainListPojo();

                                insuranceMainObj.setId(jsonObj.getString("id"));
                                insuranceMainObj.setInsurance_type_id(jsonObj.getString("insurance_type"));
                                insuranceMainObj.setInsurance_company_id(jsonObj.getString("insurance_company"));
                                insuranceMainObj.setInsurance_company_alias(jsonObj.getString("company_alias"));
                                insuranceMainObj.setInsurance_company_name(jsonObj.getString("company_name"));
                                insuranceMainObj.setClient_id(jsonObj.getString("name"));
                                insuranceMainObj.setClient_name(jsonObj.getString("client_name"));
                                insuranceMainObj.setInsurer_type_id(jsonObj.getString("insurer_name_type"));
                                insuranceMainObj.setInsurer_id(jsonObj.getString("insurer_name_id"));
                                insuranceMainObj.setInsurer_family_name(jsonObj.getString("insurer_relation_name"));
                                insuranceMainObj.setInsurer_firm_name(jsonObj.getString("insurer_firm_name"));
                                insuranceMainObj.setPolicy_no(jsonObj.getString("lic_police_no"));
                                insuranceMainObj.setPolicy_type_id(jsonObj.getString("police_type"));
                                insuranceMainObj.setPolicy_type(jsonObj.getString("policy_type"));
                                insuranceMainObj.setStart_date(jsonObj.getString("start_date"));
                                insuranceMainObj.setEnd_date(jsonObj.getString("end_date"));
                                insuranceMainObj.setFrequency_id(jsonObj.getString("frequency_id"));
                                insuranceMainObj.setFrequency(jsonObj.getString("frequency"));
                                insuranceMainObj.setSum_insured(jsonObj.getString("sum_insured"));
                                insuranceMainObj.setPremium_amount(jsonObj.getString("premium_amount"));
                                insuranceMainObj.setPolicy_status_id(jsonObj.getString("policy_status"));
                                insuranceMainObj.setPolicy_status(jsonObj.getString("policyStatus"));
                                insuranceMainObj.setFamily_code_id(jsonObj.getString("family_code_id"));
                                insuranceMainObj.setLink(jsonObj.getString("link"));
                                insuranceMainObj.setRemark(jsonObj.getString("remark"));
                                insuranceMainObj.setDescription(jsonObj.getString("description"));

                                ArrayList<LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo> maturityDatesList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("maturity_date").length(); j++) {
                                    LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo maturityDateObj = new LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo();
                                    maturityDateObj.setMaturity_date(jsonObj.getJSONArray("maturity_date").getJSONObject(j).getString("maturity_date"));
                                    maturityDateObj.setRemark(jsonObj.getJSONArray("maturity_date").getJSONObject(j).getString("remark"));
                                    maturityDatesList.add(maturityDateObj);
                                }
                                insuranceMainObj.setMaturity_date(maturityDatesList);


                                ArrayList<LifeGeneralInsuranceMainListPojo.DocumentListPojo> documentsList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("document").length(); j++) {
                                    LifeGeneralInsuranceMainListPojo.DocumentListPojo documentObj = new LifeGeneralInsuranceMainListPojo.DocumentListPojo();
                                    documentObj.setDocument(jsonObj.getJSONArray("document").getJSONObject(j).getString("document"));
                                    documentsList.add(documentObj);
                                }
                                insuranceMainObj.setDocument(documentsList);

                                insuranceList.add(insuranceMainObj);
                            }

                            filterInsuranceList();

//                            for (PolicyTypeListPojo policyTypeObj : selectedPolicyTypeList) {
//                                for (int i = 0; i < policyTypeObj.getPolicy_details().size(); i++) {
//                                    for (LifeGeneralInsuranceMainListPojo insuranceObj : insuranceList) {
//                                        if (insuranceObj.getPolicy_type_id().equals(policyTypeObj.getPolicy_details().get(i).getId())) {
//                                            filteredInsuranceList.add(insuranceObj);
//                                        }
//                                    }
//                                }
//                            }


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

    private void filterInsuranceList() {

        ArrayList<LifeGeneralInsuranceMainListPojo> filteredInsuranceList1 = new ArrayList<>();

        if (selectedFamilyCodeList.size() != 0) {
            for (FamilyCodePojo familyCodeObj : selectedFamilyCodeList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : insuranceList) {
                    if (insuranceObj.getFamily_code_id().equals(familyCodeObj.getId())) {
                        filteredInsuranceList1.add(insuranceObj);
                    }
                }
            }
        } else {
            filteredInsuranceList1.addAll(insuranceList);
        }


        ArrayList<LifeGeneralInsuranceMainListPojo> filteredInsuranceList2 = new ArrayList<>();

        if (selectedInsuranceTypeList.size() != 0) {
            for (InsuranceTypeListPojo insuranceTypeObj : selectedInsuranceTypeList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList1) {
                    if (insuranceObj.getInsurance_type_id().equals(insuranceTypeObj.getId())) {
                        filteredInsuranceList2.add(insuranceObj);
                    }
                }
            }
        } else {
            filteredInsuranceList2.addAll(filteredInsuranceList1);
        }


        ArrayList<LifeGeneralInsuranceMainListPojo> filteredInsuranceList3 = new ArrayList<>();

        if (selectedPolicyStatusList.size() != 0) {
            for (PolicyStatusListPojo policyStatusObj : selectedPolicyStatusList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList2) {
                    if (insuranceObj.getPolicy_status_id().equals(policyStatusObj.getId())) {
                        filteredInsuranceList3.add(insuranceObj);
                    }
                }
            }
        } else {
            filteredInsuranceList3.addAll(filteredInsuranceList2);
        }

        ArrayList<LifeGeneralInsuranceMainListPojo> filteredInsuranceList4 = new ArrayList<>();

        if (selectedFrequencyList.size() != 0) {
            for (FrequencyListPojo frequencyObj : selectedFrequencyList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList3) {
                    if (insuranceObj.getFrequency_id().equals(frequencyObj.getId())) {
                        filteredInsuranceList4.add(insuranceObj);
                    }
                }
            }
        } else {
            filteredInsuranceList4.addAll(filteredInsuranceList3);
        }

        ArrayList<LifeGeneralInsuranceMainListPojo> filteredInsuranceList5 = new ArrayList<>();

        if (selectedPolicyTypeList.size() != 0) {
            for (PolicyTypeListPojo.Policy_details policyTypeObj : selectedPolicyTypeList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList4) {
                    if (insuranceObj.getPolicy_type_id().equals(policyTypeObj.getId())) {
                        filteredInsuranceList5.add(insuranceObj);
                    }
                }

            }
        } else {
            filteredInsuranceList5.addAll(filteredInsuranceList4);
        }

        ArrayList<LifeGeneralInsuranceMainListPojo> filteredInsuranceList6 = new ArrayList<>();

        if (selectedClientFamilyList.size() != 0 || selectedClientFirmList.size() != 0) {

//            if (selectedClientFamilyList.size() != 0) {
            for (ClientMainListPojo.ClientFamilyDetailsPojo familyObj : selectedClientFamilyList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList5) {
                    if (insuranceObj.getInsurer_type_id().equals("R")) {
                        if (insuranceObj.getInsurer_id().equals(familyObj.getFamily_details_id())) {
                            filteredInsuranceList6.add(insuranceObj);
                        }
                    }
                }
            }
//            }

//            if (selectedClientFirmList.size() != 0) {
            for (ClientMainListPojo.ClientFirmDetailsPojo firmObj : selectedClientFirmList) {
                for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList5) {
                    if (insuranceObj.getInsurer_type_id().equals("F")) {
                        if (insuranceObj.getInsurer_id().equals(firmObj.getFirm_id())) {
                            filteredInsuranceList6.add(insuranceObj);
                        }
                    }
                }
            }
//            }
        } else {
            filteredInsuranceList6.addAll(filteredInsuranceList5);
        }


        noneRepeatInsuranceList = new ArrayList<>();

        for (LifeGeneralInsuranceMainListPojo insuranceObj : filteredInsuranceList6) {
            boolean isFound = false;
            for (LifeGeneralInsuranceMainListPojo e : noneRepeatInsuranceList) {
                if ((e.equals(insuranceObj))) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) noneRepeatInsuranceList.add(insuranceObj);
        }


        if (noneRepeatInsuranceList.size() == 0) {
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_insurancelist.setVisibility(View.GONE);
        } else {
            rv_insurancelist.setVisibility(View.VISIBLE);
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_insurancelist.setAdapter(new InsuranceListAdapter());
        }
    }

    public class InsuranceListAdapter extends RecyclerView.Adapter<InsuranceListAdapter.MyViewHolder> {

        private InsuranceListAdapter() {

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
            insuranceDetails = noneRepeatInsuranceList.get(position);
            final LifeGeneralInsuranceMainListPojo finalInsuranceDetails = insuranceDetails;
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

            holder.ll_mainlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalInsuranceDetails.getInsurance_type_id().equals("1")) {
                        Intent intent = new Intent(context, ViewGeneralInsurance_Activity.class);
                        intent.putExtra("TYPE", "FILTER");
                        intent.putExtra("generalInsuranceDetails", finalInsuranceDetails);
                        context.startActivity(intent);
                    } else if (finalInsuranceDetails.getInsurance_type_id().equals("2")) {
                        Intent intent = new Intent(context, ViewLifeInsurance_Activity.class);
                        intent.putExtra("TYPE", "FILTER");
                        intent.putExtra("lifeInsuranceDetails", finalInsuranceDetails);
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return noneRepeatInsuranceList.size();
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
        constantData.setClientFirmList(new ArrayList<>());
        constantData.setClientFamilyList(new ArrayList<>());
        constantData.setInsuranceTypeList(new ArrayList<>());
        constantData.setFrequencyList(new ArrayList<>());
        constantData.setPolicyTypeList(new ArrayList<>());
        constantData.setPolicyStatusList(new ArrayList<>());

        Filter_Fragment.selectedFamilyCodeList = new ArrayList<>();
        Filter_Fragment.selectedClientFamilyList = new ArrayList<>();
        Filter_Fragment.selectedClientFirmList = new ArrayList<>();
        Filter_Fragment.selectedInsuranceTypeList = new ArrayList<>();
        Filter_Fragment.selectedPolicyStatusList = new ArrayList<>();
        Filter_Fragment.selectedFrequencyList = new ArrayList<>();
        Filter_Fragment.selectedPolicyTypeList = new ArrayList<>();
    }
}
