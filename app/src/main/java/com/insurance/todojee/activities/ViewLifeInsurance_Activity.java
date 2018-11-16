package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.LifeInsurance_Fragment;
import com.insurance.todojee.models.LifeGeneralInsuranceMainListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.insurance.todojee.utilities.Utilities.changeDateFormat;

public class ViewLifeInsurance_Activity extends Activity {

    private Context context;
    private LinearLayout ll_parent;
    private EditText edt_insurancecompany, edt_clientname, edt_insurername, edt_insurepolicyno, edt_policytype,
            edt_startdate, edt_enddate, edt_frequency, edt_suminsured, edt_premiumamt, edt_policystatus,
            edt_link, edt_description, edt_remark;
    private LinearLayout ll_maturitydates, ll_documents;
    private ImageView img_delete, img_edit;
    private TextView tv_maturitydate, tv_documents;

    private LifeGeneralInsuranceMainListPojo lifeInsuranceDetails;

    private List<LinearLayout> maturityDatesLayoutsList;
    private List<LinearLayout> documentsLayoutsList;

    private UserSessionManager session;
    private String user_id, callType;

    private File file, lifeInsurancePicFolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lifeinsurance);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    private void init() {
        context = ViewLifeInsurance_Activity.this;
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);

        edt_clientname = findViewById(R.id.edt_clientname);
        edt_insurancecompany = findViewById(R.id.edt_insurancecompany);
        edt_insurername = findViewById(R.id.edt_insurername);
        edt_insurepolicyno = findViewById(R.id.edt_insurepolicyno);
        edt_policytype = findViewById(R.id.edt_policytype);
        edt_startdate = findViewById(R.id.edt_startdate);
        edt_enddate = findViewById(R.id.edt_enddate);
        edt_frequency = findViewById(R.id.edt_frequency);
        edt_suminsured = findViewById(R.id.edt_suminsured);
        edt_premiumamt = findViewById(R.id.edt_premiumamt);
        edt_policystatus = findViewById(R.id.edt_policystatus);
        edt_link = findViewById(R.id.edt_link);
        edt_description = findViewById(R.id.edt_description);
        edt_remark = findViewById(R.id.edt_remark);

        ll_maturitydates = findViewById(R.id.ll_maturitydates);
        ll_documents = findViewById(R.id.ll_documents);

        tv_maturitydate = findViewById(R.id.tv_maturitydate);
        tv_documents = findViewById(R.id.tv_documents);

        maturityDatesLayoutsList = new ArrayList<>();
        documentsLayoutsList = new ArrayList<>();


        lifeInsurancePicFolder = new File(Environment.getExternalStorageDirectory() + "/Insurance/" + "Life Insurance");
        if (!lifeInsurancePicFolder.exists())
            lifeInsurancePicFolder.mkdirs();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
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
        lifeInsuranceDetails = (LifeGeneralInsuranceMainListPojo) getIntent().getSerializableExtra("lifeInsuranceDetails");

        edt_clientname.setText(lifeInsuranceDetails.getClient_name());
        edt_insurancecompany.setText(lifeInsuranceDetails.getInsurance_company_name());
        edt_insurername.setText(lifeInsuranceDetails.getInsurer_family_name());
        edt_insurepolicyno.setText(lifeInsuranceDetails.getPolicy_no());
        edt_policytype.setText(lifeInsuranceDetails.getPolicy_type());
        edt_startdate.setText(changeDateFormat("dd-MM-yyyy",
                "dd/MM/yyyy",
                lifeInsuranceDetails.getStart_date()));
        edt_enddate.setText(changeDateFormat("dd-MM-yyyy",
                "dd/MM/yyyy",
                lifeInsuranceDetails.getEnd_date()));
        edt_frequency.setText(lifeInsuranceDetails.getFrequency());
        edt_suminsured.setText(lifeInsuranceDetails.getSum_insured());
        edt_premiumamt.setText(lifeInsuranceDetails.getPremium_amount());
        edt_policystatus.setText(lifeInsuranceDetails.getPolicy_status());
        edt_link.setText(lifeInsuranceDetails.getLink());
        edt_description.setText(lifeInsuranceDetails.getDescription());
        edt_remark.setText(lifeInsuranceDetails.getRemark());

        ArrayList<LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo> maturityDatesList = new ArrayList<>();
        maturityDatesList = lifeInsuranceDetails.getMaturity_date();

        if (maturityDatesList.size() != 0) {
            for (int i = 0; i < maturityDatesList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_viewmaturitydates, null);
                maturityDatesLayoutsList.add((LinearLayout) rowView);
                ll_maturitydates.addView(rowView, ll_maturitydates.getChildCount());

                ((EditText) maturityDatesLayoutsList.get(i).findViewById(R.id.edt_maturitydate)).setText(changeDateFormat("yyyy-MM-dd",
                        "dd/MM/yyyy",
                        maturityDatesList.get(i).getMaturity_date()));
                ((EditText) maturityDatesLayoutsList.get(i).findViewById(R.id.edt_remark)).setText(maturityDatesList.get(i).getRemark());

            }
        } else {
            tv_maturitydate.setText("No Maturity Dates Added");
        }


        ArrayList<LifeGeneralInsuranceMainListPojo.DocumentListPojo> documentsList = new ArrayList<>();
        documentsList = lifeInsuranceDetails.getDocument();

        if (documentsList.size() != 0) {
            for (int i = 0; i < documentsList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_viewdocuments, null);
                documentsLayoutsList.add((LinearLayout) rowView);
                ll_documents.addView(rowView, ll_documents.getChildCount());
//                Uri uri = Uri.parse(documentsList.get(i).getDocument());
//                String document_name = uri.getLastPathSegment();
//                ((EditText) documentsLayoutsList.get(i).findViewById(R.id.edt_documentname)).setText(document_name);
                ((EditText) documentsLayoutsList.get(i).findViewById(R.id.edt_selectdocuments)).setText(documentsList.get(i).getDocument());

            }
        } else {
            tv_documents.setText("No Documents Added");
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
                            new DeleteLifeInsuranceDetails().execute(lifeInsuranceDetails.getId());
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

                Intent intent = new Intent(context, EditLifeInsurance_Activity.class);
                intent.putExtra("lifeInsuranceDetails", lifeInsuranceDetails);
                context.startActivity(intent);
                finish();

            }
        });
    }

    public void viewDocument(View view) {

        if (Utilities.isInternetAvailable(context)) {
            EditText edt_selectdocuments = (EditText) view;
            new DownloadDocument().execute(edt_selectdocuments.getText().toString().trim());
        } else {

        }

    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        callType = getIntent().getStringExtra("TYPE");
        img_delete = findViewById(R.id.img_delete);
        img_edit = findViewById(R.id.img_edit);

        if (callType.equals("NONFILTER")) {
            img_delete.setVisibility(View.VISIBLE);
            img_edit.setVisibility(View.VISIBLE);
        } else if (callType.equals("FILTER")) {
            img_delete.setVisibility(View.GONE);
            img_edit.setVisibility(View.GONE);
        }

        mToolbar.setTitle("Life Insurance Details");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class DownloadDocument extends AsyncTask<String, Integer, Boolean> {
        int lenghtOfFile = -1;
        int count = 0;
        int content = -1;
        int counter = 0;
        int progress = 0;
        URL downloadurl = null;
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(context, R.style.CustomDialogTheme);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage("Downloading Document");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            int read = -1;
            byte[] buffer = new byte[1024];
            FileOutputStream fileOutputStream = null;
            long total = 0;


            try {
                downloadurl = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) downloadurl.openConnection();
                lenghtOfFile = httpURLConnection.getContentLength();
                inputStream = httpURLConnection.getInputStream();

                file = new File(lifeInsurancePicFolder, Uri.parse(params[0]).getLastPathSegment());
                fileOutputStream = new FileOutputStream(file);
                while ((read = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                    counter = counter + read;
                    publishProgress(counter);
                }
                success = true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return success;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress = (int) (((double) values[0] / lenghtOfFile) * 100);
            mProgressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mProgressDialog.dismiss();
            super.onPostExecute(aBoolean);
            if (aBoolean == true) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("file://" + file);
                if (downloadurl.toString().contains(".doc") || downloadurl.toString().contains(".docx")) {
                    // Word document
                    intent.setDataAndType(uri, "application/msword");
                } else if (downloadurl.toString().contains(".pdf")) {
                    // PDF file
                    intent.setDataAndType(uri, "application/pdf");
                } else if (downloadurl.toString().contains(".ppt") || downloadurl.toString().contains(".pptx")) {
                    // Powerpoint file
                    intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                } else if (downloadurl.toString().contains(".xls") || downloadurl.toString().contains(".xlsx")) {
                    // Excel file
                    intent.setDataAndType(uri, "application/vnd.ms-excel");
                } else if (downloadurl.toString().contains(".zip") || downloadurl.toString().contains(".rar")) {
                    // WAV audio file
                    intent.setDataAndType(uri, "application/x-wav");
                } else if (downloadurl.toString().contains(".rtf")) {
                    // RTF file
                    intent.setDataAndType(uri, "application/rtf");
                } else if (downloadurl.toString().contains(".wav") || downloadurl.toString().contains(".mp3")) {
                    // WAV audio file
                    intent.setDataAndType(uri, "audio/x-wav");
                } else if (downloadurl.toString().contains(".gif")) {
                    // GIF file
                    intent.setDataAndType(uri, "image/gif");
                } else if (downloadurl.toString().contains(".jpg") || downloadurl.toString().contains(".jpeg") || downloadurl.toString().contains(".png")) {
                    // JPG file
                    intent.setDataAndType(uri, "image/jpeg");
                } else if (downloadurl.toString().contains(".txt")) {
                    // Text file
                    intent.setDataAndType(uri, "text/plain");
                } else if (downloadurl.toString().contains(".3gp") || downloadurl.toString().contains(".mpg") || downloadurl.toString().contains(".mpeg") || downloadurl.toString().contains(".mpe") || downloadurl.toString().contains(".mp4") || downloadurl.toString().contains(".avi")) {
                    // Video files
                    intent.setDataAndType(uri, "video/*");
                } else {
                    intent.setDataAndType(uri, "*/*");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        }
    }

    public class DeleteLifeInsuranceDetails extends AsyncTask<String, Void, String> {
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
            obj.addProperty("id", params[0]);
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.INSURANCEAPI, obj.toString());
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

                        new LifeInsurance_Fragment.GetLifeInsurance().execute(user_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Life Insurance Details Deleted Successfully");
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
