package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.GeneralInsuranceCompany_Fragment;
import com.insurance.todojee.fragments.GeneralInsurance_Fragment;
import com.insurance.todojee.fragments.LifeInsuranceCompany_Fragment;
import com.insurance.todojee.fragments.LifeInsurance_Fragment;
import com.insurance.todojee.models.InsuranceCompanyListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.insurance.todojee.utilities.Utilities.hideSoftKeyboard;

public class EditInsuranceCompany_Activity extends Activity {

    private Context context;
    private LinearLayout ll_parent;
    private EditText edt_companyname, edt_aliasname, edt_companytype;
    private ImageView img_save, img_delete;
    private String insuranceTypeId;            // "1" for general insurance and "2" for life insurance.
    private UserSessionManager session;
    private String user_id, companyId;
    private InsuranceCompanyListPojo companyDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_insurancecompany);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKeyboard(EditInsuranceCompany_Activity.this);
    }

    private void init() {
        context = EditInsuranceCompany_Activity.this;
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);

        edt_companyname = findViewById(R.id.edt_companyname);
        edt_aliasname = findViewById(R.id.edt_aliasname);
        edt_companytype = findViewById(R.id.edt_companytype);
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

        companyDetails = (InsuranceCompanyListPojo) getIntent().getSerializableExtra("companyDetails");

        companyId = companyDetails.getId();
        insuranceTypeId = companyDetails.getInsurance_type();

        edt_companyname.setText(companyDetails.getCompany_name());
        edt_aliasname.setText(companyDetails.getCompany_alias());

        if (insuranceTypeId.equals("2")) {
            edt_companytype.setText("Life Insurance Company");
        } else if (insuranceTypeId.equals("1")) {
            edt_companytype.setText("General Insurance Company");
        }
    }

    private void setEventHandler() {

        edt_companyname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                edt_aliasname.setText(edt_companyname.getText().toString().trim());
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
                            new DeleteInsuranceCompany().execute(companyDetails.getId());
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
        if (edt_companyname.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Enter Company Name");
            return;
        }

        if (edt_aliasname.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Enter Display Name");
            return;
        }


        if (Utilities.isInternetAvailable(context)) {
            new EditInsuranceCompany().execute(
                    edt_companyname.getText().toString().trim(),
                    edt_aliasname.getText().toString().trim(),
                    user_id,
                    insuranceTypeId,
                    companyId
            );
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        img_save = findViewById(R.id.img_save);
        img_delete = findViewById(R.id.img_delete);
        mToolbar.setTitle("Edit Insurance Company");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class EditInsuranceCompany extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "updateInsuranceCompan");
            obj.addProperty("insurance_company", params[0]);
            obj.addProperty("alias", params[1]);
            obj.addProperty("user_id", params[2]);
            obj.addProperty("insurance_type_id", params[3]);
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

                        new LifeInsuranceCompany_Fragment.GetCompanyList().execute(user_id);
                        new GeneralInsuranceCompany_Fragment.GetCompanyList().execute(user_id);

                        new GeneralInsurance_Fragment.GetGeneralInsurance().execute(user_id);
                        new LifeInsurance_Fragment.GetLifeInsurance().execute(user_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Insurance Company Updated Successfully");
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

    public class DeleteInsuranceCompany extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "deleteInsuranceCompan");
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

                        new LifeInsuranceCompany_Fragment.GetCompanyList().execute(user_id);
                        new GeneralInsuranceCompany_Fragment.GetCompanyList().execute(user_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Insurance Company Deleted Successfully");
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
