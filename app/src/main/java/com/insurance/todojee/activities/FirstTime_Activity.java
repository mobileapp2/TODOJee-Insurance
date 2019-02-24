package com.insurance.todojee.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FirstTime_Activity extends AppCompatActivity {
    private Context context;
    private UserSessionManager session;
    private String user_id, mobile, mode, whatsApp_number;
    private CheckBox cb_sms, cb_whatsapp, cb_terms;
    private EditText edt_whatsapp_number;
    private Button btn_submit;
    private LinearLayout ll_parent;
    private TextInputLayout til_whatsapp;
    private TextView tv_terms, tv_title_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);

        init();
        getSessionData();
        setEventHandler();
    }

    private void init() {
        context = FirstTime_Activity.this;
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);
        tv_title_text = findViewById(R.id.tv_title_text);
        cb_sms = findViewById(R.id.cb_sms);
        cb_whatsapp = findViewById(R.id.cb_whatsapp);
        cb_terms = findViewById(R.id.cb_terms);
        edt_whatsapp_number = findViewById(R.id.edt_whatsapp_number);
        til_whatsapp = findViewById(R.id.til_whatsapp);
        tv_terms = findViewById(R.id.tv_terms);
        btn_submit = findViewById(R.id.btn_submit);

        String mystring = new String("Terms and Conditions*");
        SpannableString content = new SpannableString(mystring);
        content.setSpan(new UnderlineSpan(), 0, mystring.length(), 0);
        tv_terms.setText(content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_title_text.setText(Html.fromHtml("How do you want <b>Todojee Insurance</b> to communicate with you ?", Html.FROM_HTML_MODE_COMPACT));
        } else {
            tv_title_text.setText(Html.fromHtml("How do you want <b>Todojee Insurance</b> to communicate with you ?"));
        }

    }

    private void getSessionData() {
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            user_id = json.getString("id");
            mobile = json.getString("mobile");
            whatsApp_number = json.getString("whatsApp_number");
            mode = json.getString("communication_mode");

            edt_whatsapp_number.setText(mobile);
            if (mode.equals("sms"))
                cb_sms.setChecked(true);
            else if (mode.equals("whatsApp")) {
                cb_whatsapp.setChecked(true);
                edt_whatsapp_number.setText(whatsApp_number);
                til_whatsapp.setVisibility(View.VISIBLE);
            } else if (mode.equals("both")) {
                cb_whatsapp.setChecked(true);
                cb_sms.setChecked(true);
                edt_whatsapp_number.setText(whatsApp_number);
                til_whatsapp.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setEventHandler() {
        cb_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_whatsapp.isChecked()) {
                    til_whatsapp.setVisibility(View.VISIBLE);
                } else {
                    til_whatsapp.setVisibility(View.GONE);
                }
            }
        });
        cb_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_terms.setTextColor(getResources().getColor(R.color.grey));
            }
        });
        tv_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setMessage("For a better experience while using our Service, we may require you to provide us with certain personally identifiable information, including but not limited to your name, phone number, and postal address. The information that we collect will be used to contact or identify you.");
                builder.setTitle("Terms & Conditions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertD = builder.create();
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cb_terms.isChecked()) {
                    Utilities.showSnackBar(ll_parent, "Please accept the terms and conditions.");
                    tv_terms.setTextColor(getResources().getColor(R.color.Red));
                    return;
                }
                if (!cb_sms.isChecked() && !cb_whatsapp.isChecked()) {
                    Utilities.showSnackBar(ll_parent, "Please select at list one.");
                    return;
                }
                if (cb_whatsapp.isChecked() && edt_whatsapp_number.getText().toString().equals("")) {
                    Utilities.showSnackBar(ll_parent, "Please provide whats app number.");
                    return;
                }
                if (cb_whatsapp.isChecked() && !Utilities.isMobileNo(edt_whatsapp_number)) {
                    Utilities.showSnackBar(ll_parent, "Please Enter Valid WhatsApp Number");
                    return;
                }
                if (cb_whatsapp.isChecked())
                    mode = "whatsApp";
                else if (cb_sms.isChecked())
                    mode = "sms";
                if (cb_sms.isChecked() && cb_whatsapp.isChecked())
                    mode = "both";

                new UpdateDetails().execute();
            }
        });
    }

    public class UpdateDetails extends AsyncTask<String, Void, String> {
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
        protected String doInBackground(String... strings) {
            String res = "[]";
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "updateCommunicationMode");
                obj.put("user_id", user_id);
                obj.put("whatsApp_number", edt_whatsapp_number.getText().toString().trim());
                obj.put("mode", mode);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.PROFILEAPI, obj.toString());

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
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Preferred mode of communication Updated Successfully");
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(context, MainDrawer_Activity.class));
                                finish();
                            }
                        });
                        AlertDialog alertD = builder.create();
                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                        alertD.show();
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                session.updateSession(jsonarr.toString());
                            }
                            session.updateFirstTime();

                        }
                    } else if (type.equalsIgnoreCase("failure")) {
                        Utilities.showSnackBar(ll_parent, message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
