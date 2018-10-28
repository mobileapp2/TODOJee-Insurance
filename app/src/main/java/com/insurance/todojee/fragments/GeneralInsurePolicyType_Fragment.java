package com.insurance.todojee.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.insurance.todojee.R;
import com.insurance.todojee.activities.AddPolicyType_Activity;
import com.insurance.todojee.adapters.GetGeneralInsurParentPolicyTypeListAdapter;
import com.insurance.todojee.models.PolicyTypeListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeneralInsurePolicyType_Fragment extends Fragment {

    public static LinearLayout ll_parent;
    private static Context context;
    private static RecyclerView rv_policytypelist;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static LinearLayout ll_nothingtoshow;
    private FloatingActionButton fab_add_policytype;
    private LinearLayoutManager layoutManager;
//    private static ShimmerFrameLayout shimmer_view_container;
    private String user_id;
    private UserSessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_generalinsure_policytype, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_add_policytype = rootView.findViewById(R.id.fab_add_policytype);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_policytypelist = rootView.findViewById(R.id.rv_policytypelist);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(context);
        rv_policytypelist.setLayoutManager(layoutManager);
    }

    private void setDefault() {
        if (Utilities.isNetworkAvailable(context)) {
            new GetPolicyTypeList().execute(user_id, "-1");
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_policytypelist.setVisibility(View.GONE);
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

        fab_add_policytype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddPolicyType_Activity.class);
                intent.putExtra("TYPE", "1");
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetPolicyTypeList().execute(user_id, "-1");
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_policytypelist.setVisibility(View.GONE);
                }
            }
        });
    }

    public static class GetPolicyTypeList extends AsyncTask<String, Void, String> {
        private ArrayList<PolicyTypeListPojo> policyTypeList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_policytypelist.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllLICTYpe"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("company_id", params[1]));
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
                rv_policytypelist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    policyTypeList = new ArrayList<>();
                    rv_policytypelist.setAdapter(new GetGeneralInsurParentPolicyTypeListAdapter(context, policyTypeList));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                PolicyTypeListPojo policyTypeMainObj = new PolicyTypeListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);

                                if (jsonObj.getString("insurance_type").equals("1")) {

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

                            }
                            if (policyTypeList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_policytypelist.setVisibility(View.GONE);
                            } else {
                                rv_policytypelist.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                            }
                            rv_policytypelist.setAdapter(new GetGeneralInsurParentPolicyTypeListAdapter(context, policyTypeList));
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_policytypelist.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_policytypelist.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

}
