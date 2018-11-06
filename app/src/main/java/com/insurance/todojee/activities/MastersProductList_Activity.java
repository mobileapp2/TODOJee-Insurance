package com.insurance.todojee.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.insurance.todojee.R;
import com.insurance.todojee.adapters.GetProductInfoListAdapter;
import com.insurance.todojee.models.ProductInfoListPojo;
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

public class MastersProductList_Activity extends Activity {

    private static Context context;
    private static RecyclerView rv_productlist;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static LinearLayout ll_nothingtoshow;
    private static LinearLayout ll_parent;
    //    private static ShimmerFrameLayout shimmer_view_container;
    private FloatingActionButton fab_add_product;
    private LinearLayoutManager layoutManager;
    private UserSessionManager session;
    private String user_id;
    private static ArrayList<ProductInfoListPojo> productInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masters_product_list);

        init();
        getSessionData();
        setDefault();
        setEventHandlers();
        setUpToolbar();
    }

    private void init() {
        context = MastersProductList_Activity.this;
        session = new UserSessionManager(context);
        ll_nothingtoshow = findViewById(R.id.ll_nothingtoshow);
        rv_productlist = findViewById(R.id.rv_productlist);
        ll_parent = findViewById(R.id.ll_parent);
        fab_add_product = findViewById(R.id.fab_add_product);
//        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        layoutManager = new LinearLayoutManager(context);
        rv_productlist.setLayoutManager(layoutManager);

        productInfoList = new ArrayList<>();
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
            new GetProductInfoList().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_productlist.setVisibility(View.GONE);
        }
    }

    private void setEventHandlers() {

        rv_productlist.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ProductInfoListPojo productDetails = productInfoList.get(position);
                Intent intent = new Intent(context, ViewProductInfo_Activity.class);
                intent.putExtra("productDetails", productDetails);
                startActivity(intent);
            }
        }));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetProductInfoList().execute(user_id);
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_productlist.setVisibility(View.GONE);
                }
            }
        });

        fab_add_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, AddProductInfo_Activity.class));
            }
        });
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Products");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public static class GetProductInfoList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_productlist.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllProduct"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.PRODUCTINFOAPI, param);
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
                rv_productlist.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    productInfoList = new ArrayList<>();
                    rv_productlist.setAdapter(new GetProductInfoListAdapter(context, productInfoList));
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                ProductInfoListPojo summary = new ProductInfoListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                summary.setId(jsonObj.getString("id"));
                                summary.setText(jsonObj.getString("text"));
                                summary.setDocument(jsonObj.getString("document"));
                                productInfoList.add(summary);
                            }
                            if (productInfoList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_productlist.setVisibility(View.GONE);
                            } else {
                                rv_productlist.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                            }
                            rv_productlist.setAdapter(new GetProductInfoListAdapter(context, productInfoList));
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_productlist.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
