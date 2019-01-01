package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SMSBirthdaySettings_Activity extends Activity {

    private Context context;
    private EditText edt_smsmessage;
    private FloatingActionButton fab_add_message;
    private LinearLayout ll_parent;
    private UserSessionManager session;
    private String user_id, id = "", smsMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_birthday);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    private void init() {
        context = SMSBirthdaySettings_Activity.this;
        session = new UserSessionManager(context);

        ll_parent = findViewById(R.id.ll_parent);
        edt_smsmessage = findViewById(R.id.edt_smsmessage);

        fab_add_message = findViewById(R.id.fab_add_message);
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
        if (Utilities.isInternetAvailable(context)) {
            new GetBirthSMSSettings().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }
    }

    private void setEventHandler() {
        fab_add_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edt_smsmessage = new EditText(context);
                float dpi = context.getResources().getDisplayMetrics().density;
                //  edt_smsmessage.setText(smsMessage);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    edt_smsmessage.setText(Html.fromHtml(smsMessage, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    edt_smsmessage.setText(Html.fromHtml(smsMessage));
                }
                if (edt_smsmessage.getText().length() > 0)
                    edt_smsmessage.setSelection(edt_smsmessage.getText().length() - 1);
                else
                    edt_smsmessage.setSelection(0);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setTitle("Set Birthday SMS Message");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (Utilities.isInternetAvailable(context)) {
                            new AddBirthSMSSettings().execute(edt_smsmessage.getText().toString().trim(), user_id);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                final AlertDialog alertD = builder.create();
                alertD.setView(edt_smsmessage, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();
            }
        });

    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);

        mToolbar.setTitle("Birthday SMS Setting");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class GetBirthSMSSettings extends AsyncTask<String, Void, String> {
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
            param.add(new ParamsPojo("type", "getBirthSMSSettings"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.SETTINGSAPI, param);
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
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                id = jsonObj.getString("id");
                                smsMessage = jsonObj.getString("message");
                            }
                            //     edt_smsmessage.setText(smsMessage);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                edt_smsmessage.setText(Html.fromHtml(smsMessage, Html.FROM_HTML_MODE_COMPACT));
                            } else {
                                edt_smsmessage.setText(Html.fromHtml(smsMessage));
                            }
                        }
                    } else {
                        edt_smsmessage.setText("");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class AddBirthSMSSettings extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "addBirthSMSSettings");
            obj.addProperty("message", params[0]);
            obj.addProperty("user_id", params[1]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SETTINGSAPI, obj.toString());
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
                        if (Utilities.isInternetAvailable(context)) {
                            new GetBirthSMSSettings().execute(user_id);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                        }
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
