package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_Activity extends Activity {


    private Context context;
    private UserSessionManager session;
    private EditText edt_name, edt_aliasname, edt_mobile, edt_email;
    private String user_id, photo, name, alias, mobile, email, password, is_email_verified;
    private ProgressDialog pd;
    private ImageView imv_profile, img_finish, img_edit;
    private CoordinatorLayout ll_parent;

    private TextView tv_messagecount, tv_whatsappcount, tv_memorycount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
        getSessionData();
        setDefaults();
        setEventHandler();
//        setUpToolbar();
    }

    private void init() {
        context = Profile_Activity.this;
        session = new UserSessionManager(context);
        pd = new ProgressDialog(context, R.style.CustomDialogTheme);
        ll_parent = findViewById(R.id.ll_parent);
        imv_profile = findViewById(R.id.imv_profile);
        img_finish = findViewById(R.id.img_finish);
        img_edit = findViewById(R.id.img_edit);
        edt_name = findViewById(R.id.edt_name);
        edt_aliasname = findViewById(R.id.edt_aliasname);
        edt_mobile = findViewById(R.id.edt_mobile);
        edt_email = findViewById(R.id.edt_email);

        tv_messagecount = findViewById(R.id.tv_messagecount);
        tv_whatsappcount = findViewById(R.id.tv_whatsappcount);
        tv_memorycount = findViewById(R.id.tv_memorycount);
    }

    private void getSessionData() {
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            user_id = json.getString("id");
            name = json.getString("name");
            alias = json.getString("alias");
            email = json.getString("email");
            mobile = json.getString("mobile");
            password = json.getString("password");
            photo = json.getString("photo");
            is_email_verified = json.getString("is_email_verified");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setDefaults() {

        if (!photo.equals("")) {
            Picasso.with(context)
                    .load(photo)
                    .placeholder(R.drawable.icon_userprofile)
                    .into(imv_profile);
        }

        edt_name.setText(name);
        edt_aliasname.setText(alias);
        edt_mobile.setText(mobile);
        edt_email.setText(email);

        if (Utilities.isNetworkAvailable(context)) {
            new getSMSCount().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

        if (Utilities.isNetworkAvailable(context)) {
            new getWhatsAppCount().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

        if (Utilities.isNetworkAvailable(context)) {
            new getUserSpace().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }
    }

    private void setEventHandler() {
        imv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.prompt_profile_pic, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                alertDialogBuilder.setView(promptView);
                CircleImageView imv_labphoto = promptView.findViewById(R.id.imv_profile);
                Picasso.with(context)
                        .load(photo)
                        .placeholder(R.drawable.icon_userprofile)
                        .into(imv_labphoto);
                AlertDialog alertD = alertDialogBuilder.create();
                alertD.show();

            }
        });

        img_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, EditProfile_Activity.class));
                finish();
            }
        });

    }

    public class getSMSCount extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getSMSCount"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.PROFILEAPI, param);
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
                        tv_messagecount.setText(mainObj.getString("SMS Count"));
                    } else {
                        tv_messagecount.setText(mainObj.getString("SMS Count"));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class getWhatsAppCount extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getWhatsAppCount"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.PROFILEAPI, param);
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
                        tv_whatsappcount.setText(mainObj.getString("WhatsApp Count"));
                    } else {
                        tv_whatsappcount.setText(mainObj.getString("WhatsApp Count"));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class getUserSpace extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getUserSpace"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.PROFILEAPI, param);
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
                        tv_memorycount.setText(mainObj.getString("data"));
                    } else {
                        tv_memorycount.setText(mainObj.getString("data"));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
