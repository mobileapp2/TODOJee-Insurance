package com.insurance.todojee.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.insurance.todojee.models.BirthdayAnnivarsaryListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

@SuppressLint("RestrictedApi")
public class Anniversary_Fragment extends Fragment {

    public LinearLayout ll_parent;
    private Context context;
    private RecyclerView rv_annivarsary;
    private FloatingActionButton fab_wish_whatsapp, fab_wish_sms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ll_nothingtoshow;
    private LinearLayoutManager layoutManager;
    private UserSessionManager session;
    private int mYear, mMonth, mDay;
    private String user_id, date;
    private ArrayList<BirthdayAnnivarsaryListPojo> annivarsaryList;
    private String id = "", smsMessage = "", whatsappMessage = "", whatsappPicUrl = "", whatsappPic = "";
    private EditText dialog_edt_whatsappmessage, edt_date;
    private ImageView dialog_imv_whatsapppic;
    private CheckBox cb_checkall;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_annivarsary_list, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        return rootView;
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_wish_whatsapp = rootView.findViewById(R.id.fab_wish_whatsapp);
        fab_wish_sms = rootView.findViewById(R.id.fab_wish_sms);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_annivarsary = rootView.findViewById(R.id.rv_annivarsary);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        cb_checkall = rootView.findViewById(R.id.cb_checkall);
        edt_date = rootView.findViewById(R.id.edt_date);
        layoutManager = new LinearLayoutManager(context);
        rv_annivarsary.setLayoutManager(layoutManager);

