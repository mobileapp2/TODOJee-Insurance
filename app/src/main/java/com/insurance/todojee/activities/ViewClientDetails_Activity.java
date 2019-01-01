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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.Clients_Fragment;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

public class ViewClientDetails_Activity extends Activity {


    private Context context;
    private ScrollView scrollView;
    private LinearLayout ll_parent;
    private EditText edt_name, edt_alias, edt_mobile, edt_whatsapp, edt_email, edt_dob, edt_anniversary, edt_familycode;
    private LinearLayout ll_familydetails, ll_firmdetails;
    private TextView tv_familydetails, tv_firmdetails;
    private ClientMainListPojo clientDetails;
    private List<LinearLayout> familyDetailsLayouts = new ArrayList<>();
    private List<LinearLayout> firmDetailsLayouts = new ArrayList<>();
    private ImageView img_delete, img_edit;
    private UserSessionManager session;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_clientdetails);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    private void init() {
        context = ViewClientDetails_Activity.this;
        session = new UserSessionManager(context);
        scrollView = findViewById(R.id.scrollView);
        ll_parent = findViewById(R.id.ll_parent);

        edt_name = findViewById(R.id.edt_name);
        edt_alias = findViewById(R.id.edt_alias);
        edt_mobile = findViewById(R.id.edt_mobile);
        edt_whatsapp = findViewById(R.id.edt_whatsapp);
        edt_email = findViewById(R.id.edt_email);
        edt_dob = findViewById(R.id.edt_dob);
        edt_anniversary = findViewById(R.id.edt_anniversary);
        edt_familycode = findViewById(R.id.edt_familycode);

        ll_familydetails = findViewById(R.id.ll_familydetails);
        ll_firmdetails = findViewById(R.id.ll_firmdetails);

        tv_familydetails = findViewById(R.id.tv_familydetails);
        tv_firmdetails = findViewById(R.id.tv_firmdetails);
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
        clientDetails = (ClientMainListPojo) getIntent().getSerializableExtra("clientDetails");

        edt_name.setText(clientDetails.getFirst_name());
        edt_alias.setText(clientDetails.getAlias());
        edt_mobile.setText(clientDetails.getMobile());
        edt_whatsapp.setText(clientDetails.getWhats_app_no());
        edt_email.setText(clientDetails.getEmail());
        edt_dob.setText(changeDateFormat("yyyy-MM-dd",
                "dd/MM/yyyy",
                clientDetails.getDob()));
        edt_anniversary.setText(changeDateFormat("yyyy-MM-dd",
                "dd/MM/yyyy",
                clientDetails.getAnniversary_date()));
        edt_familycode.setText(clientDetails.getFamily_code());

        ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> familyDetailsList = new ArrayList<>();
        familyDetailsList = clientDetails.getRelation_details();


        if (familyDetailsList.size() != 0) {
            for (int i = 0; i < familyDetailsList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_viewfamilydetails, null);
                familyDetailsLayouts.add((LinearLayout) rowView);
                ll_familydetails.addView(rowView, ll_familydetails.getChildCount());

                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyname)).setText(familyDetailsList.get(i).getName());
                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familydob)).setText(changeDateFormat("yyyy-MM-dd",
                        "dd/MM/yyyy",
                        familyDetailsList.get(i).getDob()));
                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyrelation)).setText(familyDetailsList.get(i).getRelation());
                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_mobile)).setText(familyDetailsList.get(i).getMobile());
            }
        } else {
            tv_familydetails.setText("No Family Details Added");
        }

        ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> firmDetailsList = new ArrayList<>();
        firmDetailsList = clientDetails.getFirm_details();

        if (firmDetailsList.size() != 0) {
            for (int i = 0; i < firmDetailsList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_viewfirmdetails, null);
                firmDetailsLayouts.add((LinearLayout) rowView);
                ll_firmdetails.addView(rowView, ll_firmdetails.getChildCount());

                ((EditText) firmDetailsLayouts.get(i).findViewById(R.id.edt_firmname)).setText(firmDetailsList.get(i).getFirm_name());
            }
        } else {
            tv_firmdetails.setText("No Firm Details Added");
        }

    }

    private void setEventHandler() {
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
                            new DeleteClientDetails().execute(user_id, clientDetails.getId());
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

        img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditClientDetails_Activity.class);
                intent.putExtra("clientDetails", clientDetails);
                context.startActivity(intent);
                finish();

            }
        });
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);

        img_delete = findViewById(R.id.img_delete);
        img_edit = findViewById(R.id.img_edit);
        mToolbar.setTitle("Client Details");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class DeleteClientDetails extends AsyncTask<String, Void, String> {
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
            obj.addProperty("user_id", params[0]);
            obj.addProperty("id", params[1]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.CLIENTAPI, obj.toString());
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

                        new Clients_Fragment.GetClientList().execute(user_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Client Details Deleted Successfully");
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
