package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.models.ProductInfoListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewProductInfo_Activity extends Activity {

    private Context context;
    private LinearLayout ll_parent;
    private EditText edt_productinfo;
    private FloatingActionButton fab_add_share;
    private ProductInfoListPojo productDetails;
    private ImageView img_delete, img_edit, imv_product;
    private UserSessionManager session;
    private String user_id, document_url, document_name;
    private ArrayList<ClientMainListPojo> clientList;

    private RecyclerView lv_checkboxlist;
    private CheckBox cb_selectallclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_productinfo);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    private void init() {
        context = ViewProductInfo_Activity.this;
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);

        imv_product = findViewById(R.id.imv_product);
        fab_add_share = findViewById(R.id.fab_add_share);

        edt_productinfo = findViewById(R.id.edt_productinfo);

        clientList = new ArrayList<>();
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

    private void setDefaults() {
        productDetails = (ProductInfoListPojo) getIntent().getSerializableExtra("productDetails");

        if (!productDetails.getDocument().equals("")) {
            Picasso.with(context)
                    .load(productDetails.getDocument())
                    .placeholder(R.drawable.img_product)
                    .into(imv_product, new Callback() {
                        @Override
                        public void onSuccess() {
                            document_url = productDetails.getDocument();
                            Uri uri = Uri.parse(document_url);
                            document_name = uri.getLastPathSegment();
                            imv_product.setClickable(true);
                        }

                        @Override
                        public void onError() {
                            document_name = "";
                            imv_product.setClickable(false);
                        }
                    });
        }


        edt_productinfo.setText(productDetails.getText());
    }

    private void setEventHandler() {

        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(context, EditProductInfo_Activity.class);
                intent.putExtra("productDetails", productDetails);
                startActivity(intent);
            }
        });

        img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setMessage("Are you sure you want to delete this item?");
                builder.setTitle("Alert");
                builder.setIcon(R.drawable.ic_alert_red_24dp);
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Utilities.isInternetAvailable(context)) {
                            new DeleteProductInfo().execute(productDetails.getId());
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertD = builder.create();
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();
            }
        });

        imv_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.prompt_fullpic, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                alertDialogBuilder.setView(promptView);

                ImageView imv_fullpic = promptView.findViewById(R.id.imv_fullpic);
                if (!productDetails.getDocument().equals("")) {
                    Picasso.with(context)
                            .load(productDetails.getDocument())
                            .placeholder(R.drawable.img_product)
                            .into(imv_fullpic);
                }
                AlertDialog alertD = alertDialogBuilder.create();
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();
            }
        });

        fab_add_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clientList.size() == 0) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetClientList().execute(user_id);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    clientListDialog();
                }
            }
        });
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);

        img_delete = findViewById(R.id.img_delete);
        img_edit = findViewById(R.id.img_edit);
        mToolbar.setTitle("Product Details");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class DeleteProductInfo extends AsyncTask<String, Void, String> {
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
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "delete");
            obj.addProperty("id", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.PRODUCTINFOAPI, obj.toString());
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
                    if (type.equalsIgnoreCase("success")) {

                        new MastersProductList_Activity.GetProductInfoList().execute(user_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Product Details Deleted Successfully");
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                        AlertDialog alertD = builder.create();
                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                        alertD.show();
                    } else {
                        Utilities.showAlertDialog(context, "Alert", message, false);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetClientList extends AsyncTask<String, Void, String> {

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

                                clientList.add(clientMainObj);
                            }
                        }

                        if (clientList.size() != 0) {
                            clientListDialog();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setTitle("No Record Found");
                        builder.setMessage("Please add client details");
                        builder.setIcon(R.drawable.ic_alert_red_24dp);
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(context, AddClientDetails_Activity.class));
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog alertD = builder.create();
                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                        alertD.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    private void clientListDialog(final ArrayList<ClientMainListPojo> clientList) {
//        final String[] clientNameList = new String[clientList.size()];
//        final String[] clientIdList = new String[clientList.size()];
////        final int checkedItemCount;
//
//        for (int i = 0; i < clientList.size(); i++) {
//            clientNameList[i] = (String.valueOf(clientList.get(i).getFirst_name()));
//            clientIdList[i] = (String.valueOf(clientList.get(i).getId()));
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
//        LayoutInflater inflater = getLayoutInflater();
//        builder.setTitle("Select Clients");
//        builder.setCancelable(false);
//        View rawview = inflater.inflate(R.layout.prompt_checkbox_listview, null);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, clientNameList);
//        lv_checkboxlist = rawview.findViewById(R.id.lv_checkboxlist);
//        lv_checkboxlist.setAdapter(adapter);
//
//        builder.setView(rawview);
//
//        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                int checkedItemCount = getCheckedItemCount();
//
//                if (checkedItemCount != 0) {
//                    JsonArray clientIdJSONArray = new JsonArray();
//                    SparseBooleanArray positions = lv_checkboxlist.getCheckedItemPositions();
//                    if (positions != null) {
//                        int length = positions.size();
//                        for (int z = 0; z < length; z++) {
//                            if (positions.get(positions.keyAt(z))) {
//                                JsonObject childObj = new JsonObject();
//                                childObj.addProperty("id", String.valueOf(clientList.get(positions.keyAt(z)).getId()));
//                                clientIdJSONArray.add(childObj);
//                            }
//                        }
//
//                    }
//
//                    JsonObject mainObj = new JsonObject();
//                    mainObj.addProperty("type", "shareProduct");
//                    mainObj.add("client-id", clientIdJSONArray);
//                    mainObj.addProperty("recored-id", productDetails.getId());
//                    mainObj.addProperty("user_id", user_id);
//
//                    if (Utilities.isInternetAvailable(context)) {
//                        new ShareProductDetails().execute(mainObj.toString());
//                    } else {
//                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
//                    }
//                } else {
//                    Utilities.showMessageString(context, "No Client Selected");
//                }
//
//
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//            }
//        });
//
//        AlertDialog alertD = builder.create();
//        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
//        alertD.show();
//    }

    private void clientListDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.prompt_checkbox_listview, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle("Client List");
        alertDialogBuilder.setView(promptView);

        lv_checkboxlist = promptView.findViewById(R.id.lv_checkboxlist);
        lv_checkboxlist.setLayoutManager(new LinearLayoutManager(context));
        cb_selectallclient = promptView.findViewById(R.id.cb_selectallclient);

        cb_selectallclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < clientList.size(); i++) {

                    ClientListAdapter.MyViewHolder myViewHolder =
                            (ClientListAdapter.MyViewHolder) lv_checkboxlist.findViewHolderForAdapterPosition(i);

                    if (((CheckBox) v).isChecked()) {
                        myViewHolder.cb_check.setChecked(true);
                        clientList.get(i).setChecked(true);
                    } else {
                        myViewHolder.cb_check.setChecked(false);
                        clientList.get(i).setChecked(false);
                    }
                }
            }
        });

        lv_checkboxlist.setAdapter(new ClientListAdapter());

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ArrayList<ClientMainListPojo> selectedClientList = new ArrayList<>();
                for (ClientMainListPojo clientObj : clientList) {
                    if (clientObj.isChecked()) {
                        selectedClientList.add(clientObj);
                    }

                }


                if (selectedClientList.size() != 0) {
                    JsonArray clientIdJSONArray = new JsonArray();

                    for (ClientMainListPojo clientObj : selectedClientList) {
                        JsonObject childObj = new JsonObject();
                        childObj.addProperty("id", clientObj.getId());
                        clientIdJSONArray.add(childObj);


                    }

                    JsonObject mainObj = new JsonObject();
                    mainObj.addProperty("type", "shareProduct");
                    mainObj.add("client-id", clientIdJSONArray);
                    mainObj.addProperty("recored-id", productDetails.getId());
                    mainObj.addProperty("user_id", user_id);

                    if (Utilities.isInternetAvailable(context)) {
                        new ShareProductDetails().execute(mainObj.toString());
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    Utilities.showMessageString(context, "No Client Selected");
                }


            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertD = alertDialogBuilder.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();


    }

    public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_filteritem, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            ClientMainListPojo annivarsaryDetails = new ClientMainListPojo();
            annivarsaryDetails = clientList.get(position);

            holder.tv_itemname.setText(annivarsaryDetails.getFirst_name());

            holder.cb_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (holder.cb_check.isChecked())
                        clientList.get(position).setChecked(true);
                    else
                        clientList.get(position).setChecked(false);

                    if (isAllValuesChecked(clientList)) {
                        cb_selectallclient.setChecked(true);
                    } else {
                        cb_selectallclient.setChecked(false);
                    }

                }
            });

            holder.view1.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return clientList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_itemname;
            private CheckBox cb_check;
            private View view1;

            public MyViewHolder(View view) {
                super(view);
                tv_itemname = view.findViewById(R.id.tv_itemname);
                cb_check = view.findViewById(R.id.cb_check);
                view1 = view.findViewById(R.id.view);
            }
        }

        private boolean isAllValuesChecked(ArrayList<ClientMainListPojo> clientList) {
            for (int i = 0; i < clientList.size(); i++)
                if (!clientList.get(i).isChecked())
                    return false;
            return true;
        }
    }


    public class ShareProductDetails extends AsyncTask<String, Void, String> {
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
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.PRODUCTINFOAPI, params[0]);
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
                    if (type.equalsIgnoreCase("success")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Product Details Shared Successfully");
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        AlertDialog alertD = builder.create();
                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                        alertD.show();
                    } else {

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
