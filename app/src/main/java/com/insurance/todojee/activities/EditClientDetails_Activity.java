package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.Clients_Fragment;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.models.FamilyCodePojo;
import com.insurance.todojee.models.RelationListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;
import static com.insurance.todojee.utilities.Utilities.hideSoftKeyboard;

public class EditClientDetails_Activity extends Activity {

    private Context context;
    private ScrollView scrollView;
    private LinearLayout ll_parent;
    private EditText edt_name, edt_alias, edt_mobile, edt_whatsapp, edt_email, edt_dob, edt_anniversary, edt_familycode;
    private LinearLayout ll_familydetails, ll_firmdetails;
    private ImageView btn_addfamilydetails, btn_addfirmdetails;
    private int mYear, mMonth, mDay;
    private int mYear1, mMonth1, mDay1;
    private int mYear2, mMonth2, mDay2;

    private ClientMainListPojo clientDetails;
    private List<LinearLayout> familyDetailsLayouts = new ArrayList<>();
    private List<LinearLayout> firmDetailsLayouts = new ArrayList<>();
    private ArrayList<FamilyCodePojo> familyCodeList;
    private ArrayList<RelationListPojo> relationsList;
    private UserSessionManager session;
    private String user_id, familyCodeId = "0";
    private ImageView img_save;
    private EditText edt_relation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clientdetails);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKeyboard(EditClientDetails_Activity.this);
    }

    private void init() {
        context = EditClientDetails_Activity.this;
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
        btn_addfamilydetails = findViewById(R.id.btn_addfamilydetails);
        btn_addfirmdetails = findViewById(R.id.btn_addfirmdetails);
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
        Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        mYear1 = cal.get(Calendar.YEAR);
        mMonth1 = cal.get(Calendar.MONTH);
        mDay1 = cal.get(Calendar.DAY_OF_MONTH);

        mYear2 = cal.get(Calendar.YEAR);
        mMonth2 = cal.get(Calendar.MONTH);
        mDay2 = cal.get(Calendar.DAY_OF_MONTH);

        clientDetails = (ClientMainListPojo) getIntent().getSerializableExtra("clientDetails");

        familyCodeList = new ArrayList<>();
        relationsList = new ArrayList<>();

        if (relationsList.size() == 0) {
            if (Utilities.isNetworkAvailable(context)) {
                new GetRelationList().execute(user_id, "0");
            } else {
                Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            }
        }

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
                final View rowView = inflater.inflate(R.layout.add_layout_familydetails, null);
                familyDetailsLayouts.add((LinearLayout) rowView);
                ll_familydetails.addView(rowView, ll_familydetails.getChildCount() - 1);

                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyname)).setText(familyDetailsList.get(i).getName());
                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familydob)).setText(changeDateFormat("yyyy-MM-dd",
                        "dd/MM/yyyy",
                        familyDetailsList.get(i).getDob()));
                ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyrelation)).setText(familyDetailsList.get(i).getRelation());
            }
        }

        ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> firmDetailsList = new ArrayList<>();
        firmDetailsList = clientDetails.getFirm_details();

        if (firmDetailsList.size() != 0) {
            for (int i = 0; i < firmDetailsList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_firmdetails, null);
                firmDetailsLayouts.add((LinearLayout) rowView);
                ll_firmdetails.addView(rowView, ll_firmdetails.getChildCount() - 1);

                ((EditText) firmDetailsLayouts.get(i).findViewById(R.id.edt_firmname)).setText(firmDetailsList.get(i).getFirm_name());
            }
        }

    }

    private void setEventHandler() {

        edt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                edt_alias.setText(edt_name.getText().toString().trim());
            }
        });

        edt_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd1 = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        edt_dob.setText(
                                changeDateFormat("yyyy-MM-dd",
                                        "dd/MM/yyyy",
                                        Utilities.ConvertDateFormat(Utilities.dfDate, dayOfMonth, monthOfYear + 1, year))
                        );

                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                    }
                }, mYear, mMonth, mDay);
                try {
                    dpd1.getDatePicker().setCalendarViewShown(false);
                    dpd1.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000 * 60 * 60));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dpd1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                dpd1.show();
            }
        });

        edt_anniversary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd1 = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        edt_anniversary.setText(
                                changeDateFormat("yyyy-MM-dd",
                                        "dd/MM/yyyy",
                                        Utilities.ConvertDateFormat(Utilities.dfDate, dayOfMonth, monthOfYear + 1, year))

                        );

                        mYear1 = year;
                        mMonth1 = monthOfYear;
                        mDay1 = dayOfMonth;
                    }
                }, mYear1, mMonth1, mDay1);
                try {
                    dpd1.getDatePicker().setCalendarViewShown(false);
                    dpd1.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000 * 60 * 60));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dpd1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                dpd1.show();
            }
        });

        edt_familycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (familyCodeList.size() == 0) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetFamilyCodeList().execute(user_id);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    familyCodeDialog(familyCodeList);
                }
            }
        });

        edt_familycode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                familyCodeId = "0";
            }

            @Override
            public void afterTextChanged(Editable s) {
                familyCodeId = "0";
            }
        });

        btn_addfamilydetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_familydetails, null);
                LinearLayout ll = (LinearLayout) rowView;
                familyDetailsLayouts.add(ll);
                ll_familydetails.addView(rowView, ll_familydetails.getChildCount() - 1);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });

        btn_addfirmdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_firmdetails, null);
                LinearLayout ll = (LinearLayout) rowView;
                firmDetailsLayouts.add(ll);
                ll_firmdetails.addView(rowView, ll_firmdetails.getChildCount() - 1);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });

        img_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitData();
            }
        });

    }

    public void removeFamilyDetailsView(View view) {
        ll_familydetails.removeView((View) view.getParent());
        familyDetailsLayouts.remove(view.getParent());
    }

    public void removeFirmDetailsView(View view) {
        ll_firmdetails.removeView((View) view.getParent());
        firmDetailsLayouts.remove(view.getParent());
    }

    public void selectDate(View view) {
        final TextView edt_familydob = (TextView) view;
        DatePickerDialog dpd1 = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                edt_familydob.setText(
                        changeDateFormat("yyyy-MM-dd",
                                "dd/MM/yyyy",
                                Utilities.ConvertDateFormat(Utilities.dfDate, dayOfMonth, monthOfYear + 1, year))
                );

            }
        }, mYear2, mMonth2, mDay2);
        try {
            dpd1.getDatePicker().setCalendarViewShown(false);
            dpd1.getDatePicker().setMaxDate(System.currentTimeMillis() + (1000 * 60 * 60));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dpd1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        dpd1.show();
    }

    public void selectRelation(View view) {
        edt_relation = (EditText) view;
//        edt_relation.setTag(relationsIdList.size());
        if (relationsList.size() == 0) {
            if (Utilities.isNetworkAvailable(context)) {
                new GetRelationList().execute(user_id, "1");
            } else {
                Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            }
        } else {
            relationDialog(relationsList);
        }
    }

    private void submitData() {
        if (relationsList.size() == 0) {
            if (Utilities.isNetworkAvailable(context)) {
                new GetRelationList().execute(user_id, "0");
            } else {
                Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            }
        }


        if (edt_name.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Enter Name");
            return;
        }

        if (edt_alias.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Enter Display Name");
            return;
        }

        if (!Utilities.isMobileNo(edt_mobile)) {
            Utilities.showSnackBar(ll_parent, "Please Enter Valid Mobile Number");
            return;
        }

        if (!edt_whatsapp.getText().toString().equals("")) {
            if (!Utilities.isMobileNo(edt_whatsapp)) {
                Utilities.showSnackBar(ll_parent, "Please Enter Valid Mobile Number");
                return;
            }
        }

        if (!edt_email.getText().toString().equals("")) {
            if (!Utilities.isEmailValid(edt_email)) {
                Utilities.showSnackBar(ll_parent, "Please Enter Valid Email Address");
                return;
            }
        }

        ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> familyDetailsList = new ArrayList<>();
        for (int i = 0; i < familyDetailsLayouts.size(); i++) {

            if (!((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyname)).getText().toString().trim().equals("") ||
                    !((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familydob)).getText().toString().trim().equals("") ||
                    !((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyrelation)).getText().toString().trim().equals("")) {

                ClientMainListPojo.ClientFamilyDetailsPojo clientFamilyObj = new ClientMainListPojo.ClientFamilyDetailsPojo();
                clientFamilyObj.setName(((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyname)).getText().toString().trim());
                clientFamilyObj.setDob(changeDateFormat(
                        "dd/MM/yyyy",
                        "yyyy-MM-dd",
                        ((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familydob)).getText().toString().trim()));
//                clientFamilyObj.setRelation(((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyrelation)).getText().toString().trim());

                for (int j = 0; j < relationsList.size(); j++) {
                    if (!((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyrelation)).getText().toString().trim().equals("")) {
                        if (relationsList.get(j).getRelation().equals(((EditText) familyDetailsLayouts.get(i).findViewById(R.id.edt_familyrelation)).getText().toString().trim())) {
                            clientFamilyObj.setRelation(String.valueOf(j + 1));
                        }
                    } else {
                        clientFamilyObj.setRelation("0");
                    }
                }
                familyDetailsList.add(clientFamilyObj);
            }
        }


        ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> firmDetailsList = new ArrayList<>();
        for (int i = 0; i < firmDetailsLayouts.size(); i++) {

            if (!((EditText) firmDetailsLayouts.get(i).findViewById(R.id.edt_firmname)).getText().toString().trim().equals("")) {

                ClientMainListPojo.ClientFirmDetailsPojo clientFirmObj = new ClientMainListPojo.ClientFirmDetailsPojo();
                clientFirmObj.setFirm_name(((EditText) firmDetailsLayouts.get(i).findViewById(R.id.edt_firmname)).getText().toString().trim());
                firmDetailsList.add(clientFirmObj);

            }
        }

        JsonObject mainObj = new JsonObject();

        JsonArray familyJSONArray = new JsonArray();

        for (int i = 0; i < familyDetailsList.size(); i++) {
            JsonObject familyJSONObj = new JsonObject();
            familyJSONObj.addProperty("name", familyDetailsList.get(i).getName());
            familyJSONObj.addProperty("dob", familyDetailsList.get(i).getDob());
            familyJSONObj.addProperty("relation", familyDetailsList.get(i).getRelation());
            familyJSONArray.add(familyJSONObj);
        }


        JsonArray firmJSONArray = new JsonArray();

        for (int i = 0; i < firmDetailsList.size(); i++) {
            JsonObject firmJSONObj = new JsonObject();
            firmJSONObj.addProperty("firm_name", firmDetailsList.get(i).getFirm_name());
            firmJSONArray.add(firmJSONObj);
        }


        mainObj.addProperty("type", "update");
        mainObj.addProperty("name", edt_name.getText().toString().trim());
        mainObj.addProperty("alias", edt_alias.getText().toString().trim());
        mainObj.addProperty("email", edt_email.getText().toString().trim());
        mainObj.addProperty("mobile", edt_mobile.getText().toString().trim());
        mainObj.addProperty("dob", changeDateFormat("dd/MM/yyyy",
                "yyyy-MM-dd",
                edt_dob.getText().toString().trim()));
        mainObj.addProperty("anniversary", changeDateFormat("dd/MM/yyyy",
                "yyyy-MM-dd",
                edt_anniversary.getText().toString().trim()));
        mainObj.addProperty("family_code_id", familyCodeId);
        mainObj.addProperty("family_code", edt_familycode.getText().toString().trim());
        mainObj.addProperty("id", clientDetails.getId());
        mainObj.addProperty("user_id", user_id);
        mainObj.addProperty("whatsapp", edt_whatsapp.getText().toString().trim());
        mainObj.addProperty("sync_status", "U");
        mainObj.add("family_relation", familyJSONArray);
        mainObj.add("firm_details", firmJSONArray);

        Log.i("ClientDetailsJSON", mainObj.toString());

        if (Utilities.isInternetAvailable(context)) {
            new UpdateClientDetails().execute(mainObj.toString());
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        img_save = findViewById(R.id.img_save);
        mToolbar.setTitle("Edit Client Details");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class GetFamilyCodeList extends AsyncTask<String, Void, String> {

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
            param.add(new ParamsPojo("type", "getAllFamilyCode"));
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
                    if (type.equalsIgnoreCase("success")) {
                        familyCodeList = new ArrayList<>();
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                FamilyCodePojo summary = new FamilyCodePojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                if (!jsonObj.getString("code").equals("")) {
                                    summary.setId(jsonObj.getString("id"));
                                    summary.setCode(jsonObj.getString("code"));
                                    familyCodeList.add(summary);
                                }
                            }
                            if (familyCodeList.size() != 0) {
                                familyCodeDialog(familyCodeList);
                            } else {
                                Utilities.showAlertDialog(context, "No Record Found", "Please enter family code manually", false);
                            }
                        }
                    } else {
                        Utilities.showAlertDialog(context, "No Record Found", "Please enter family code manually", false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void familyCodeDialog(final ArrayList<FamilyCodePojo> familyCodeList) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Family Code");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < familyCodeList.size(); i++) {

            arrayAdapter.add(String.valueOf(familyCodeList.get(i).getCode()));
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
                edt_familycode.setText(familyCodeList.get(which).getCode());
                familyCodeId = familyCodeList.get(which).getId();
            }
        });
        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    public class GetRelationList extends AsyncTask<String, Void, String> {

        ProgressDialog pd;
        String TYPE = "0";

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
            TYPE = params[1];

            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllRelations"));
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
                    if (type.equalsIgnoreCase("success")) {
                        relationsList = new ArrayList<>();
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                RelationListPojo summary = new RelationListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                summary.setId(jsonObj.getString("id"));
                                summary.setRelation(jsonObj.getString("relation"));
                                relationsList.add(summary);
                            }
                            if (relationsList.size() != 0) {
                                if (TYPE.equals("1")) {
                                    relationDialog(relationsList);
                                }
                            }
                        }
                    } else {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void relationDialog(final ArrayList<RelationListPojo> relationsList) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Relation");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < relationsList.size(); i++) {

            arrayAdapter.add(String.valueOf(relationsList.get(i).getRelation()));
        }

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                edt_relation.setText(relationsList.get(which).getRelation());
//                relationsIdList.add(Integer.parseInt(String.valueOf(edt_relation.getTag())), relationsList.get(which).getId());
//                familyCodeId = familyCodeList.get(which).getId();

            }
        });
        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    public class UpdateClientDetails extends AsyncTask<String, Void, String> {
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
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.CLIENTAPI, params[0]);
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
                        builder.setMessage("Client Details Updated Successfully");
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

}
