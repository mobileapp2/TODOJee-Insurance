package com.insurance.todojee.ccavenue;

import android.app.ProgressDialog;
import android.content.Context;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.models.CCAvenueModel;
import com.insurance.todojee.utilities.ApplicationConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WebViewActivity extends AppCompatActivity {
    Intent mainIntent;
    String encVal;
    String vResponse;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_web_view);
        mainIntent = getIntent();

//get rsa key method
        get_RSA_key(mainIntent.getStringExtra(AvenuesParams.ACCESS_CODE), mainIntent.getStringExtra(AvenuesParams.ORDER_ID));
    }

    private class RenderView extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            LoadingDialog.showLoadingDialog(WebViewActivity.this, "Loading...");

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

                        if (Utilities.isInternetAvailable(WebViewActivity.this)) {
                            new AddClientDetails().execute(mainObj.toString());
                        } else {
                            Utilities.showMessageString(WebViewActivity.this, "Please Check Internet Connection");
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
                    LoadingDialog.showLoadingDialog(WebViewActivity.this, "Loading...");
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
        LoadingDialog.showLoadingDialog(WebViewActivity.this, "Loading...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, mainIntent.getStringExtra(AvenuesParams.RSA_KEY_URL),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(WebViewActivity.this,response,Toast.LENGTH_LONG).show();
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
                        //Toast.makeText(WebViewActivity.this,error.toString(),Toast.LENGTH_LONG).show();
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
                WebViewActivity.this).create();

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

    public class AddClientDetails extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(WebViewActivity.this, R.style.CustomDialogTheme);
            pd.setMessage("Please wait ...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
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
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        JSONObject obj = jsonarr.getJSONObject(0);

                        AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this, R.style.CustomDialogTheme);
                        builder.setMessage("Plan buy successfully!");
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

    private void AlertDialog(String status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this, R.style.CustomDialogTheme);
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
