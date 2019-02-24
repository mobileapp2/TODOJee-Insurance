package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Settings_Activity extends Activity {

    private Context context;
    private UserSessionManager session;
    private SwitchCompat switchBirthdayWhatsappButton, switchBirthdaySMSButton, switchAnnniWhatsappButton, switchAnnniSMSButton, switchPremiumDueButton;
    String user_id;
    LinearLayout ll_parent, ll_signature;
    int birthSMS, birthWhatsapp, anniSMS, anniWhatsapp, due;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
        getSessionData();

        new getSettings().execute();
        setEventHandler();
        setUpToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getSettings().execute();

    }

    private void init() {
        context = Settings_Activity.this;
        ll_parent = findViewById(R.id.ll_parent);
        session = new UserSessionManager(context);
        ll_signature = findViewById(R.id.ll_signature);

        switchBirthdayWhatsappButton = findViewById(R.id.switchBirthdayWhatsappButton);
        switchBirthdaySMSButton = findViewById(R.id.switchBirthdaySMSButton);
        switchAnnniWhatsappButton = findViewById(R.id.switchAnnniWhatsappButton);
        switchAnnniSMSButton = findViewById(R.id.switchAnnniSMSButton);
        switchPremiumDueButton = findViewById(R.id.switchPremiumDueButton);

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

    private void setEventHandler() {
        switchBirthdayWhatsappButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Utilities.showSnackBar(ll_parent, "Birthday WhatsApp clicked" + isChecked);
                String checked;
                if (isChecked) {
                    checked = "1";
                    if (birthWhatsapp == 2) {
                        checked = "0";
                        switchBirthdayWhatsappButton.setChecked(false);
                        showAlertDialog("birthWhats");
                    }
                } else {
                    checked = "0";
                }
                new updateBirthdayWhatsApp().execute(checked);
            }
        });
        switchBirthdaySMSButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Utilities.showSnackBar(ll_parent, "Birthday SMS clicked" + isChecked);
                String checked;
                if (isChecked) {
                    checked = "1";
                    if (birthSMS == 2) {
                        checked = "0";
                        switchBirthdaySMSButton.setChecked(false);
                        showAlertDialog("birthSMS");
                    }
                } else {
                    checked = "0";
                }

                new updateBirthdaySMS().execute(checked);
            }
        });
        switchAnnniWhatsappButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Utilities.showSnackBar(ll_parent, "Anniversary Whatsapp clicked" + isChecked);
                String checked;
                if (isChecked) {
                    checked = "1";
                    if (anniWhatsapp == 2) {
                        checked = "0";
                        switchAnnniWhatsappButton.setChecked(false);
                        showAlertDialog("anniWhats");
                    }
                } else {
                    checked = "0";
                }
                new updateAnniversaryWhatsApp().execute(checked);
            }
        });
        switchAnnniSMSButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Utilities.showSnackBar(ll_parent, "Anniversary SMS clicked" + isChecked);
                String checked;
                if (isChecked) {
                    checked = "1";
                    if (anniSMS == 2) {
                        checked = "0";
                        switchAnnniSMSButton.setChecked(false);
                        showAlertDialog("anniSMS");

                    }
                } else {
                    checked = "0";
                }
                new updateAnniversarySMS().execute(checked);
            }
        });
        switchPremiumDueButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Utilities.showSnackBar(ll_parent, "Premium due clicked" + isChecked);
                String checked;
                if (isChecked) {
                    checked = "1";
                    if (due == 2) {
                        checked = "0";
                        switchPremiumDueButton.setChecked(false);
                        showAlertDialog("due");
                        startActivity(new Intent(context, PremiumDueMessageSetting_Activity.class));
                    }
                } else {
                    checked = "0";
                }
                new updatePremiumDue().execute(checked);
            }
        });
        ll_signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getSign().execute();

            }
        });
    }

    public void openBirthdayWhatsappSettings(View view) {
        startActivity(new Intent(context, WhatsappBirthdaySettings_Activity.class));
    }

    public void openBirthdaySMSSettings(View view) {
        startActivity(new Intent(context, SMSBirthdaySettings_Activity.class));
    }

    public void openAnniversaryWhatsappSettings(View view) {
        startActivity(new Intent(context, WhatsappAnniversarySettings_Activity.class));
    }

    public void openAnniversarySMSSettings(View view) {
        startActivity(new Intent(context, SMSAnniversarySettings_Activity.class));
    }

    public void openPremiumDueMessageSettings(View view) {
        startActivity(new Intent(context, PremiumDueMessageSetting_Activity.class));
    }

    public void showAlertDialog(String type) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("There are no default settings to send automated messages. Please add settings !");
        alertDialog.setIcon(R.drawable.ic_alert_red_24dp);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch (type) {
                    case "birthWhats":
                        startActivity(new Intent(context, WhatsappBirthdaySettings_Activity.class));
                        break;
                    case "birthSMS":
                        startActivity(new Intent(context, SMSBirthdaySettings_Activity.class));
                        break;
                    case "anniWhats":
                        startActivity(new Intent(context, WhatsappAnniversarySettings_Activity.class));
                        break;
                    case "anniSMS":
                        startActivity(new Intent(context, SMSAnniversarySettings_Activity.class));
                        break;
                    case "due":
                        startActivity(new Intent(context, PremiumDueMessageSetting_Activity.class));
                        break;
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertD = alertDialog.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }


    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Settings");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class updateBirthdayWhatsApp extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "addauomatedBirthWhats");
            obj.addProperty("user_id", user_id);
            obj.addProperty("setting", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SETTINGSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                // pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        if (Utilities.isInternetAvailable(context)) {

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

    public class updateBirthdaySMS extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "addauomatedBirthSMS");
            obj.addProperty("user_id", user_id);
            obj.addProperty("setting", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SETTINGSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                //   pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        if (Utilities.isInternetAvailable(context)) {

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

    public class updateAnniversaryWhatsApp extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "addauomatedAnniWhats");
            obj.addProperty("user_id", user_id);
            obj.addProperty("setting", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SETTINGSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                //pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        if (Utilities.isInternetAvailable(context)) {

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

    public class updateAnniversarySMS extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "addauomatedAnniSMS");
            obj.addProperty("user_id", user_id);
            obj.addProperty("setting", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SETTINGSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                //  pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        if (Utilities.isInternetAvailable(context)) {

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

    public class updatePremiumDue extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "addauomateDue");
            obj.addProperty("user_id", user_id);
            obj.addProperty("setting", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SETTINGSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                //  pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        if (Utilities.isInternetAvailable(context)) {

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

    public class getSettings extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "getAutomatedSettings");
            obj.addProperty("user_id", user_id);

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
                        JSONObject data = mainObj.getJSONObject("result");
                        if (data.getString("birthsms").equals("1")) {
                            birthSMS = 0;
                            switchBirthdaySMSButton.setChecked(true);
                        } else if (data.getString("birthsms").equals("2")) {
                            birthSMS = 2;
                        } else {
                            birthSMS = 0;
                            switchBirthdaySMSButton.setChecked(false);
                        }


                        if (data.getString("birthapp").equals("1")) {
                            birthWhatsapp = 0;
                            switchBirthdayWhatsappButton.setChecked(true);
                        } else if (data.getString("birthapp").equals("2")) {
                            birthWhatsapp = 2;
                        } else {
                            birthWhatsapp = 0;
                            switchBirthdayWhatsappButton.setChecked(false);
                        }

                        if (data.getString("annisms").equals("1")) {
                            anniSMS = 0;
                            switchAnnniSMSButton.setChecked(true);
                        } else if (data.getString("annisms").equals("2")) {
                            anniSMS = 2;
                        } else {
                            anniSMS = 0;
                            switchAnnniSMSButton.setChecked(false);
                        }

                        if (data.getString("anniapp").equals("1")) {
                            anniWhatsapp = 0;
                            switchAnnniWhatsappButton.setChecked(true);
                        } else if (data.getString("anniapp").equals("2")) {
                            anniWhatsapp = 2;
                        } else {
                            anniWhatsapp = 0;
                            switchAnnniWhatsappButton.setChecked(false);
                        }

                        if (data.getString("due").equals("1")) {
                            due = 0;
                            switchPremiumDueButton.setChecked(true);
                        } else if (data.getString("due").equals("2")) {
                            due = 2;
                        } else {
                            due = 0;
                            switchPremiumDueButton.setChecked(false);
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

    public class getSign extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String res = "[]";
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "getSign");
                obj.put("userid", user_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SIGNATURE, obj.toString());

            return res;

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
                                final EditText edt_signature = new EditText(context);
                                float dpi = context.getResources().getDisplayMetrics().density;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    edt_signature.setText(Html.fromHtml(jsonObj.getString("signature"), Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    edt_signature.setText(Html.fromHtml(jsonObj.getString("signature")));
                                }
                                if (edt_signature.getText().length() > 0)

                                    edt_signature.setSelection(edt_signature.getText().length() - 1);
                                else
                                    edt_signature.setSelection(0);

                                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                                builder.setTitle("Update Signature");
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        new updateSign().execute(edt_signature.getText().toString().trim());
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                final AlertDialog alertD = builder.create();
                                alertD.setView(edt_signature, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
                                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                                alertD.show();

                                edt_signature.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    }

                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        if (TextUtils.isEmpty(s)) {
                                            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                        } else {
                                            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                        }

                                    }
                                });
                            }
                        }
                    } else {
                        final EditText edt_signature = new EditText(context);
                        float dpi = context.getResources().getDisplayMetrics().density;

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setTitle("Add Signature");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new updateSign().execute(edt_signature.getText().toString().trim());
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        final AlertDialog alertD = builder.create();
                        alertD.setView(edt_signature, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                        alertD.show();

                        edt_signature.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                if (TextUtils.isEmpty(s)) {
                                    alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                } else {
                                    alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                }

                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public class updateSign extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String res = "[]";
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "updateSign");
                obj.put("userid", user_id);
                obj.put("sign", strings[0]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.SIGNATURE, obj.toString());

            return res;

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
                        builder.setMessage("Signature updated Successfully");
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
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
