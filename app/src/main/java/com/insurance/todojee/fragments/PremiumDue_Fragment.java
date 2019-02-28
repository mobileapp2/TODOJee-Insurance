package com.insurance.todojee.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import com.insurance.todojee.models.EventListPojo;
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
public class PremiumDue_Fragment extends Fragment {

    public LinearLayout ll_parent;
    private Context context;
    private RecyclerView rv_premiumdue;
    private FloatingActionButton fab_wish_whatsapp, fab_wish_sms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout ll_nothingtoshow;
    private LinearLayoutManager layoutManager;
    private UserSessionManager session;
    private int mYear, mMonth, mDay;
    private String user_id, date, user_name;
    private ArrayList<EventListPojo> premiumDueList;
    private String id = "", message = "", whatsappPicUrl = "", whatsappPic = "", sign = "";
    private EditText edt_date, dialog_edt_whatsappmessage;
    private CheckBox cb_checkall;
    private ImageView dialog_imv_whatsapppic;
    private static boolean isImageSet = false;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_premiumdue_list, container, false);
        context = getActivity();
        init(rootView);
        getSessionData();
        setDefault();
        setEventHandlers();
        new GetPremiumMessage().execute(user_id);
        new GetSignature().execute();
        return rootView;
    }

    @Override
    public void onResume() {
        Log.e("DEBUG", "onResume of premium");
        setDefault();
        super.onResume();
    }

    private void init(View rootView) {
        session = new UserSessionManager(context);
        fab_wish_whatsapp = rootView.findViewById(R.id.fab_wish_whatsapp);
        fab_wish_sms = rootView.findViewById(R.id.fab_wish_sms);
        ll_nothingtoshow = rootView.findViewById(R.id.ll_nothingtoshow);
        rv_premiumdue = rootView.findViewById(R.id.rv_premiumdue);
        ll_parent = getActivity().findViewById(R.id.ll_parent);
//        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        cb_checkall = rootView.findViewById(R.id.cb_checkall);
        edt_date = rootView.findViewById(R.id.edt_date);
        layoutManager = new LinearLayoutManager(context);
        rv_premiumdue.setLayoutManager(layoutManager);

        premiumDueList = new ArrayList<>();
    }

    private void getSessionData() {
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            user_id = json.getString("id");
            user_name = json.getString("name");
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
            new GetEventList().execute(user_id, date);
//            new GetEventList().execute("472", "2018-10-13");
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            swipeRefreshLayout.setRefreshing(false);
            ll_nothingtoshow.setVisibility(View.VISIBLE);
            rv_premiumdue.setVisibility(View.GONE);
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
                    new GetEventList().execute(user_id, date);
                    swipeRefreshLayout.setRefreshing(true);
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    swipeRefreshLayout.setRefreshing(false);
                    ll_nothingtoshow.setVisibility(View.VISIBLE);
                    rv_premiumdue.setVisibility(View.GONE);
                }
            }
        });

        fab_wish_whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isInternetAvailable(context)) {
                    // new GetPremiumMessage().execute(user_id, "", "WHATSAPP");
                    //   showDialog("","WHATSAPP", "fab");
                    confirmDialogForWhatsapp("", "all selected clients.", "all");

                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            }
        });

        fab_wish_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isInternetAvailable(context)) {
                    // new GetPremiumMessage().execute(user_id, "", "SMS");
                    //  showDialog("","SMS", "fab");
                    confirmDialogForSMS("", "all selected clients.", "all");

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
                            new GetEventList().execute(user_id, date);
                        } else {
                            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                            swipeRefreshLayout.setRefreshing(false);
                            ll_nothingtoshow.setVisibility(View.VISIBLE);
                            rv_premiumdue.setVisibility(View.GONE);
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
                for (int i = 0; i < premiumDueList.size(); i++) {

                    GetEventListAdapter.MyViewHolder myViewHolder =
                            (GetEventListAdapter.MyViewHolder) rv_premiumdue.findViewHolderForAdapterPosition(i);

                    if (((CheckBox) v).isChecked()) {
                        myViewHolder.cb_wish.setChecked(true);
                        premiumDueList.get(i).setChecked(true);
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        myViewHolder.cb_wish.setChecked(false);
                        premiumDueList.get(i).setChecked(false);
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    public class GetEventList extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_nothingtoshow.setVisibility(View.GONE);
            rv_premiumdue.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            JsonObject obj = new JsonObject();
            obj.addProperty("type", "getEventStatus");
            obj.addProperty("user_id", params[0]);
            obj.addProperty("date", params[1]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.EVENTSAPI, obj.toString());
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "", message = "";
            try {
                swipeRefreshLayout.setRefreshing(false);
                rv_premiumdue.setVisibility(View.VISIBLE);
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    premiumDueList = new ArrayList<>();
                    rv_premiumdue.setAdapter(new GetEventListAdapter());
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                EventListPojo eventMainObj = new EventListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                eventMainObj.setId(jsonObj.getString("id"));
                                eventMainObj.setDescription(jsonObj.getString("description"));
                                eventMainObj.setStatus(jsonObj.getString("status"));
                                eventMainObj.setDate(jsonObj.getString("date"));
                                eventMainObj.setClient_id(jsonObj.getString("client_id"));
                                eventMainObj.setClient_name(jsonObj.getString("client_name"));
                                eventMainObj.setClient_mobile(jsonObj.getString("client_mobile"));
                                eventMainObj.setClient_whatsapp(jsonObj.getString("client_whatsapp"));
                                eventMainObj.setInsurance_company(jsonObj.getString("insurance_company"));
                                eventMainObj.setPremium_amount(jsonObj.getString("premium_amount"));
                                eventMainObj.setInsurance_policy_number(jsonObj.getString("insurance_policy_number"));

                                premiumDueList.add(eventMainObj);
                            }
                            if (premiumDueList.size() == 0) {
                                ll_nothingtoshow.setVisibility(View.VISIBLE);
                                rv_premiumdue.setVisibility(View.GONE);
                                cb_checkall.setVisibility(View.GONE);
                            } else {
                                rv_premiumdue.setVisibility(View.VISIBLE);
                                ll_nothingtoshow.setVisibility(View.GONE);
                                cb_checkall.setVisibility(View.VISIBLE);
                            }
                            rv_premiumdue.setAdapter(new GetEventListAdapter());
                        }
                    } else {
                        ll_nothingtoshow.setVisibility(View.VISIBLE);
                        rv_premiumdue.setVisibility(View.GONE);
                        cb_checkall.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                ll_nothingtoshow.setVisibility(View.VISIBLE);
                rv_premiumdue.setVisibility(View.GONE);
                cb_checkall.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    public class GetEventListAdapter extends RecyclerView.Adapter<GetEventListAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_row_birthannidays, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            EventListPojo annivarsaryDetails = new EventListPojo();
            annivarsaryDetails = premiumDueList.get(position);
            String client_name = annivarsaryDetails.getClient_name();
            String client_mobile = annivarsaryDetails.getClient_mobile();
            holder.tv_clientname.setText(annivarsaryDetails.getDescription());

            holder.cb_wish.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public void onClick(View v) {

                    if (holder.cb_wish.isChecked())
                        premiumDueList.get(position).setChecked(true);
                    else
                        premiumDueList.get(position).setChecked(false);

                    if (isAllValuesChecked(premiumDueList)) {
                        cb_checkall.setChecked(true);
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        cb_checkall.setChecked(false);
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }

                    if (isAtleastOneChecked(premiumDueList)) {
                        fab_wish_whatsapp.setVisibility(View.VISIBLE);
                        fab_wish_sms.setVisibility(View.VISIBLE);
                    } else {
                        fab_wish_whatsapp.setVisibility(View.GONE);
                        fab_wish_sms.setVisibility(View.GONE);
                    }
                }
            });

            holder.imv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {
                        context.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.getPackageName(), null)));
                        Utilities.showMessageString(context, "Please provide permission for making call");
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        alertDialogBuilder.setTitle("Make a Call");
                        alertDialogBuilder.setIcon(R.drawable.icon_call_24dp);
                        alertDialogBuilder.setMessage("Are you sure you want to call " + client_name + " ?");
                        alertDialogBuilder.setCancelable(true);
                        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @SuppressLint("MissingPermission")
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                context.startActivity(new Intent(Intent.ACTION_CALL,
                                        Uri.parse("tel:" + client_mobile)));
                            }
                        });
                        alertDialogBuilder.setNegativeButton(
                                "No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert11 = alertDialogBuilder.create();
                        alert11.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                        alert11.show();
                    }
                }
            });

            holder.imv_sms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        //    new GetPremiumMessage().execute(user_id, premiumDueList.get(position).getClient_id(), "SMS");
                        // showDialog(premiumDueList.get(position).getClient_id(),"SMS", "holder");
                        confirmDialogForSMS(premiumDueList.get(position).getId(), "selected client", "single");

                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

            holder.imv_whatsapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        //   new GetPremiumMessage().execute(user_id, premiumDueList.get(position).getClient_id(), "WHATSAPP");
                        // showDialog(premiumDueList.get(position).getClient_id(),"WHATSAPP", "holder");
                        confirmDialogForWhatsapp(premiumDueList.get(position).getId(), "selected client.", "single");


                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });

            EventListPojo finalAnnivarsaryDetails = annivarsaryDetails;
            holder.imv_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utilities.isInternetAvailable(context)) {
                        shareImage(client_name, finalAnnivarsaryDetails.getInsurance_company(), finalAnnivarsaryDetails.getPremium_amount(), finalAnnivarsaryDetails.getInsurance_policy_number());
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return premiumDueList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv_clientname;
            private ImageView imv_sms, imv_whatsapp, imv_share, imv_call;
            private CheckBox cb_wish;

            public MyViewHolder(View view) {
                super(view);
                tv_clientname = view.findViewById(R.id.tv_clientname);
                imv_sms = view.findViewById(R.id.imv_sms);
                imv_whatsapp = view.findViewById(R.id.imv_whatsapp);
                imv_share = view.findViewById(R.id.imv_share);
                cb_wish = view.findViewById(R.id.cb_wish);
                imv_call = view.findViewById(R.id.imv_call);
            }
        }

        private boolean isAtleastOneChecked(ArrayList<EventListPojo> premiumDueList) {
            for (int i = 0; i < premiumDueList.size(); i++)
                if (premiumDueList.get(i).isChecked())
                    return true;
            return false;
        }

        private boolean isAllValuesChecked(ArrayList<EventListPojo> premiumDueList) {
            for (int i = 0; i < premiumDueList.size(); i++)
                if (!premiumDueList.get(i).isChecked())
                    return false;
            return true;
        }
    }


    public class GetPremiumMessage extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;
        private String singleReceiverID = "";
        private String messageType = "";

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
            //   singleReceiverID = params[1];
            //messageType = params[2];

            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getPremiummessage"));
            param.add(new ParamsPojo("user_id", params[0]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.SETTINGSAPI, param);
            return res.trim();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String type = "";
            try {
//                pd.dismiss();
                if (!result.equals("")) {
                    JSONObject mainObj = new JSONObject(result);
                    type = mainObj.getString("type");
                    message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("success")) {
                        JSONObject jsonObject = mainObj.getJSONObject("result");
                        JSONArray jsonarr = jsonObject.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                id = jsonObj.getString("id");
                                message = jsonObj.getString("message");
                            }
                        }
                        whatsappPicUrl = jsonObject.getString("url");
                        Uri uri = Uri.parse(whatsappPicUrl);
                        whatsappPic = uri.getLastPathSegment();

                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void confirmDialogForWhatsapp(String singleReceiverID, String name, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.icons_success_color);
        builder.setTitle("Confirm");
        builder.setMessage("WhatsApp message will be sent to " + name);
        builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendWhatsapp("", "", singleReceiverID);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertD = builder.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    private void confirmDialogForSMS(String singleReceiverID, String name, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.icons_success_color);
        builder.setTitle("Confirm");
        builder.setMessage("Message will be sent to " + name);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendSMS("", singleReceiverID);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertD = builder.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    private void showDialog(String singleReceiverID, String messageType, String caller) {
        if (!caller.equals("share")) {
            if (messageType.equals("SMS")) {
                final EditText edt_smsmessage = new EditText(context);
                float dpi = context.getResources().getDisplayMetrics().density;
                //  edt_smsmessage.setText(message);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    edt_smsmessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    edt_smsmessage.setText(Html.fromHtml(message));
                }
                if (edt_smsmessage.getText().length() > 0)
                    edt_smsmessage.setSelection(edt_smsmessage.getText().length() - 1);
                else
                    edt_smsmessage.setSelection(0);

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setTitle("Send Message");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        sendSMS(edt_smsmessage.getText().toString().trim(), singleReceiverID);
//                            } else if (messageType.equals("WHATSAPP")) {
//                                sendWhatsapp(edt_smsmessage.getText().toString().trim(), whatsappPic, singleReceiverID);
//                            }
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
            } else if (messageType.equals("WHATSAPP")) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.prompt_send_whatsappmsg, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                alertDialogBuilder.setTitle("Whatsapp Message");
                alertDialogBuilder.setView(promptView);

                dialog_edt_whatsappmessage = promptView.findViewById(R.id.dialog_edt_whatsappmessage);
                dialog_imv_whatsapppic = promptView.findViewById(R.id.dialog_imv_whatsapppic);
                CheckBox cb_whatsappmsg = promptView.findViewById(R.id.cb_whatsappmsg);
                CheckBox cb_whatsappimg = promptView.findViewById(R.id.cb_whatsappimg);

                // dialog_edt_whatsappmessage.setText(message);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dialog_edt_whatsappmessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    dialog_edt_whatsappmessage.setText(Html.fromHtml(message));
                }
                if (!whatsappPicUrl.equals("")) {
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
            }
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


            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.prompt_send_whatsappmsg, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle("Whatsapp Message");
            alertDialogBuilder.setView(promptView);
            dialog_imv_whatsapppic = promptView.findViewById(R.id.dialog_imv_whatsapppic);
            dialog_edt_whatsappmessage = promptView.findViewById(R.id.dialog_edt_whatsappmessage);
            CheckBox cb_whatsappmsg = promptView.findViewById(R.id.cb_whatsappmsg);
            CheckBox cb_whatsappimg = promptView.findViewById(R.id.cb_whatsappimg);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dialog_edt_whatsappmessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
            } else {
                dialog_edt_whatsappmessage.setText(Html.fromHtml(message));
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

//                    String whatsappPicSend = "", whatsappMsgSend = "";
//
//
//                    whatsappMsgSend = dialog_edt_whatsappmessage.getText().toString().trim();
//                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
//                    share.setType("text/plain");
//                    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//                    share.putExtra(Intent.EXTRA_TEXT, whatsappMsgSend);
//                    startActivity(Intent.createChooser(share, "Share link!"));


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

    private void sendSMS(String message, String singleReceiverID) {
        JsonArray clientIdJSONArray = new JsonArray();

        if (singleReceiverID.equals("")) {
            for (int i = 0; i < premiumDueList.size(); i++) {
                if (premiumDueList.get(i).isChecked()) {
                    JsonObject childObj = new JsonObject();
                    childObj.addProperty("id", premiumDueList.get(i).getId());
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
                mainObj.addProperty("type", "premiumdueSMS");
                mainObj.add("client_id", clientIdJSONArray);
                mainObj.addProperty("message", message);
                mainObj.addProperty("date", date);
                mainObj.addProperty("user_id", user_id);

                if (Utilities.isInternetAvailable(context)) {
                    new SendAnniversarySMS().execute(mainObj.toString());
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


    private void sendWhatsapp(String message, String whatsappPic, String singleReceiverID) {
        JsonArray clientIdJSONArray = new JsonArray();

        if (singleReceiverID.equals("")) {
            for (int i = 0; i < premiumDueList.size(); i++) {
                if (premiumDueList.get(i).isChecked()) {
                    JsonObject childObj = new JsonObject();
                    childObj.addProperty("id", premiumDueList.get(i).getId());
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
                mainObj.addProperty("type", "premiumDue");
                mainObj.add("client_id", clientIdJSONArray);
                mainObj.addProperty("message", message);
                mainObj.addProperty("image", whatsappPic);
                mainObj.addProperty("user_id", user_id);
                mainObj.addProperty("date", date);

                if (Utilities.isInternetAvailable(context)) {
                    new SendAnniversaryWhatsapp().execute(mainObj.toString());
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

    private void shareImage(String client_name, String company, String premium_amount, String policy) {

//        showDialog("", "", "share");
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
        /*try {
            sign = new GetSignature().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.prompt_send_whatsappmsg, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle("Share Message");
        alertDialogBuilder.setView(promptView);
        dialog_imv_whatsapppic = promptView.findViewById(R.id.dialog_imv_whatsapppic);
        dialog_edt_whatsappmessage = promptView.findViewById(R.id.dialog_edt_whatsappmessage);
        CheckBox cb_whatsappmsg = promptView.findViewById(R.id.cb_whatsappmsg);
        CheckBox cb_whatsappimg = promptView.findViewById(R.id.cb_whatsappimg);
        dialog_imv_whatsapppic.setVisibility(View.GONE);
        cb_whatsappimg.setVisibility(View.GONE);
        cb_whatsappimg.setChecked(false);
        cb_whatsappmsg.setVisibility(View.GONE);

        dialog_edt_whatsappmessage.setText("Dear " + client_name + ",\n You have premium due for " + company + " policy with policy number : " + policy + ". Please pay the premium of amount Rs. " + premium_amount + ". If you have already paid the amount, please ignore the message.\n From " + sign + "");

       /* if (!whatsappPicUrl.equals("")) {
            isImageSet = true;
            Picasso.with(context)
                    .load(whatsappPicUrl)
                    .placeholder(R.drawable.img_photo)
                    .into(dialog_imv_whatsapppic);
        }*/


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
                     /*   share.putExtra(Intent.EXTRA_STREAM, uri);
                        if (cb_whatsappmsg.isChecked())
                            whatsappMsgSend = dialog_edt_whatsappmessage.getText().toString().trim();
                        share.putExtra(Intent.EXTRA_TEXT, whatsappMsgSend);
                        startActivity(Intent.createChooser(share, "Share Image!"));*/
                    } else {
                        // Utilities.showAlertDialog(context, "Information", "Please set the image in the Birthday Settings", false);
                    }
                } else if (cb_whatsappmsg.isChecked()) {
                    whatsappMsgSend = dialog_edt_whatsappmessage.getText().toString().trim();
                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    //added by Varsha
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    share.putExtra(Intent.EXTRA_TEXT, whatsappMsgSend);
                    startActivity(Intent.createChooser(share, "Share message!"));
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

            String result = "[]";
            JSONObject obj = new JSONObject();
            try {
                obj.put("type", "getSign");
                obj.put("userid", user_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            result = WebServiceCalls.JSONAPICall(ApplicationConstants.SIGNATURE, obj.toString());

            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            pd.dismiss();
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