        annivarsaryList = new ArrayList<>();
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
            new GetAnniverasryList().execute(user_id, date);
//            new GetAnniverasryList().execute("472", "2018-10-13");
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_annivarsary.setVisibility(View.GONE);
        }


        fab_wish_whatsapp.setVisibility(View.GONE);
        fab_wish_sms.setVisibility(View.GONE);
        cb_checkall.setVisibility(View.GONE);
    }

    private void setEventHandlers() {

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isNetworkAvailable(context)) {
                    new GetAnniverasryList().execute(user_id, date);
//                    new GetAnniverasryList().execute("472", "2018-10-13");
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_annivarsary.setVisibility(View.GONE);
                }
            }
        });

        fab_wish_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isInternetAvailable(context)) {
                    new GetAnniWhatsappSettings().execute(user_id, "");
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            }
        });

        fab_wish_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isInternetAvailable(context)) {
                    new GetAnniSMSSettings().execute(user_id, "");
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
                            new GetAnniverasryList().execute(user_id, date);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                            swipeRefreshLayout.setRefreshing(false);
                            ll_nothingtoshow.setVisibility(View.VISIBLE);
                            rv_annivarsary.setVisibility(View.GONE);
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
                for (int i = 0; i < annivarsaryList.size(); i++) {

                    GetAnnivarsaryListAdapter.MyViewHolder myViewHolder =
                            (GetAnnivarsaryListAdapter.MyViewHolder) rv_annivarsary.findViewHolderForAdapterPosition(i);

                    if (((CheckBox) v).isChecked()) {
                        myViewHolder.cb_wish.setChecked(true);
                        annivarsaryList.get(i).setChecked(true);
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        myViewHolder.cb_wish.setChecked(false);
                        annivarsaryList.get(i).setChecked(false);
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    public class GetAnniverasryList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_annivarsary.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
//            shimmer_view_container.setVisibility(View.VISIBLE);
//            shimmer_view_container.startShimmer();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllAnniverary"));
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
                rv_annivarsary.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    annivarsaryList = new ArrayList<>();
                    rv_annivarsary.setAdapter(new GetAnnivarsaryListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                BirthdayAnnivarsaryListPojo anniversaryMainObj = new BirthdayAnnivarsaryListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                anniversaryMainObj.setId(jsonObj.getString("id"));
                                anniversaryMainObj.setName(jsonObj.getString("client_name"));
                                annivarsaryList.add(anniversaryMainObj);
                            }
                            if (annivarsaryList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_annivarsary.setVisibility(View.GONE);
                                cb_checkall.setVisibility(View.GONE);
                            } else {
                                rv_annivarsary.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                                cb_checkall.setVisibility(View.VISIBLE);
                            }
                            rv_annivarsary.setAdapter(new GetAnnivarsaryListAdapter());
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_annivarsary.setVisibility(View.GONE);
                        cb_checkall.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_annivarsary.setVisibility(View.GONE);
                cb_checkall.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public class GetAnnivarsaryListAdapter extends RecyclerView.Adapter<GetAnnivarsaryListAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_birthannidays, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            BirthdayAnnivarsaryListPojo annivarsaryDetails = new BirthdayAnnivarsaryListPojo();
            annivarsaryDetails = annivarsaryList.get(position);

            holder.tv_clientname.setText(annivarsaryDetails.getName());

            holder.cb_wish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (holder.cb_wish.isChecked())
                        annivarsaryList.get(position).setChecked(true);
                    else
                        annivarsaryList.get(position).setChecked(false);

                    if (isAllValuesChecked(annivarsaryList)) {
                        cb_checkall.setChecked(true);
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        cb_checkall.setChecked(false);
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }

                    if (isAtleastOneChecked(annivarsaryList)) {
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
                        new GetAnniSMSSettings().execute(user_id, annivarsaryList.get(position).getId());
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

            holder.imv_whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        new GetAnniWhatsappSettings().execute(user_id, annivarsaryList.get(position).getId());
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return annivarsaryList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_clientname;
            private ImageView imv_sms, imv_whatsapp;
            private CheckBox cb_wish;

            public MyViewHolder(View view) {
                super(view);
                tv_clientname = view.findViewById(R.id.tv_clientname);
                imv_sms = view.findViewById(R.id.imv_sms);
                imv_whatsapp = view.findViewById(R.id.imv_whatsapp);
                cb_wish = view.findViewById(R.id.cb_wish);
            }
        }

        private boolean isAtleastOneChecked(ArrayList<BirthdayAnnivarsaryListPojo> annivarsaryList) {
            for (int i = 0; i < annivarsaryList.size(); i++)
                if (annivarsaryList.get(i).isChecked())
                    return true;
            return false;
        }

        private boolean isAllValuesChecked(ArrayList<BirthdayAnnivarsaryListPojo> annivarsaryList) {
            for (int i = 0; i < annivarsaryList.size(); i++)
                if (!annivarsaryList.get(i).isChecked())
                    return false;
            return true;
        }
    }

    public class GetAnniSMSSettings extends AsyncTask<String, Void, String> {
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
            param.add(new ParamsPojo("type", "getAnniSMSSettings"));
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

                    final EditText edt_smsmessage = new EditText(context);
                    float dpi = context.getResources().getDisplayMetrics().density;
                    edt_smsmessage.setText(smsMessage);
                    edt_smsmessage.setSelection(smsMessage.length());
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                    builder.setTitle("Send Anniversary SMS Message");
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
            for (int i = 0; i < annivarsaryList.size(); i++) {
                if (annivarsaryList.get(i).isChecked()) {
                    JsonObject childObj = new JsonObject();
                    childObj.addProperty("id", annivarsaryList.get(i).getId());
                    clientIdJSONArray.add(childObj);
                }
            }
        } else {
            JsonObject childObj = new JsonObject();
            childObj.addProperty("id", singleReceiverID);
            clientIdJSONArray.add(childObj);
        }

        JsonObject mainObj = new JsonObject();
        mainObj.addProperty("type", "sendAnniversarySMS");
        mainObj.add("client_id", clientIdJSONArray);
        mainObj.addProperty("message", message);
        mainObj.addProperty("user_id", user_id);

        if (Utilities.isInternetAvailable(context)) {
            new SendAnniversarySMS().execute(mainObj.toString());
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

    }

    public class SendAnniversarySMS extends AsyncTask<String, Void, String> {
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


    public class GetAnniWhatsappSettings extends AsyncTask<String, Void, String> {
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
            param.add(new ParamsPojo("type", "getAnniWhatsAPPSettings"));
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
                                whatsappMessage = jsonObj.getString("message");
                                whatsappPicUrl = jsonObj.getString("images");
                                whatsappPic = jsonObj.getString("image");
                            }
                        }
                    }

                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    View promptView = layoutInflater.inflate(R.layout.prompt_whatsappsett, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                    alertDialogBuilder.setTitle("Send Anniversary Whatsapp Message");
                    alertDialogBuilder.setView(promptView);

                    dialog_edt_whatsappmessage = promptView.findViewById(R.id.dialog_edt_whatsappmessage);
                    dialog_imv_whatsapppic = promptView.findViewById(R.id.dialog_imv_whatsapppic);

                    dialog_edt_whatsappmessage.setText(whatsappMessage);

                    if (!whatsappPicUrl.equals("")) {
                        Picasso.with(context)
                                .load(whatsappPicUrl)
                                .placeholder(R.drawable.img_photo)
                                .into(dialog_imv_whatsapppic);
                    }

                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendWhatsapp(dialog_edt_whatsappmessage.getText().toString().trim(), singleReceiverID);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendWhatsapp(String message, String singleReceiverID) {
        JsonArray clientIdJSONArray = new JsonArray();

        if (singleReceiverID.equals("")) {
            for (int i = 0; i < annivarsaryList.size(); i++) {
                if (annivarsaryList.get(i).isChecked()) {
                    JsonObject childObj = new JsonObject();
                    childObj.addProperty("id", annivarsaryList.get(i).getId());
                    clientIdJSONArray.add(childObj);
                }
            }
        } else {
            JsonObject childObj = new JsonObject();
            childObj.addProperty("id", singleReceiverID);
            clientIdJSONArray.add(childObj);
        }

        JsonObject mainObj = new JsonObject();
        mainObj.addProperty("type", "sendAnniversaryWhtasAppMsg");
        mainObj.add("client_id", clientIdJSONArray);
        mainObj.addProperty("message", message);
        mainObj.addProperty("image", whatsappPic);
        mainObj.addProperty("user_id", user_id);

        if (Utilities.isInternetAvailable(context)) {
            new SendAnniversaryWhatsapp().execute(mainObj.toString());
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }

    }

    public class SendAnniversaryWhatsapp extends AsyncTask<String, Void, String> {
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


}
