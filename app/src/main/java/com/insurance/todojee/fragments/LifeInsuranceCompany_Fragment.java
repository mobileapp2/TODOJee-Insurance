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
import com.insurance.todojee.activities.AddInsuranceCompany_Activity;
import com.insurance.todojee.adapters.GetLifeInsurCompListAdapter;
import com.insurance.todojee.models.InsuranceCompanyListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LifeInsuranceCompany_Fragment extends Fragment {

    public static LinearLayout ll_parent;
    private static Context context;
    private static RecyclerView rv_companylist;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static LinearLayout ll_nothingtoshow;
    private FloatingActionButton fab_add_company;
    private LinearLayoutManager layoutManager;
    //    private static ShimmerFrameLayout shimmer_view_container;
    private UserSessionManager session;
    private String user_id;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lifeinsure_company, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_add_company = rootView.findViewById(R.id.fab_add_company);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_companylist = rootView.findViewById(R.id.rv_companylist);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(context);
        rv_companylist.setLayoutManager(layoutManager);
    }

    private void setDefault() {
        if (Utilities.isNetworkAvailable(context)) {
            new GetCompanyList().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_companylist.setVisibility(View.GONE);
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
        fab_add_company.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddInsuranceCompany_Activity.class);
                intent.putExtra("TYPE", "2");
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetCompanyList().execute(user_id);
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_companylist.setVisibility(View.GONE);
                }
            }
        });
    }

    public static class GetCompanyList extends AsyncTask<String, Void, String> {
        private ArrayList<InsuranceCompanyListPojo> companyList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_companylist.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllCompany"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("insurance_type_id", "2"));
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
                rv_companylist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    companyList = new ArrayList<>();
                    rv_companylist.setAdapter(new GetLifeInsurCompListAdapter(context, companyList));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                InsuranceCompanyListPojo companyMainObj = new InsuranceCompanyListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                companyMainObj.setId(jsonObj.getString("id"));
                                companyMainObj.setCompany_name(jsonObj.getString("company_name"));
                                companyMainObj.setCompany_alias(jsonObj.getString("company_alias"));
                                companyMainObj.setInsurance_type(jsonObj.getString("insurance_type"));
                                companyList.add(companyMainObj);
                            }
                            if (companyList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_companylist.setVisibility(View.GONE);
                            } else {
                                rv_companylist.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                            }
                            rv_companylist.setAdapter(new GetLifeInsurCompListAdapter(context, companyList));
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_companylist.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_companylist.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }


}
