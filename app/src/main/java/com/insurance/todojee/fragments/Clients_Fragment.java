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
import com.insurance.todojee.activities.AddClientDetails_Activity;
import com.insurance.todojee.adapters.GetClientListAdapter;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Clients_Fragment extends Fragment {

    public static DrawerLayout drawerlayout;
    private static Context context;
    private static RecyclerView rv_clientlist;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static LinearLayout ll_nothingtoshow;
    private FloatingActionButton fab_add_client;
    private LinearLayoutManager layoutManager;
    //    private static ShimmerFrameLayout shimmer_view_container;
    private UserSessionManager session;
    private String user_id;
    private SearchView searchView;
    private static ArrayList<ClientMainListPojo> clientList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_clients, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_add_client = rootView.findViewById(R.id.fab_add_client);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_clientlist = rootView.findViewById(R.id.rv_clientlist);
        drawerlayout = getActivity().findViewById(R.id.drawerlayout);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        layoutManager = new LinearLayoutManager(context);
        rv_clientlist.setLayoutManager(layoutManager);
        searchView = rootView.findViewById(R.id.searchView);
        searchView.setFocusable(false);
    }

    private void setDefault() {
        if (Utilities.isNetworkAvailable(getActivity())) {
            new GetClientList().execute(user_id);
        } else {
            Utilities.showSnackBar(drawerlayout, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_clientlist.setVisibility(View.GONE);
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

        fab_add_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray user_info = null;
                try {
                    user_info = new JSONArray(session.getUserDetails().get(
                            ApplicationConstants.KEY_LOGIN_INFO));
                    JSONObject json = user_info.getJSONObject(0);
                    if (Integer.parseInt(json.getString("customerCount")) < Integer.parseInt(json.getString("customerLimit"))) {
                        startActivity(new Intent(context, AddClientDetails_Activity.class));
                    } else {
                        Utilities.buildDialogForClientValidation(context);
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
                    new GetClientList().execute(user_id);
                } else {
                    Utilities.showSnackBar(drawerlayout, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_clientlist.setVisibility(View.GONE);
                }
            }
        });

    }

    public static class GetClientList extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_clientlist.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
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
                swipeRefreshLayout.setRefreshing(false);
//                shimmer_view_container.stopShimmer();
//                shimmer_view_container.setVisibility(View.GONE);
                rv_clientlist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    clientList.clear();
                    Collections.sort(clientList, ClientMainListPojo.NameComparator);
                    rv_clientlist.setAdapter(new GetClientListAdapter(context, clientList));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                ClientMainListPojo clientMainObj = new ClientMainListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                clientMainObj.setId(jsonObj.getString("id"));
                                clientMainObj.setFirst_name(jsonObj.getString("first_name"));
                                clientMainObj.setAlias(jsonObj.getString("alias"));
                                clientMainObj.setMobile(jsonObj.getString("mobile"));
                                clientMainObj.setWhats_app_no(jsonObj.getString("whats_app_no"));
                                clientMainObj.setEmail(jsonObj.getString("email"));
                                clientMainObj.setDob(jsonObj.getString("dob"));
                                clientMainObj.setAnniversary_date(jsonObj.getString("anniversary_date"));
                                clientMainObj.setFamily_code(jsonObj.getString("family_code"));
                                clientMainObj.setFamily_code_id(jsonObj.getString("family_code_id"));
                                clientMainObj.setIs_main(jsonObj.getString("is_main"));
                                clientMainObj.setSync_status(jsonObj.getString("sync_status"));

                                ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> familyDetailsList = new ArrayList<>();

                                for (int j = 0; j < jsonObj.getJSONArray("relation_details").length(); j++) {
                                    ClientMainListPojo.ClientFamilyDetailsPojo clientFamilyObj = new ClientMainListPojo.ClientFamilyDetailsPojo();
                                    clientFamilyObj.setFamily_details_id(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("family_details_id"));
                                    clientFamilyObj.setName(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("name"));
                                    clientFamilyObj.setDob(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("dob"));
                                    clientFamilyObj.setRelation(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("relation"));
                                    clientFamilyObj.setMobile(jsonObj.getJSONArray("relation_details").getJSONObject(j).getString("mobile"));
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
                            if (clientList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_clientlist.setVisibility(View.GONE);
                            } else {
                                rv_clientlist.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                            }
                            Collections.sort(clientList, ClientMainListPojo.NameComparator);
                            rv_clientlist.setAdapter(new GetClientListAdapter(context, clientList));

                        }
                    } else if (type.equalsIgnoreCase("failure")) {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_clientlist.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_clientlist.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public boolean filterList(String filterKeyword) {
        if (!filterKeyword.equals("")) {
            Set<ClientMainListPojo> searchedProductsSet = new HashSet<>();

            for (ClientMainListPojo product : clientList) {

                if ((null != product.getFirst_name() && product.getFirst_name().toLowerCase().contains(filterKeyword.toLowerCase())) ||
                        (null != product.getLast_name() && product.getLast_name().toLowerCase().contains(filterKeyword.toLowerCase()))
                        || (null != product.getMiddle_name() && product.getMiddle_name().toLowerCase().contains(filterKeyword.toLowerCase()))) {
                    searchedProductsSet.add(product);
                }
            }
            List<ClientMainListPojo> searchedProductsList = new ArrayList(searchedProductsSet);
            rv_clientlist.setAdapter(new GetClientListAdapter(context, searchedProductsList));
        } else {
            Collections.sort(clientList, ClientMainListPojo.NameComparator);
            rv_clientlist.setAdapter(new GetClientListAdapter(context, clientList));
        }
        return true;
    }

}
