package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.GeneralInsuranceCompany_Fragment;
import com.insurance.todojee.fragments.GeneralInsurePolicyType_Fragment;
import com.insurance.todojee.fragments.LifeInsuranceCompany_Fragment;
import com.insurance.todojee.fragments.LifeInsurePolicyType_Fragment;
import com.insurance.todojee.models.InsuranceCompanyListPojo;
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

import static com.insurance.todojee.utilities.Utilities.hideSoftKeyboard;

public class EditPolicyType_Activity extends Activity {

    private Context context;
    private LinearLayout ll_parent;
    private EditText edt_policytype, edt_insurancecompany, edt_policytypename, edt_aliasname;
    private ImageView img_save, img_delete;
    private String insuranceTypeId, companyId, user_id, policyTypeId;            // "1" for general insurance and "2" for life insurance.
    private UserSessionManager session;
    private PolicyTypeListPojo policyDetails;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_policytype);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKeyboard(EditPolicyType_Activity.this);
    }

    private void init() {
        context = EditPolicyType_Activity.this;
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);

        edt_policytype = findViewById(R.id.edt_policytype);
        edt_insurancecompany = findViewById(R.id.edt_insurancecompany);
        edt_policytypename = findViewById(R.id.edt_policytypename);
        edt_aliasname = findViewById(R.id.edt_aliasname);
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

        policyDetails = (PolicyTypeListPojo) getIntent().getSerializableExtra("policyDetails");
        position = getIntent().getIntExtra("position", 0);

        companyId = policyDetails.getId();
        insuranceTypeId = policyDetails.getInsurance_type();

        edt_insurancecompany.setText(policyDetails.getCompany_name());

        policyTypeId = policyDetails.getPolicy_details().get(position).getId();
        edt_policytypename.setText(policyDetails.getPolicy_details().get(position).getType());
        edt_aliasname.setText(policyDetails.getPolicy_details().get(position).getAlias());


        if (insuranceTypeId.equals("2")) {
            edt_policytype.setText("Life Insurance Company");
        } else if (insuranceTypeId.equals("1")) {
            edt_policytype.setText("General Insurance Company");
        }
    }

    private void setEventHandler() {

        edt_policytypename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                edt_aliasname.setText(edt_policytypename.getText().toString().trim());
            }
        });

        edt_insurancecompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetCompanyList().execute(user_id, insuranceTypeId);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            }
        });

        img_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitData();
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
                            new DeletePolicyType().execute(policyTypeId);
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


    }

    private void submitData() {

        if (edt_insurancecompany.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Select Insurance Company");
            return;
        }

        if (edt_policytypename.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Enter Policy Type Name");
            return;
        }

        if (edt_aliasname.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Enter Display Name");
            return;
        }

        if (Utilities.isInternetAvailable(context)) {
            new EditPolicyType().execute(
                    edt_policytypename.getText().toString().trim(),
                    edt_aliasname.getText().toString().trim(),
                    companyId,
                    user_id,
                    policyTypeId
            );
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        img_save = findViewById(R.id.img_save);
        img_delete = findViewById(R.id.img_delete);
        mToolbar.setTitle("Edit Policy Type");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class GetCompanyList extends AsyncTask<String, Void, String> {
        private ArrayList<InsuranceCompanyListPojo> companyList;
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please Wait. . .");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllCompany"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("insurance_type_id", params[1]));
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
                    companyList = new ArrayList<>();
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                InsuranceCompanyListPojo companyMainObj = new InsuranceCompanyListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                companyMainObj.setId(jsonObj.getString("id"));
                                companyMainObj.setCompany_name(jsonObj.getString("company_name"));
                                companyMainObj.setCompany_alias(jsonObj.getString("company_alias"));
                                companyList.add(companyMainObj);
                            }
                            companyListDialog(companyList);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);

                        if (insuranceTypeId.equals("2")) {
                            builder.setMessage("Please add life insurance company");
                        } else if (insuranceTypeId.equals("1")) {
                            builder.setMessage("Please add general insurance company");
                        }

                        builder.setIcon(R.drawable.ic_alert_red_24dp);
                        builder.setTitle("No Record Found");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(context, AddInsuranceCompany_Activity.class);
                                intent.putExtra("TYPE", insuranceTypeId);
                                startActivity(intent);
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

    private void companyListDialog(final ArrayList<InsuranceCompanyListPojo> companyList) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Insurance Company");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < companyList.size(); i++) {

            arrayAdapter.add(String.valueOf(companyList.get(i).getCompany_name()));
        }

        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                edt_insurancecompany.setText(companyList.get(which).getCompany_name());
                companyId = companyList.get(which).getId();

            }
        });
        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    public class EditPolicyType extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "updateLICType");
            obj.addProperty("lic_type", params[0]);
            obj.addProperty("alias", params[1]);
            obj.addProperty("company_id", params[2]);
            obj.addProperty("user_id", params[3]);
            obj.addProperty("id", params[4]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.INSURANCEAPI, obj.toString());
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

                        new LifeInsurePolicyType_Fragment.GetPolicyTypeList().execute(user_id, "-1");
                        new GeneralInsurePolicyType_Fragment.GetPolicyTypeList().execute(user_id, "-1");

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Policy Type Updated Successfully");
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

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class DeletePolicyType extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "deleteLICType");
            obj.addProperty("id", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.INSURANCEAPI, obj.toString());
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

                        new LifeInsurePolicyType_Fragment.GetPolicyTypeList().execute(user_id, "-1");
                        new GeneralInsurePolicyType_Fragment.GetPolicyTypeList().execute(user_id, "-1");

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Policy Type Deleted Successfully");
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


}
