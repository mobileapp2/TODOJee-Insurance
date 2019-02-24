package com.insurance.todojee.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.insurance.todojee.R;
import com.insurance.todojee.activities.AddLifeInsurance_Activity;
import com.insurance.todojee.adapters.GetLifeInsuranceListAdapter;
import com.insurance.todojee.models.LifeGeneralInsuranceMainListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LifeInsurance_Fragment extends Fragment {

    public static DrawerLayout drawerlayout;
    private static Context context;
    private static RecyclerView rv_lifeinsurancelist;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static LinearLayout ll_nothingtoshow;
    private FloatingActionButton fab_add_lifeinsurance;
    private LinearLayoutManager layoutManager;
    //    private static ShimmerFrameLayout shimmer_view_container;
    private UserSessionManager session;
    private String user_id;
    private SearchView searchView;
    private static ArrayList<LifeGeneralInsuranceMainListPojo> lifeInsuranceList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lifeinsurance, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_add_lifeinsurance = rootView.findViewById(R.id.fab_add_lifeinsurance);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_lifeinsurancelist = rootView.findViewById(R.id.rv_lifeinsurancelist);
        drawerlayout = getActivity().findViewById(R.id.drawerlayout);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        layoutManager = new LinearLayoutManager(context);
        rv_lifeinsurancelist.setLayoutManager(layoutManager);
        searchView = rootView.findViewById(R.id.searchView);
        searchView.setFocusable(false);
    }

    private void setDefault() {
        if (Utilities.isNetworkAvailable(getActivity())) {
            new GetLifeInsurance().execute(user_id);
        } else {
            Utilities.showSnackBar(drawerlayout, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_lifeinsurancelist.setVisibility(View.GONE);
        }
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

    private void setEventHandlers() {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return filterList(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return filterList(newText);
            }
        });

        fab_add_lifeinsurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray user_info = null;
                try {
                    user_info = new JSONArray(session.getUserDetails().get(
                            ApplicationConstants.KEY_LOGIN_INFO));
                    JSONObject json = user_info.getJSONObject(0);
                    if (Integer.parseInt(json.getString("policyCount")) < Integer.parseInt(json.getString("policyLimit"))) {
                        startActivity(new Intent(context, AddLifeInsurance_Activity.class));
                    } else {
                        Utilities.buildDialogForPolicyValidation(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetLifeInsurance().execute(user_id);
                } else {
                    Utilities.showSnackBar(drawerlayout, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_lifeinsurancelist.setVisibility(View.GONE);
                }
            }
        });
    }

    public static class GetLifeInsurance extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_lifeinsurancelist.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
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
                swipeRefreshLayout.setRefreshing(false);
//                shimmer_view_container.stopShimmer();
//                shimmer_view_container.setVisibility(View.GONE);
                rv_lifeinsurancelist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    lifeInsuranceList.clear();
                    rv_lifeinsurancelist.setAdapter(new GetLifeInsuranceListAdapter(context, lifeInsuranceList));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                JSONObject jsonObj = jsonarr.getJSONObject(i);

                                if (jsonObj.getString("insurance_type").equals("2")) {
                                    LifeGeneralInsuranceMainListPojo lifeInsuranceMainObj = new LifeGeneralInsuranceMainListPojo();

                                    lifeInsuranceMainObj.setId(jsonObj.getString("id"));
                                    lifeInsuranceMainObj.setInsurance_type_id(jsonObj.getString("insurance_type"));
                                    lifeInsuranceMainObj.setInsurance_company_id(jsonObj.getString("insurance_company"));
                                    lifeInsuranceMainObj.setInsurance_company_name(jsonObj.getString("company_name"));
                                    lifeInsuranceMainObj.setInsurance_company_alias(jsonObj.getString("company_alias"));
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
                                    lifeInsuranceMainObj.setLink(jsonObj.getString("link"));
                                    lifeInsuranceMainObj.setRemark(jsonObj.getString("remark"));
                                    lifeInsuranceMainObj.setDescription(jsonObj.getString("description"));
                                    lifeInsuranceMainObj.setIs_shared(jsonObj.getString("is_shared"));
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

                                    lifeInsuranceList.add(lifeInsuranceMainObj);

                                }

                            }
                            if (lifeInsuranceList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_lifeinsurancelist.setVisibility(View.GONE);
                            } else {
                                rv_lifeinsurancelist.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                            }
                            rv_lifeinsurancelist.setAdapter(new GetLifeInsuranceListAdapter(context, lifeInsuranceList));

                        }
                    } else if (type.equalsIgnoreCase("failure")) {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_lifeinsurancelist.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_lifeinsurancelist.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public boolean filterList(String filterKeyword) {
        if (!filterKeyword.equals("")) {
            ArrayList<LifeGeneralInsuranceMainListPojo> searchedProductsList = new ArrayList<>();
            for (LifeGeneralInsuranceMainListPojo product : lifeInsuranceList) {

                if (product.getClient_name().toLowerCase().contains(filterKeyword.toLowerCase()) ||
                        product.getPolicy_no().toLowerCase().contains(filterKeyword.toLowerCase())) {
                    searchedProductsList.add(product);
                }
            }
            rv_lifeinsurancelist.setAdapter(new GetLifeInsuranceListAdapter(context, searchedProductsList));
        } else {
            rv_lifeinsurancelist.setAdapter(new GetLifeInsuranceListAdapter(context, lifeInsuranceList));
        }
        return true;
    }
}
