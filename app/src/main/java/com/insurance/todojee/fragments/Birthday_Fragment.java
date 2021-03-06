package com.insurance.todojee.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.activities.WhatsappBirthdaySettings_Activity;
import com.insurance.todojee.models.BirthdayAnnivarsaryListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

@SuppressLint("RestrictedApi")
public class Birthday_Fragment extends Fragment {

    public LinearLayout ll_parent;
    private Context context;
    private RecyclerView rv_birthday;
    private FloatingActionButton fab_wish_whatsapp, fab_wish_sms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ll_nothingtoshow;
    private LinearLayoutManager layoutManager;
    private UserSessionManager session;
    private int mYear, mMonth, mDay;
    private String user_id, date;
    private ArrayList<BirthdayAnnivarsaryListPojo> birthdayList;
    private String id = "", smsMessage = "", whatsappMessage = "", whatsappPicUrl = "", whatsappPic = "", sign = "";
    private EditText dialog_edt_whatsappmessage, edt_date;
    private ImageView dialog_imv_whatsapppic;
    private CheckBox cb_checkall;
    private static boolean isImageSet = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_birthday_list, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        //Changes by Varsha 19/12
        new GetBirthWhatsappSettings().execute(user_id, "");
        new GetSignature().execute();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_wish_whatsapp = rootView.findViewById(R.id.fab_wish_whatsapp);
        fab_wish_sms = rootView.findViewById(R.id.fab_wish_sms);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_birthday = rootView.findViewById(R.id.rv_birthday);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        cb_checkall = rootView.findViewById(R.id.cb_checkall);
        edt_date = rootView.findViewById(R.id.edt_date);
        layoutManager = new LinearLayoutManager(context);
        rv_birthday.setLayoutManager(layoutManager);

