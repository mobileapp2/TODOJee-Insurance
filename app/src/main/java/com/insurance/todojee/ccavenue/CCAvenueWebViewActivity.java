package com.insurance.todojee.ccavenue;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CCAvenueWebViewActivity extends AppCompatActivity {
    Intent mainIntent;
    String encVal;
    String vResponse, user_id;
    private UserSessionManager session;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_ccavenue_webview);
        mainIntent = getIntent();
        session = new UserSessionManager(CCAvenueWebViewActivity.this);

        getSessionData();

//get rsa key method
        get_RSA_key(mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE), mainIntent.getStringExtra(AvenuesParams.ORDER_ID));
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

    private class RenderView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            LoadingDialog.showLoadingDialog(CCAvenueWebViewActivity.this, "Loading...");

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if (!ServiceUtility.chkNull(vResponse).equals("")
                    && ServiceUtility.chkNull(vResponse).toString().indexOf("ERROR") == -1) {
                StringBuffer vEncVal = new StringBuffer("");
                vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.AMOUNT, mainIntent.getStringExtra(AvenuesParams.AMOUNT)));
                vEncVal.append(ServiceUtility.addToPostParams(AvenuesParams.CURRENCY, mainIntent.getStringExtra(AvenuesParams.CURRENCY)));
                encVal = RSAUtility.encrypt(vEncVal.substring(0, vEncVal.length() - 1), vResponse);  //encrypt amount and currency
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            LoadingDialog.cancelLoading();

            @SuppressWarnings("unused")
            class MyJavaScriptInterface {
                @JavascriptInterface
                public void processHTML(String html) {
                    // process the html source code to get final status of transaction
                    String status = null;
                    if (html.indexOf("Failure") != -1) {
                        AlertDialog("Transaction Declined!");
                    } else if (html.indexOf("Success") != -1) {

                        Document doc = Jsoup.parse(html);
                        Elements tableElements = doc.select("table");
                        Elements tableRowElements = tableElements.select(":not(thead) tr");
                        ArrayList<HashMap<String, String>> arraylist = new ArrayList();


                        HashMap<String, String> map1 = new HashMap<String, String>();
                        HashMap<String, String> map2 = new HashMap<String, String>();
                        HashMap<String, String> map3 = new HashMap<String, String>();
                        HashMap<String, String> map4 = new HashMap<String, String>();
                        HashMap<String, String> map5 = new HashMap<String, String>();
                        HashMap<String, String> map6 = new HashMap<String, String>();
                        HashMap<String, String> map7 = new HashMap<String, String>();
                        HashMap<String, String> map8 = new HashMap<String, String>();
                        HashMap<String, String> map9 = new HashMap<String, String>();

                        map1.put("key", "type");
                        map1.put("value", "buyPlan");
                        arraylist.add(map1);

                        map2.put("key", "user_id");
                        map2.put("value", mainIntent.getStringExtra("user_id"));
                        arraylist.add(map2);

                        map3.put("key", "plan_id");
                        map3.put("value", mainIntent.getStringExtra("plan_id"));
                        arraylist.add(map3);

                        map4.put("key", "space");
                        map4.put("value", mainIntent.getStringExtra("space"));
                        arraylist.add(map4);

                        map5.put("key", "sms");
                        map5.put("value", mainIntent.getStringExtra("sms"));
                        arraylist.add(map5);

                        map6.put("key", "whatsApp_msg");
                        map6.put("value", mainIntent.getStringExtra("whatsApp_msg"));
                        arraylist.add(map6);

                        map7.put("key", "expire_date");
                        map7.put("value", mainIntent.getStringExtra("expire_date"));
                        arraylist.add(map7);

                        map8.put("key", "cc_bank_ref_no");
                        map8.put("value", "");
                        arraylist.add(map8);

                        map9.put("key", "record_status");
                        map9.put("value", "");
                        arraylist.add(map9);

                        for (int i = 0; i < tableRowElements.size(); i++) {
                            HashMap<String, String> map = new HashMap<String, String>();

                            Element row = tableRowElements.get(i);
                            Elements rowItems = row.select("td");

                            map.put("key", rowItems.get(0).text());
                            map.put("value", rowItems.get(1).text());
                            arraylist.add(map);
                        }


                        JsonObject mainObj = new JsonObject();
                        for (HashMap<String, String> entry : arraylist) {
                            String myID = entry.get("key").toString();
                            String mySKU = entry.get("value").toString();
                            mainObj.addProperty(myID, mySKU);
                        }

                        Log.i("CCAVENUE", mainObj.toString());

                        if (Utilities.isInternetAvailable(CCAvenueWebViewActivity.this)) {
                            new BuyPlan().execute(mainObj.toString());
                        } else {
                            Utilities.showMessageString(CCAvenueWebViewActivity.this, "Please Check Internet Connection");
                        }

                    } else if (html.indexOf("Aborted") != -1) {
                        AlertDialog("Transaction Cancelled!");
                    } else {
                        AlertDialog("Status Not Known!");
                    }

                }
            }

            final WebView webview = (WebView) findViewById(R.id.webview);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(webview, url);
                    LoadingDialog.cancelLoading();
                    if (url.indexOf("/ccavResponseHandler.php") != -1) {
                        webview.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    LoadingDialog.showLoadingDialog(CCAvenueWebViewActivity.this, "Loading...");
                }
            });


            try {
                String postData = AvenuesParams.ACCESS_CODE + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE), "UTF-8") + "&" + AvenuesParams.MERCHANT_ID + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.MERCHANT_ID), "UTF-8") + "&" + AvenuesParams.ORDER_ID + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.ORDER_ID), "UTF-8") + "&" + AvenuesParams.REDIRECT_URL + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.REDIRECT_URL), "UTF-8") + "&" + AvenuesParams.CANCEL_URL + "=" + URLEncoder.encode(mainIntent.getStringExtra(AvenuesParams.CANCEL_URL), "UTF-8") + "&" + AvenuesParams.ENC_VAL + "=" + URLEncoder.encode(encVal, "UTF-8");
                webview.postUrl(ApplicationConstants.TRANS_URL, postData.getBytes());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    public void get_RSA_key(final String ac, final String od) {
        LoadingDialog.showLoadingDialog(CCAvenueWebViewActivity.this, "Loading...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, mainIntent.getStringExtra(AvenuesParams.RSA_KEY_URL),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(CCAvenueWebViewActivity.this,response,Toast.LENGTH_LONG).show();
                        LoadingDialog.cancelLoading();

                        if (response != null && !response.equals("")) {
                            vResponse = response;     ///save retrived rsa key
                            if (vResponse.contains("!ERROR!")) {
                                show_alert(vResponse);
                            } else {
                                new RenderView().execute();   // Calling async task to get display content
                            }


                        } else {
                            show_alert("No response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LoadingDialog.cancelLoading();
                        //Toast.makeText(CCAvenueWebViewActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(AvenuesParams.ACCESS_CODE, ac);
                params.put(AvenuesParams.ORDER_ID, od);
                return params;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void show_alert(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(
                CCAvenueWebViewActivity.this).create();

        alertDialog.setTitle("Error!!!");
        if (msg.contains("\n"))
            msg = msg.replaceAll("\\\n", "");

        alertDialog.setMessage(msg);


        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });


        alertDialog.show();
    }

    public class BuyPlan extends AsyncTask<String, Void, String> {
        ProgressDialog pd;
        private String JSONString = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(CCAvenueWebViewActivity.this, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";

            JSONString = params[0];
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.PLANLISTAPI, params[0]);
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

//                        AlertDialog.Builder builder = new AlertDialog.Builder(CCAvenueWebViewActivity.this, R.style.CustomDialogTheme);
//                        builder.setMessage("Plan buy successfully!");
//                        builder.setIcon(R.drawable.ic_success_24dp);
//                        builder.setTitle("Success");
//                        builder.setCancelable(false);
//                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                finish();
//                            }
//                        });
//                        AlertDialog alertD = builder.create();
//                        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
//                        alertD.show();
                        if (Utilities.isInternetAvailable(CCAvenueWebViewActivity.this)) {
                            new UpdateCounts().execute(JSONString);
                        } else {
                            Utilities.showMessageString(CCAvenueWebViewActivity.this, "Please Check Internet Connection");
                        }


                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    public class UpdateCounts extends AsyncTask<String, Void, String> {
        ProgressDialog pd;
        private String JSONString = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(CCAvenueWebViewActivity.this, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONString = params[0];
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getCounts"));
            param.add(new ParamsPojo("user_id", user_id));
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
                        JSONArray jsonArray = mainObj.getJSONArray("counts");
                        JSONArray user_info = null;
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        try {
                            user_info = new JSONArray(session.getUserDetails().get(
                                    ApplicationConstants.KEY_LOGIN_INFO));
                            JSONObject json = user_info.getJSONObject(0);
                            json.put("smsCount", jsonObject.getString("smsCount"));
                            json.put("whatsAppCount", jsonObject.getString("whatsAppCount"));
                            json.put("smsLimit", jsonObject.getString("smsLimit"));
                            json.put("whatsAppLimit", jsonObject.getString("whatsAppLimit"));
                            json.put("customerCount", jsonObject.getString("customerCount"));
                            json.put("customerLimit", jsonObject.getString("customerLimit"));
                            json.put("policyCount", jsonObject.getString("policyCount"));
                            json.put("policyLimit", jsonObject.getString("policyLimit"));
                            session.updateSession(user_info.toString());

                            startActivity(new Intent(CCAvenueWebViewActivity.this, PlanBuySuccess_Activity.class)
                                    .putExtra("JSONString", JSONString)
                                    .putExtra("validity", getIntent().getStringExtra("validity"))
                                    .putExtra("clients", getIntent().getStringExtra("clients"))
                                    .putExtra("policies", getIntent().getStringExtra("policies")));
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    private void AlertDialog(String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CCAvenueWebViewActivity.this, R.style.CustomDialogTheme);
        builder.setMessage(status);
        builder.setIcon(R.drawable.ic_alert_red_24dp);
        builder.setTitle("Fail");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        AlertDialog alertD = builder.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

}
