package com.insurance.todojee.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.SharedInsurance_Fragment;
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

public class ViewSharedLifeInsurance_Activity extends Activity {

    private Context context;
    private TextView tv_insurance_type, tv_insurance_company, tv_client_name, tv_insurer_name, tv_insurance_policy_no, tv_policy_type, tv_startenddate, tv_frequency, tv_sum_insured, tv_premium_amt, tv_policy_status, tv_link, tv_desc, tv_remark, tv_maturity_dates, tv_documents, tv_insurance_created_by, tv_enddate;
    private UserSessionManager session;
    private String user_id;
    private LinearLayout ll_parent, ll_documents;
    ImageView img_delete;

    private LifeGeneralInsuranceMainListPojo lifeInsuranceDetails;
    private List<LinearLayout> documentsLayoutsList;
    private File file, lifeInsurancePicFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_shared_life_insurance);
        init();
        setUpToolbar();
        getSessionData();
        setDefault();
        setEventHandler();
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        img_delete = findViewById(R.id.img_delete);
        mToolbar.setTitle("Shared Life Insurance");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void init() {
        context = ViewSharedLifeInsurance_Activity.this;
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);
        tv_insurance_type = findViewById(R.id.tv_insurance_type);
        tv_insurance_company = findViewById(R.id.tv_insurance_company);
        tv_client_name = findViewById(R.id.tv_client_name);
        tv_insurer_name = findViewById(R.id.tv_insurer_name);
        tv_insurance_policy_no = findViewById(R.id.tv_insurance_policy_no);
        tv_policy_type = findViewById(R.id.tv_policy_type);
        tv_startenddate = findViewById(R.id.tv_startenddate);
        tv_frequency = findViewById(R.id.tv_frequency);
        tv_sum_insured = findViewById(R.id.tv_sum_insured);
        tv_premium_amt = findViewById(R.id.tv_premium_amt);
        tv_policy_status = findViewById(R.id.tv_policy_status);
        tv_link = findViewById(R.id.tv_link);
        tv_desc = findViewById(R.id.tv_desc);
        tv_remark = findViewById(R.id.tv_remark);
        tv_maturity_dates = findViewById(R.id.tv_maturity_dates);
        tv_documents = findViewById(R.id.tv_documents);
        tv_insurance_created_by = findViewById(R.id.tv_insurance_created_by);
        ll_documents = findViewById(R.id.ll_documents);
        tv_enddate = findViewById(R.id.tv_enddate);
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

    private void setEventHandler() {
        img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setMessage("Are you sure you want to remove this item?");
                builder.setTitle("Alert");
                builder.setIcon(R.drawable.ic_alert_red_24dp);
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (Utilities.isInternetAvailable(context)) {
                            new DeleteSharedInsuranceDetails().execute(lifeInsuranceDetails.getId());
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
        lifeInsuranceDetails = (LifeGeneralInsuranceMainListPojo) getIntent().getSerializableExtra("lifeInsuranceDetails");

        tv_insurance_type.setText("Life Insurance");
        tv_insurance_created_by.setText(lifeInsuranceDetails.getLic_created_by());
        tv_insurance_created_by.setText(lifeInsuranceDetails.getLic_created_by());
        tv_insurance_company.setText(lifeInsuranceDetails.getInsurance_company_name());
        tv_client_name.setText(lifeInsuranceDetails.getClient_name());
        tv_insurer_name.setText(lifeInsuranceDetails.getInsurer_family_name());
        tv_insurance_policy_no.setText(lifeInsuranceDetails.getPolicy_no());
        tv_policy_type.setText(lifeInsuranceDetails.getPolicy_type());
        tv_startenddate.setText(changeDateFormat("dd-MM-yyyy",
                "dd/MM/yyyy",
                lifeInsuranceDetails.getStart_date()));
        tv_enddate.setText(changeDateFormat("dd-MM-yyyy",
                "dd/MM/yyyy",
                lifeInsuranceDetails.getEnd_date()));
        tv_frequency.setText(lifeInsuranceDetails.getFrequency());
        tv_sum_insured.setText("Rs. " + lifeInsuranceDetails.getSum_insured());
        tv_premium_amt.setText("Rs. " + lifeInsuranceDetails.getPremium_amount());
        tv_policy_status.setText(lifeInsuranceDetails.getPolicy_status());
        tv_link.setText(lifeInsuranceDetails.getLink());
        tv_link.setMovementMethod(LinkMovementMethod.getInstance());
        tv_desc.setText(lifeInsuranceDetails.getDescription());
        tv_remark.setText(lifeInsuranceDetails.getRemark());

        ArrayList<LifeGeneralInsuranceMainListPojo.MaturityDatesListPojo> maturityDatesList = new ArrayList<>();
        maturityDatesList = lifeInsuranceDetails.getMaturity_date();
        if (maturityDatesList.size() != 0) {
            String dates = "";
            for (int i = 0; i < maturityDatesList.size(); i++) {
                dates = dates + changeDateFormat("yyyy-MM-dd",
                        "dd/MM/yyyy",
                        maturityDatesList.get(i).getMaturity_date()) + " \n ";
            }
            tv_maturity_dates.setText(dates);
        } else {
            tv_maturity_dates.setText("No Maturity Dates Added");
        }

        ArrayList<LifeGeneralInsuranceMainListPojo.DocumentListPojo> documentsList = new ArrayList<>();
        documentsList = lifeInsuranceDetails.getDocument();

        if (documentsList.size() != 0) {
            for (int i = 0; i < documentsList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.list_row_documents, null);
                documentsLayoutsList.add((LinearLayout) rowView);
                ll_documents.addView(rowView, ll_documents.getChildCount());
                ((TextView) documentsLayoutsList.get(i).findViewById(R.id.edt_doc_name)).setText("Download Document " + (i + 1));
                ((TextView) documentsLayoutsList.get(i).findViewById(R.id.edt_selectdocuments)).setText(documentsList.get(i).getDocument());

            }
        } else {
            tv_documents.setText("No Documents Added");
        }


    }

    public void viewDocument(View view) {

        if (Utilities.isInternetAvailable(context)) {
            //TextView edt_selectdocuments = (TextView) view;

            LinearLayout ll_layout = (LinearLayout) view;
            TextView edt_selectdocuments = ll_layout.findViewById(R.id.edt_selectdocuments);
            new DownloadDocument().execute(edt_selectdocuments.getText().toString().trim());
            // Utilities.showSnackBar(ll_parent, "doc selected " +edt_selectdocuments.getText().toString().trim());

        } else {

        }

    }

    public class DeleteSharedInsuranceDetails extends AsyncTask<String, Void, String> {
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
            obj.addProperty("type", "deleteShared");
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


                        new SharedInsurance_Fragment.GetsharedInsurance().execute(user_id);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Insurance Details Removed Successfully");
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
}