        birthdayList = new ArrayList<>();
    }

    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of birthday");
        setDefault();
        super.onResume();
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

    private void setDefault() {

        Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        date = Utilities.ConvertDateFormat(Utilities.dfDate, mDay, mMonth + 1, mYear);

        edt_date.setText(changeDateFormat("yyyy-MM-dd",
                "dd/MM/yyyy", Utilities.ConvertDateFormat(Utilities.dfDate, mDay, mMonth + 1, mYear)));

        if (Utilities.isNetworkAvailable(context)) {
            new GetBirthdayList().execute(user_id, date);
//            new GetBirthdayList().execute("472", "2018-10-13");
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_birthday.setVisibility(View.GONE);
        }


        fab_wish_whatsapp.setVisibility(View.GONE);
        fab_wish_sms.setVisibility(View.GONE);
        cb_checkall.setVisibility(View.GONE);
        cb_checkall.setChecked(false);
    }

    private void setEventHandlers() {

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetBirthdayList().execute(user_id, date);
//                    new GetBirthdayList().execute("472", "2018-10-13");
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_birthday.setVisibility(View.GONE);
                }
            }
        });

        fab_wish_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isInternetAvailable(context)) {
                    //Changes by Varsha - 19/12
                    //        new GetBirthWhatsappSettings().execute(user_id, "");
                    showDialog("", "fab");
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            }
        });

        fab_wish_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isInternetAvailable(context)) {

                    new GetBirthSMSSettings().execute(user_id, "");
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }

            }
        });

        edt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd1 = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        date = Utilities.ConvertDateFormat(Utilities.dfDate, dayOfMonth, monthOfYear + 1, year);

                        edt_date.setText(changeDateFormat("yyyy-MM-dd",
                                "dd/MM/yyyy", Utilities.ConvertDateFormat(Utilities.dfDate, dayOfMonth, monthOfYear + 1, year)));

                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;

                        if (Utilities.isNetworkAvailable(context)) {
                            new GetBirthdayList().execute(user_id, date);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                            swipeRefreshLayout.setRefreshing(false);
                            ll_nothingtoshow.setVisibility(View.VISIBLE);
                            rv_birthday.setVisibility(View.GONE);
                        }

                    }
                }, mYear, mMonth, mDay);
                try {
                    dpd1.getDatePicker().setCalendarViewShown(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dpd1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                dpd1.show();
            }
        });

        cb_checkall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < birthdayList.size(); i++) {

                    GetBirthdayListAdapter.MyViewHolder myViewHolder =
                            (GetBirthdayListAdapter.MyViewHolder) rv_birthday.findViewHolderForAdapterPosition(i);

                    if (((CheckBox) v).isChecked()) {
                        myViewHolder.cb_wish.setChecked(true);
                        birthdayList.get(i).setChecked(true);
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        myViewHolder.cb_wish.setChecked(false);
                        birthdayList.get(i).setChecked(false);
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    public class GetBirthdayList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_birthday.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllBirthday"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("date", params[1]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.BIRTHDAYANNIVERSARYAPI, param);
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                swipeRefreshLayout.setRefreshing(false);
//                shimmer_view_container.stopShimmer();
//                shimmer_view_container.setVisibility(View.GONE);
                rv_birthday.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    birthdayList = new ArrayList<>();
                    rv_birthday.setAdapter(new GetBirthdayListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                BirthdayAnnivarsaryListPojo birthdayMainObj = new BirthdayAnnivarsaryListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                birthdayMainObj.setId(jsonObj.getString("id"));
                                birthdayMainObj.setName(jsonObj.getString("name"));
                                birthdayMainObj.setChecked(false);
                                birthdayList.add(birthdayMainObj);
                            }
                            if (birthdayList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_birthday.setVisibility(View.GONE);
                                cb_checkall.setVisibility(View.GONE);
                            } else {
                                rv_birthday.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                                cb_checkall.setVisibility(View.VISIBLE);
                            }
                            rv_birthday.setAdapter(new GetBirthdayListAdapter());
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_birthday.setVisibility(View.GONE);
                        cb_checkall.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_birthday.setVisibility(View.GONE);
                cb_checkall.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public class GetBirthdayListAdapter extends RecyclerView.Adapter<GetBirthdayListAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_birthannidays, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            BirthdayAnnivarsaryListPojo birthdayDetails = new BirthdayAnnivarsaryListPojo();
            birthdayDetails = birthdayList.get(position);

            holder.tv_clientname.setText(birthdayDetails.getName());

            holder.cb_wish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (holder.cb_wish.isChecked())
                        birthdayList.get(position).setChecked(true);
                    else
                        birthdayList.get(position).setChecked(false);

                    if (isAllValuesChecked(birthdayList)) {
                        cb_checkall.setChecked(true);
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        cb_checkall.setChecked(false);
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }

                    if (isAtleastOneChecked(birthdayList)) {
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }
                }
            });

            holder.imv_sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        new GetBirthSMSSettings().execute(user_id, birthdayList.get(position).getId());
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

            holder.imv_whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        //Changes by Varsha 19/12
                        //   new GetBirthWhatsappSettings().execute(user_id, birthdayList.get(position).getId());
                        showDialog(birthdayList.get(position).getId(), "holder");
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

            holder.imv_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        shareImage();
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return birthdayList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_clientname;
            private ImageView imv_sms, imv_whatsapp, imv_share;
            private CheckBox cb_wish;

            public MyViewHolder(View view) {
                super(view);
                tv_clientname = view.findViewById(R.id.tv_clientname);
                imv_sms = view.findViewById(R.id.imv_sms);
                imv_whatsapp = view.findViewById(R.id.imv_whatsapp);
                cb_wish = view.findViewById(R.id.cb_wish);
                imv_share = view.findViewById(R.id.imv_share);

            }
        }

        private boolean isAtleastOneChecked(ArrayList<BirthdayAnnivarsaryListPojo> birthdayList) {
            for (int i = 0; i < birthdayList.size(); i++)
                if (birthdayList.get(i).isChecked())
                    return true;
            return false;
        }

        private boolean isAllValuesChecked(ArrayList<BirthdayAnnivarsaryListPojo> birthdayList) {
            for (int i = 0; i < birthdayList.size(); i++)
                if (!birthdayList.get(i).isChecked())
                    return false;
            return true;
        }
    }

    public class GetBirthSMSSettings extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;
        private String singleReceiverID = "";

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
            singleReceiverID = params[1];

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
                        }
                    }
                    // sign = new GetSignature().execute().get();
                    final EditText edt_smsmessage = new EditText(context);
                    float dpi = context.getResources().getDisplayMetrics().density;


                    //  edt_smsmessage.setText(smsMessage);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        edt_smsmessage.setText(Html.fromHtml(smsMessage + "\nFrom :" + sign, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        edt_smsmessage.setText(Html.fromHtml(smsMessage + "\nFrom :" + sign));
                    }
                    if (edt_smsmessage.getText().length() > 0)

                        edt_smsmessage.setSelection(edt_smsmessage.getText().length() - 1);
                    else
                        edt_smsmessage.setSelection(0);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                    builder.setTitle("SMS Message");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendSMS(edt_smsmessage.getText().toString().trim(), singleReceiverID);
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

                    edt_smsmessage.addTextChangedListener(new TextWatcher() {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendSMS(String message, String singleReceiverID) {
        JsonArray clientIdJSONArray = new JsonArray();

        if (singleReceiverID.equals("")) {
            for (int i = 0; i < birthdayList.size(); i++) {
                if (birthdayList.get(i).isChecked()) {
                    JsonObject childObj = new JsonObject();
                    childObj.addProperty("id", birthdayList.get(i).getId());
                    clientIdJSONArray.add(childObj);
                }
            }
        } else {
            JsonObject childObj = new JsonObject();
            childObj.addProperty("id", singleReceiverID);
            clientIdJSONArray.add(childObj);
        }
        JSONArray user_info = null;
        try {
            user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            if (Integer.parseInt(json.getString("smsCount")) + clientIdJSONArray.size() <= Integer.parseInt(json.getString("smsLimit"))) {
                JsonObject mainObj = new JsonObject();
                mainObj.addProperty("type", "sendBithdaySMS");
                mainObj.add("client_id", clientIdJSONArray);
                mainObj.addProperty("message", message);
                mainObj.addProperty("user_id", user_id);

                if (Utilities.isInternetAvailable(context)) {
                    new SendBithdaySMS().execute(mainObj.toString());
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                Utilities.buildDialogForSmsValidation(context, clientIdJSONArray.size());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public class SendBithdaySMS extends AsyncTask<String, Void, String> {
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
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.BIRTHDAYANNIVERSARYAPI, params[0]);
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
                        JSONObject obj = jsonarr.getJSONObject(0);
                        changeSessionSMSCount(obj.getString("smsCount"), obj.getString("whatsAppCount"), obj.getString("smsLimit"), obj.getString("whatsAppLimit"));

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("SMS Sent Successfully");
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

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

    public void changeSessionSMSCount(String smsCount, String whatsappCount, String maxSMS, String maxWhatsapp) {
        JSONArray user_info = null;
        try {
            user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            json.put("smsCount", smsCount);
            json.put("whatsAppCount", whatsappCount);
            json.put("smsLimit", maxSMS);
            json.put("whatsAppLimit", maxWhatsapp);
            session.updateSession(user_info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public class GetBirthWhatsappSettings extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;
        private String singleReceiverID = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
//            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            singleReceiverID = params[1];

            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getBirthWhatsAPPSettings"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.SETTINGSAPI, param);
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
//                pd.dismiss();
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
                                whatsappMessage = jsonObj.getString("message");
                                whatsappPicUrl = jsonObj.getString("images");
                                whatsappPic = jsonObj.getString("image");
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWhatsapp(String message, String whatsappPic, String singleReceiverID) {
        JsonArray clientIdJSONArray = new JsonArray();

        if (singleReceiverID.equals("")) {
            for (int i = 0; i < birthdayList.size(); i++) {
                if (birthdayList.get(i).isChecked()) {
                    JsonObject childObj = new JsonObject();
                    childObj.addProperty("id", birthdayList.get(i).getId());
                    clientIdJSONArray.add(childObj);
                }
            }
        } else {
            JsonObject childObj = new JsonObject();
            childObj.addProperty("id", singleReceiverID);
            clientIdJSONArray.add(childObj);
        }
        JSONArray user_info = null;
        try {
            user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            if (Integer.parseInt(json.getString("whatsAppCount")) + clientIdJSONArray.size() <= Integer.parseInt(json.getString("whatsAppLimit"))) {
                JsonObject mainObj = new JsonObject();
                mainObj.addProperty("type", "sendBithdayWhtasAppMsg");
                mainObj.add("client_id", clientIdJSONArray);
                mainObj.addProperty("message", message);
                mainObj.addProperty("image", whatsappPic);
                mainObj.addProperty("user_id", user_id);

                if (Utilities.isInternetAvailable(context)) {
                    new SendBirthdayWhatsapp().execute(mainObj.toString());
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            } else {
                Utilities.buildDialogForSmsValidation(context, clientIdJSONArray.size());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public class SendBirthdayWhatsapp extends AsyncTask<String, Void, String> {
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
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.BIRTHDAYANNIVERSARYAPI, params[0]);
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
                        JSONObject obj = jsonarr.getJSONObject(0);
                        changeSessionSMSCount(obj.getString("smsCount"), obj.getString("whatsAppCount"), obj.getString("smsLimit"), obj.getString("whatsAppLimit"));

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Whatsapp Message Sent Successfully");
                        builder.setIcon(R.drawable.ic_success_24dp);
                        builder.setTitle("Success");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

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

    private void showDialog(String singleReceiverID, String callfrom) {
        if (!callfrom.equals("share")) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.prompt_send_whatsappmsg, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle("Whatsapp Message");
            alertDialogBuilder.setView(promptView);
          /*  try {
                sign = new GetSignature().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/

            dialog_edt_whatsappmessage = promptView.findViewById(R.id.dialog_edt_whatsappmessage);
            dialog_imv_whatsapppic = promptView.findViewById(R.id.dialog_imv_whatsapppic);
            CheckBox cb_whatsappmsg = promptView.findViewById(R.id.cb_whatsappmsg);
            CheckBox cb_whatsappimg = promptView.findViewById(R.id.cb_whatsappimg);

            //dialog_edt_whatsappmessage.setText(whatsappMessage);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dialog_edt_whatsappmessage.setText(Html.fromHtml(whatsappMessage + "\nFrom :" + sign, Html.FROM_HTML_MODE_COMPACT));
            } else {
                dialog_edt_whatsappmessage.setText(Html.fromHtml(whatsappMessage + "\nFrom :" + sign));
            }
            if (!whatsappPicUrl.equals("")) {
                isImageSet = true;
                Picasso.with(context)
                        .load(whatsappPicUrl)
                        .placeholder(R.drawable.img_photo)
                        .into(dialog_imv_whatsapppic);
            }

            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!cb_whatsappmsg.isChecked() && !cb_whatsappimg.isChecked()) {
                        Utilities.showMessageString(context, "Please Check Atleast One");
                        return;
                    }

                    String whatsappPicSend = "", whatsappMsgSend = "";
                    if (cb_whatsappmsg.isChecked()) {
                        whatsappMsgSend = dialog_edt_whatsappmessage.getText().toString().trim();
                    }
                    if (cb_whatsappimg.isChecked()) {
                        whatsappPicSend = whatsappPic;
                    }
                    sendWhatsapp(whatsappMsgSend, whatsappPicSend, singleReceiverID);
                }
            });

            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertD = alertDialogBuilder.create();
            alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
            alertD.show();

            dialog_edt_whatsappmessage.addTextChangedListener(new TextWatcher() {
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
        } else {
            Intent share = new Intent(Intent.ACTION_SEND);

            // If you want to share a png image only, you can do:
            // setType("image/png"); OR for jpeg: setType("image/jpeg");
            share.setType("image/*");

            // Make sure you put example png image named myImage.png in your
            // directory
            String imagePath = Environment.getExternalStorageDirectory() + "/Insurance/"
                    + "/Settings/" + File.separatorChar + "uplimg1.png";

            File imageFileToShare = new File(imagePath);
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.insurance.todojee.provider", imageFileToShare);

            //    Uri uri = Uri.fromFile(imageFileToShare);
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.prompt_send_whatsappmsg, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle("Whatsapp Message");
            alertDialogBuilder.setView(promptView);

            dialog_edt_whatsappmessage = promptView.findViewById(R.id.dialog_edt_whatsappmessage);
            dialog_imv_whatsapppic = promptView.findViewById(R.id.dialog_imv_whatsapppic);
            CheckBox cb_whatsappmsg = promptView.findViewById(R.id.cb_whatsappmsg);
            CheckBox cb_whatsappimg = promptView.findViewById(R.id.cb_whatsappimg);

            // dialog_edt_whatsappmessage.setText(whatsappMessage);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dialog_edt_whatsappmessage.setText(Html.fromHtml(whatsappMessage, Html.FROM_HTML_MODE_COMPACT));
            } else {
                dialog_edt_whatsappmessage.setText(Html.fromHtml(whatsappMessage));
            }

            if (!whatsappPicUrl.equals("")) {
                isImageSet = true;
//                Picasso.with(context)
//                        .load(whatsappPicUrl)
//                        .placeholder(R.drawable.img_photo)
//                        .into(dialog_imv_whatsapppic);
            }

            if (isImageSet) {
                Picasso.with(context)
                        .load(imageFileToShare)
                        .placeholder(R.drawable.img_photo)
                        .into(dialog_imv_whatsapppic);
            }

            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!cb_whatsappmsg.isChecked() && !cb_whatsappimg.isChecked()) {
                        Utilities.showMessageString(context, "Please Check Atleast One");
                        return;
                    }

                    String whatsappPicSend = "", whatsappMsgSend = "";

                    if (cb_whatsappimg.isChecked()) {
                        if (isImageSet) {
                            share.putExtra(Intent.EXTRA_STREAM, uri);
                            if (cb_whatsappmsg.isChecked())
                                whatsappMsgSend = dialog_edt_whatsappmessage.getText().toString().trim();
                            share.putExtra(Intent.EXTRA_TEXT, whatsappMsgSend);
                            startActivity(Intent.createChooser(share, "Share Image!"));
                        } else
                            Utilities.showAlertDialog(context, "Information", "Please set the image in the Birthday Settings", false);
                    } else if (cb_whatsappmsg.isChecked()) {
                        whatsappMsgSend = dialog_edt_whatsappmessage.getText().toString().trim();
                        Intent share = new Intent(android.content.Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        //added by Varsha
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        share.putExtra(Intent.EXTRA_TEXT, whatsappMsgSend);
                        startActivity(Intent.createChooser(share, "Share link!"));
                    }

                }
            });

            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertD = alertDialogBuilder.create();
            alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
            alertD.show();

            dialog_edt_whatsappmessage.addTextChangedListener(new TextWatcher() {
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

    private void shareImage() {


        showDialog("", "share");

//        share.putExtra(Intent.EXTRA_STREAM, uri);
//        share.putExtra(Intent.EXTRA_TEXT, "Happy Birthday");
//        startActivity(Intent.createChooser(share, "Share Image!"));


    }

    public class GetSignature extends AsyncTask<String, String, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
//            pd.show();
        }


        @Override
        protected String doInBackground(String... strings) {
//            pd.dismiss();
            String result = "[]";
            String sign = "";
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "getSign");
                obj.put("userid", user_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            result = WebServiceCalls.JSONAPICall(ApplicationConstants.SIGNATURE, obj.toString());

                 /*   if (!result.equals("")) {
                        JSONObject mainObj = null;
                        try {
                            mainObj = new JSONObject(result);
                            String type = mainObj.getString("type");
                            if (type.equalsIgnoreCase("success")) {
                                JSONArray jsonarr = mainObj.getJSONArray("result");
                                if (jsonarr.length() > 0) {
                                    for (int i = 0; i < jsonarr.length(); i++) {
                                        JSONObject jsonObj = jsonarr.getJSONObject(i);
                                        sign =jsonObj.getString("signature");
                                    }
                                }
                                    }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        }
                    return sign;*/
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (!result.equals("")) {
                JSONObject mainObj = null;
                try {
                    mainObj = new JSONObject(result);
                    String type = mainObj.getString("type");
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                sign = jsonObj.getString("signature");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }

}
