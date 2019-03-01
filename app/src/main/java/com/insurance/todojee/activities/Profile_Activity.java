package com.insurance.todojee.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.insurance.todojee.R;
import com.insurance.todojee.ccavenue.PlanBuySuccess_Activity;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.insurance.todojee.utilities.PermissionUtil.PERMISSION_ALL;

public class Profile_Activity extends Activity {


    private Context context;
    private UserSessionManager session;
    private EditText edt_name, edt_aliasname, edt_mobile, edt_email, edt_referral_code, edt_whatsapp_number;
    private String user_id, photo, name, alias, mobile, email, password, is_email_verified, referral_code, mode, whatsApp;
    private ProgressDialog pd;
    private ImageView imv_profile, img_finish, img_edit, img_share_referral_code;
    private CoordinatorLayout ll_parent;
    private CheckBox cb_sms, cb_whatsApp;
    private TextInputLayout til_whatsapp;

    private TextView tv_messagecount, tv_whatsappcount, tv_memorycount, tv_pro;

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
        img_share_referral_code = findViewById(R.id.img_share_referral_code);
        edt_name = findViewById(R.id.edt_name);
        edt_aliasname = findViewById(R.id.edt_aliasname);
        edt_mobile = findViewById(R.id.edt_mobile);
        edt_email = findViewById(R.id.edt_email);
        edt_referral_code = findViewById(R.id.edt_referral_code);
        cb_sms = findViewById(R.id.cb_sms);
        cb_whatsApp = findViewById(R.id.cb_whatsApp);
        til_whatsapp = findViewById(R.id.til_whatsapp);
        edt_whatsapp_number = findViewById(R.id.edt_whatsapp_number);
        tv_messagecount = findViewById(R.id.tv_messagecount);
        tv_whatsappcount = findViewById(R.id.tv_whatsappcount);
        tv_memorycount = findViewById(R.id.tv_memorycount);
        tv_pro = findViewById(R.id.tv_pro);
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
            referral_code = json.getString("referral_code");
            mode = json.getString("communication_mode");
            whatsApp = json.getString("whatsApp_number");
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
        if (referral_code.equals("null"))
            edt_referral_code.setText("");
        else
            edt_referral_code.setText(referral_code);


        if (mode.equals("sms")) {
            cb_sms.setChecked(true);
        } else if (mode.equals("whatsApp")) {
            cb_whatsApp.setChecked(true);
            til_whatsapp.setVisibility(View.VISIBLE);
            edt_whatsapp_number.setText(whatsApp);
        } else if (mode.equals("both")) {
            cb_sms.setChecked(true);
            cb_whatsApp.setChecked(true);
            til_whatsapp.setVisibility(View.VISIBLE);
            edt_whatsapp_number.setText(whatsApp);
        }

        if (Utilities.isNetworkAvailable(context)) {
            new getSMSCount().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

       /* if (Utilities.isNetworkAvailable(context)) {
            new getWhatsAppCount().execute(user_id);
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }*/

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

        img_share_referral_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.app_banner);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                        b, "Title", null);
                Uri imageUri = Uri.parse(path);
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("*/*");
                share.putExtra(Intent.EXTRA_TEXT, "Sign-up using my code (" + referral_code + ") on the ToDoJee Insurance App & get reward points. \n" +
                        "Download the App using - https://play.google.com/store/apps/details?id=com.insurance.todojee\n" +
                        "Hurry, it doesn’t get better than this! Download the  ToDoJee Insurance App NOW!");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                share.putExtra(Intent.EXTRA_STREAM, imageUri);

                startActivity(Intent.createChooser(share, "Share Referral Code!"));
            }
        });

        tv_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(context, SelectPremiumPlan_Activity.class));
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
            /*List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getSMSCount"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.PROFILEAPI, param);
            return res.trim();*/
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getCounts"));
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
                        JSONArray jsonarr = mainObj.getJSONArray("counts");
                        JSONObject obj = jsonarr.getJSONObject(0);

                        tv_messagecount.setText(obj.getString("smsCount") + "/" + obj.getString("smsLimit"));
                        tv_whatsappcount.setText(obj.getString("whatsAppCount") + "/" + obj.getString("whatsAppLimit"));

                    } else {
                        tv_messagecount.setText("0/0");
                        tv_whatsappcount.setText("0/0");
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
