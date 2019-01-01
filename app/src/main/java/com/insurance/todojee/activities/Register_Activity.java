package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.insurance.todojee.R;
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
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.insurance.todojee.utilities.Utilities.hideSoftKeyboard;

public class Register_Activity extends Activity {

    private Context context;
    private ConstraintLayout cl_parent;
    private EditText edt_name, edt_mobile, edt_password, edt_referral;
    private TextView tv_alreadyregister;
    private Button btn_register;
    private UserSessionManager session;
    CheckBox chk_referral;
    String referralCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        setEventHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKeyboard(Register_Activity.this);
    }

    private void init() {
        context = Register_Activity.this;
        session = new UserSessionManager(context);
        cl_parent = findViewById(R.id.cl_parent);

        edt_name = findViewById(R.id.edt_name);
        edt_mobile = findViewById(R.id.edt_mobile);
        edt_password = findViewById(R.id.edt_password);

        chk_referral = findViewById(R.id.chk_referral);
        tv_alreadyregister = findViewById(R.id.tv_alreadyregister);
        btn_register = findViewById(R.id.btn_register);
    }

    private void setEventHandler() {
        chk_referral.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                        if (isChecked)
                                                            createDialogForReferralCode();
                                                    }
                                                }
        );

        tv_alreadyregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });
    }

    private void submitData() {
        if (edt_name.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(cl_parent, "Please Enter Name");
            return;
        }

        if (!Utilities.isMobileNo(edt_mobile)) {
            Utilities.showSnackBar(cl_parent, "Please Enter Valid Mobile Number");
            return;
        }

        if (edt_password.getText().toString().equals("")) {
            Utilities.showSnackBar(cl_parent, "Please Enter Password");
            return;
        }

        if (Utilities.isNetworkAvailable(context)) {
            new SendOTP().execute(edt_mobile.getText().toString().trim());
        } else {
            Utilities.showSnackBar(cl_parent, "Please Check Internet Connection");
        }
    }

    private void createDialogForOTP(final String otp) {
        final EditText edt_enterotp = new EditText(context);
        float dpi = context.getResources().getDisplayMetrics().density;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle("Enter OTP");

        alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (otp.equals(edt_enterotp.getText().toString().trim())) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new RegisterNewUser().execute(edt_name.getText().toString().trim(),
                                edt_mobile.getText().toString().trim(),
                                edt_password.getText().toString().trim(), referralCode);
                    } else {
                        Utilities.showSnackBar(cl_parent, "Please Check Internet Connection");
                    }
                } else {
                    Utilities.showMessageString(context, "Please Enter Correct OTP");
                    createDialogForOTP(otp);
                }
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        alertDialogBuilder.setNeutralButton("Resend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utilities.isNetworkAvailable(context)) {
                    new SendOTP().execute(edt_mobile.getText().toString().trim());
                } else {
                    Utilities.showSnackBar(cl_parent, "Please Check Internet Connection");
                }
            }
        });

        AlertDialog alertD = alertDialogBuilder.create();
        alertD.setView(edt_enterotp, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
        alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        edt_enterotp.addTextChangedListener(new TextWatcher() {
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

    private void createDialogForReferralCode() {
        final EditText edt_referral = new EditText(context);
        float dpi = context.getResources().getDisplayMetrics().density;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle("Enter Referral Code");

        alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utilities.isNetworkAvailable(context)) {
                    new ValidateReferralCode().execute(edt_referral.getText().toString().trim());
                } else {
                    Utilities.showSnackBar(cl_parent, "Please Check Internet Connection");
                    chk_referral.setChecked(false);
                }
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                chk_referral.setChecked(false);
            }
        });


        AlertDialog alertD = alertDialogBuilder.create();
        alertD.setView(edt_referral, (int) (19 * dpi), (int) (5 * dpi), (int) (14 * dpi), (int) (5 * dpi));
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
        alertD.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        edt_referral.addTextChangedListener(new TextWatcher() {
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

    private void saveRegistrationID() {
        String user_id = "", regToken = "";
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));

            for (int j = 0; j < user_info.length(); j++) {
                JSONObject json = user_info.getJSONObject(j);
                user_id = json.getString("id");
            }

            regToken = session.getAndroidToken().get(ApplicationConstants.KEY_ANDROIDTOKETID);

            if (regToken != null && !regToken.isEmpty() && !regToken.equals("null") && !regToken.equals(""))
                new SendRegistrationToken().execute(user_id, regToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String totalRAMSize() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        double totalRAM = memoryInfo.totalMem / 1048576.0;
        return String.valueOf(totalRAM);
    }

    public class ValidateReferralCode extends AsyncTask<String, Void, String> {

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
            param.add(new ParamsPojo("type", "getValidReferral"));
            param.add(new ParamsPojo("referral", params[0]));
            referralCode = params[0];
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.REGISTERAPI, param);
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

                    } else {
                        createDialogForReferralCode();
                        Utilities.showAlertDialog(context, "Alert", message, false);

                        // chk_referral.setChecked(false);
                        referralCode = "";
                    }

                }
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    public class SendOTP extends AsyncTask<String, Void, String> {

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
            param.add(new ParamsPojo("type", "send"));
            param.add(new ParamsPojo("mobile", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.OTPAPI, param);
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
                        if (message.equals("Mobile Number is Already Registered.")) {
                            Utilities.showAlertDialog(context, "Alert", message, false);
                        } else {
                            String OTP = mainObj.getString("otp");
                            createDialogForOTP(OTP);
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

    public class RegisterNewUser extends AsyncTask<String, Void, String> {

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

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = null;
            if (chk_referral.isChecked()) {
                formBody = new FormBody.Builder()
                        .add("name", params[0])
                        .add("mobile", params[1])
                        .add("password", params[2])
                        .add("referral_code", params[3])
                        .build();
            } else {
                formBody = new FormBody.Builder()
                        .add("name", params[0])
                        .add("mobile", params[1])
                        .add("password", params[2])
                        .build();
            }
            Request request = new Request.Builder()
                    .url(ApplicationConstants.REGISTERAPI)
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                res = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                        builder.setMessage(message);
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (Utilities.isNetworkAvailable(context)) {
                                    new LoginUser().execute(edt_mobile.getText().toString().trim(), edt_password.getText().toString().trim(), "1");
                                } else {
                                    Utilities.showSnackBar(cl_parent, "Please Check Internet Connection");
                                }

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

    public class LoginUser extends AsyncTask<String, Void, String> {

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

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("email", params[0])
                    .add("password", params[1])
                    .add("is_registered", params[2])
                    .build();
            Request request = new Request.Builder()
                    .url(ApplicationConstants.LOGINAPI)
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                res = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                                session.createUserLoginSession(jsonarr.toString());
                                saveRegistrationID();
                            }
                        }
                    } else {
                        Utilities.showSnackBar(cl_parent, message);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class SendRegistrationToken extends AsyncTask<String, Integer, String> {
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
            String s = "";
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "registerDevice");
                obj.put("device_type", "Android");
                obj.put("customers_id", params[0]);
                obj.put("device_id", params[1]);
                obj.put("ram", totalRAMSize());
                obj.put("processor", Build.CPU_ABI);
                obj.put("device_os", Build.VERSION.RELEASE);
                obj.put("location", "0.0, 0.0");
                obj.put("device_model", Build.MODEL);
                obj.put("manufacturer", Build.MANUFACTURER);
                s = obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.PROFILEAPI, s);
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (result != null && result.length() > 0 && !result.equalsIgnoreCase("[]")) {
                try {
                    int c = 0;
                    JSONObject obj1 = new JSONObject(result);
                    String success = obj1.getString("success");
                    String message = obj1.getString("message");
                    if (success.equalsIgnoreCase("1")) {
                        startActivity(new Intent(context, MainDrawer_Activity.class));
                        finish();
                    } else {
                        Utilities.showAlertDialog(context, "Server Not Responding", "Please Try After Sometime", false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
