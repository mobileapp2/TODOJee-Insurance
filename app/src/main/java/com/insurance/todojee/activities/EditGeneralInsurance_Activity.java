package com.insurance.todojee.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.insurance.todojee.R;
import com.insurance.todojee.fragments.GeneralInsurance_Fragment;
import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.models.FamilyInsurerNameListPojo;
import com.insurance.todojee.models.FirmInsurerNameListPojo;
import com.insurance.todojee.models.FrequencyListPojo;
import com.insurance.todojee.models.InsuranceCompanyListPojo;
import com.insurance.todojee.models.LifeGeneralInsuranceMainListPojo;
import com.insurance.todojee.models.PolicyTypeListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.MultipartUtility;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.insurance.todojee.utilities.PermissionUtil.doesAppNeedPermissions;
import static com.insurance.todojee.utilities.Utilities.changeDateFormat;
import static com.insurance.todojee.utilities.Utilities.hideSoftKeyboard;

public class EditGeneralInsurance_Activity extends Activity {

    private Context context;
    private ScrollView scrollView;
    private LinearLayout ll_parent;
    private LinearLayout ll_documents;
    private ImageView btn_adddocuments;
    private RadioButton rb_individual, rb_firm;
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;
    private EditText edt_insurancecompany, edt_clientname, edt_insurername, edt_insurepolicyno, edt_policytype,
            edt_startdate, edt_enddate, edt_frequency, edt_suminsured, edt_premiumamt, edt_link, edt_description, edt_remark;

    private int mYear, mMonth, mDay;
    private int mYear1, mMonth1, mDay1;
    private EditText edt_selectdocuments = null;
    private ImageView img_save;
    private List<LinearLayout> documentsLayoutsList;
    private ArrayList<ClientMainListPojo> clientList;
    private ArrayList<InsuranceCompanyListPojo> companyList;
    private ArrayList<FamilyInsurerNameListPojo> familyInsurerList;
    private ArrayList<FirmInsurerNameListPojo> firmInsurerList;
    private ArrayList<PolicyTypeListPojo> policyTypeList;
    private ArrayList<FrequencyListPojo> frequencyList;
    private UserSessionManager session;
    private String companyAliasName = "";
    private String user_id, id, companyId, clientId, insurerId, policyTypeID = "0", frequencyId, insurerTypeId, insurerType;
    private String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private Uri photoURI;
    private File photoFile, generalInsurancePicFolder;
    private LifeGeneralInsuranceMainListPojo generalInsuranceDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_generalinsurance);

        init();
        setUpToolbar();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftKeyboard(EditGeneralInsurance_Activity.this);
    }

    private void init() {
        context = EditGeneralInsurance_Activity.this;
        session = new UserSessionManager(context);
        scrollView = findViewById(R.id.scrollView);
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
        edt_link = findViewById(R.id.edt_link);
        edt_description = findViewById(R.id.edt_description);
        edt_remark = findViewById(R.id.edt_remark);

        rb_individual = findViewById(R.id.rb_individual);
        rb_firm = findViewById(R.id.rb_firm);

        ll_documents = findViewById(R.id.ll_documents);

        btn_adddocuments = findViewById(R.id.btn_adddocuments);

        generalInsurancePicFolder = new File(Environment.getExternalStorageDirectory() + "/Insurance/" + "General Insurance");
        if (!generalInsurancePicFolder.exists())
            generalInsurancePicFolder.mkdirs();

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

        Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        mYear1 = cal.get(Calendar.YEAR);
        mMonth1 = cal.get(Calendar.MONTH);
        mDay1 = cal.get(Calendar.DAY_OF_MONTH);

        companyList = new ArrayList<>();
        clientList = new ArrayList<>();
        familyInsurerList = new ArrayList<>();
        firmInsurerList = new ArrayList<>();
        policyTypeList = new ArrayList<>();
        frequencyList = new ArrayList<>();

        documentsLayoutsList = new ArrayList<>();


        generalInsuranceDetails = (LifeGeneralInsuranceMainListPojo) getIntent().getSerializableExtra("generalInsuranceDetails");

        id = generalInsuranceDetails.getId();
        companyId = generalInsuranceDetails.getInsurance_company_id();
        clientId = generalInsuranceDetails.getClient_id();
        insurerId = generalInsuranceDetails.getInsurer_id();
        policyTypeID = generalInsuranceDetails.getPolicy_type_id();
        frequencyId = generalInsuranceDetails.getFrequency_id();
        insurerType = generalInsuranceDetails.getInsurer_type_id();

        edt_clientname.setText(generalInsuranceDetails.getClient_name());
        edt_insurancecompany.setText(generalInsuranceDetails.getInsurance_company_name());
        edt_insurepolicyno.setText(generalInsuranceDetails.getPolicy_no());
        edt_policytype.setText(generalInsuranceDetails.getPolicy_type());
        edt_startdate.setText(changeDateFormat("dd-MM-yyyy",
                "dd/MM/yyyy",
                generalInsuranceDetails.getStart_date()));
        edt_enddate.setText(changeDateFormat("dd-MM-yyyy",
                "dd/MM/yyyy",
                generalInsuranceDetails.getEnd_date()));
        edt_frequency.setText(generalInsuranceDetails.getFrequency());
        edt_suminsured.setText(generalInsuranceDetails.getSum_insured());
        edt_premiumamt.setText(generalInsuranceDetails.getPremium_amount());
        edt_link.setText(generalInsuranceDetails.getLink());
//        edt_description.setText(generalInsuranceDetails.getDescription());
        edt_remark.setText(generalInsuranceDetails.getRemark());

        if (insurerType.equals("R")) {
            insurerTypeId = "2";
            rb_individual.setChecked(true);
            edt_insurername.setText(generalInsuranceDetails.getInsurer_family_name());
        } else if (insurerType.equals("F")) {
            insurerTypeId = "1";
            rb_firm.setChecked(true);
            edt_insurername.setText(generalInsuranceDetails.getInsurer_firm_name());
        }

        ArrayList<LifeGeneralInsuranceMainListPojo.DocumentListPojo> documentsList = new ArrayList<>();
        documentsList = generalInsuranceDetails.getDocument();

        if (documentsList.size() != 0) {
            for (int i = 0; i < documentsList.size(); i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_viewdocuments, null);
                documentsLayoutsList.add((LinearLayout) rowView);
                ll_documents.addView(rowView, ll_documents.getChildCount() - 1);
                Uri uri = Uri.parse(documentsList.get(i).getDocument());
                String document_name = uri.getLastPathSegment();
                ((EditText) documentsLayoutsList.get(i).findViewById(R.id.edt_selectdocuments)).setText(document_name);
            }
        }

    }

    private void companyListDialog(final ArrayList<InsuranceCompanyListPojo> companyListMain) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Insurance Company");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < companyListMain.size(); i++) {

            arrayAdapter.add(String.valueOf(companyListMain.get(i).getCompany_name()));
        }

        builderSingle.setNeutralButton("Add New", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                companyList = new ArrayList<>();
                Intent intent = new Intent(context, AddInsuranceCompany_Activity.class);
                intent.putExtra("TYPE", "1");
                startActivity(intent);
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                edt_policytype.setText("");
                policyTypeID = "0";

                edt_insurancecompany.setText(companyListMain.get(which).getCompany_name());
                companyId = companyListMain.get(which).getId();
                companyAliasName = companyListMain.get(which).getCompany_alias();

            }
        });
        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    private void clientListDialog(final ArrayList<ClientMainListPojo> clientListMain) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Client");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < clientListMain.size(); i++) {

            arrayAdapter.add(String.valueOf(clientListMain.get(i).getFirst_name()));
        }

        builderSingle.setNeutralButton("Add New", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clientList = new ArrayList<>();
                startActivity(new Intent(context, AddClientDetails_Activity.class));
            }
        });

        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                edt_insurername.setText("");
                insurerId = "";

                edt_clientname.setText(clientListMain.get(which).getFirst_name());
                clientId = clientListMain.get(which).getId();

            }
        });

        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    private void familyInsurerListDialog(final ArrayList<FamilyInsurerNameListPojo> familyInsurerList) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Insurer");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < familyInsurerList.size(); i++) {
            if (!familyInsurerList.get(i).getRelation().trim().equals("")) {
                arrayAdapter.add(familyInsurerList.get(i).getName()/* + " (" + familyInsurerList.get(i).getRelation() + ")"*/);
            } else {
                arrayAdapter.add(familyInsurerList.get(i).getName());
            }
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
                edt_insurername.setText(familyInsurerList.get(which).getName());
                insurerId = familyInsurerList.get(which).getId();

            }
        });

        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    private void firmInsurerListDialog(ArrayList<FirmInsurerNameListPojo> firmInsurerList) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Insurer");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < firmInsurerList.size(); i++) {
            arrayAdapter.add(firmInsurerList.get(i).getFirm_name());
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
                edt_insurername.setText(firmInsurerList.get(which).getFirm_name());
                insurerId = firmInsurerList.get(which).getId();

            }
        });

        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();
    }

    private void policyTypeListDialog(ArrayList<PolicyTypeListPojo> parentPolicyTypeList) {
        ArrayList<PolicyTypeListPojo.Policy_details> policyTypeList = parentPolicyTypeList.get(0).getPolicy_details();

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Policy Type");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < policyTypeList.size(); i++) {

            arrayAdapter.add(policyTypeList.get(i).getType());
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
                edt_policytype.setText(policyTypeList.get(which).getType());
                policyTypeID = policyTypeList.get(which).getId();

            }
        });

        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();


    }

    private void frequencyListDialog(ArrayList<FrequencyListPojo> frequencyList) {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        builderSingle.setTitle("Select Frequency");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.list_row);

        for (int i = 0; i < frequencyList.size(); i++) {

            arrayAdapter.add(frequencyList.get(i).getFrequency());
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
                edt_frequency.setText(frequencyList.get(which).getFrequency());
                frequencyId = frequencyList.get(which).getId();

            }
        });

        AlertDialog alertD = builderSingle.create();
        alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
        alertD.show();


    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEventHandler() {


        rb_individual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edt_insurername.setText("");
                insurerId = "";
            }
        });

        rb_firm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edt_insurername.setText("");
                insurerId = "";
            }
        });


        edt_insurancecompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (companyList.size() == 0) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetCompanyList().execute(user_id, "1");
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    companyListDialog(companyList);
                }
            }
        });

        edt_clientname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clientList.size() == 0) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetClientList().execute(user_id);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    clientListDialog(clientList);
                }
            }
        });

        edt_insurername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rb_individual.isChecked()) {
                    insurerTypeId = "2";
                } else if (rb_firm.isChecked()) {
                    insurerTypeId = "1";
                }


                if (!edt_clientname.getText().toString().trim().equals("")) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetInsurerList().execute(clientId, insurerTypeId);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Select Client");
                }

            }
        });

        edt_policytype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!edt_insurancecompany.getText().toString().trim().equals("")) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetPolicyTypeList().execute(user_id, companyId);
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Select Insurance Company");
                }
            }
        });

        edt_startdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd1 = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        edt_enddate.setText("");

                        edt_startdate.setText(
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dpd1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                dpd1.show();
            }
        });

        edt_enddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!edt_startdate.getText().toString().trim().equals("")) {

                    DatePickerDialog dpd1 = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            edt_enddate.setText(
                                    changeDateFormat("yyyy-MM-dd",
                                            "dd/MM/yyyy",
                                            Utilities.ConvertDateFormat(Utilities.dfDate, dayOfMonth, monthOfYear + 1, year))

                            );

                            mYear1 = year;
                            mMonth1 = monthOfYear;
                            mDay1 = dayOfMonth;
                        }
                    }, mYear1, mMonth1, mDay1);
                    Calendar c = Calendar.getInstance();
                    c.set(mYear, mMonth, mDay);
                    try {
                        dpd1.getDatePicker().setCalendarViewShown(false);
                        dpd1.getDatePicker().setMinDate(c.getTimeInMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dpd1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                    dpd1.show();
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Select Start Date");
                }
            }
        });

        edt_frequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (frequencyList.size() == 0) {
                    if (Utilities.isNetworkAvailable(context)) {
                        new GetFrequenctList().execute();
                    } else {
                        Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                    }
                } else {
                    frequencyListDialog(frequencyList);
                }
            }
        });

        edt_description.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (edt_description.getText().toString().trim().equals("")) {

                    if (!companyAliasName.equals("") ||
                            !edt_insurepolicyno.getText().toString().trim().equals("") ||
                            !edt_insurername.getText().toString().trim().equals("")) {

                        edt_description.setText(companyAliasName
                                + " - " + edt_insurepolicyno.getText().toString().trim()
                                + " - " + edt_insurername.getText().toString().trim()
                        );
                        edt_description.setSelection(edt_description.getText().length());

                    }
                }
                return false;
            }
        });

        btn_adddocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View rowView = inflater.inflate(R.layout.add_layout_document, null);
                LinearLayout ll = (LinearLayout) rowView;
                documentsLayoutsList.add(ll);
                ll_documents.addView(rowView, ll_documents.getChildCount() - 1);
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
                img_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submitData();
                    }
                });
            }
        });

    }

    public void selectDocuments(View view) {
        if (doesAppNeedPermissions()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, 1);
                return;
            } else {
                if (Utilities.isNetworkAvailable(context)) {
                    edt_selectdocuments = (EditText) view;
                    final CharSequence[] options = {"Take a Photo", "Choose from Gallery", "Choose a Document"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                    builder.setCancelable(false);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            if (options[item].equals("Take a Photo")) {
                                photoFile = new File(generalInsurancePicFolder, "doc_image.png");
                                photoURI = Uri.fromFile(photoFile);
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(intent, CAMERA_REQUEST);
                            } else if (options[item].equals("Choose from Gallery")) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY_REQUEST);
                            } else if (options[item].equals("Choose a Document")) {
                                FilePickerBuilder
                                        .getInstance()
                                        .setMaxCount(1)
                                        .setActivityTheme(R.style.LibAppTheme)
                                        .pickFile(EditGeneralInsurance_Activity.this);
                            }
                        }
                    });
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertD = builder.create();
                    alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                    alertD.show();
                } else {
                    Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
                }
            }
        } else {
            if (Utilities.isNetworkAvailable(context)) {
                edt_selectdocuments = (EditText) view;
                final CharSequence[] options = {"Take a Photo", "Choose from Gallery", "Choose a Document"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                builder.setCancelable(false);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take a Photo")) {
                            photoFile = new File(generalInsurancePicFolder, "doc_image.png");
                            photoURI = Uri.fromFile(photoFile);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, CAMERA_REQUEST);
                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, GALLERY_REQUEST);
                        } else if (options[item].equals("Choose a Document")) {
                            FilePickerBuilder
                                    .getInstance()
                                    .setMaxCount(1)
                                    .setActivityTheme(R.style.LibAppTheme)
                                    .pickFile(EditGeneralInsurance_Activity.this);
                        }
                    }
                });
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertD = builder.create();
                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                alertD.show();
            } else {
                Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
            }
        }


    }

    public void removeDocument(View view) {
        ll_documents.removeView((View) view.getParent());
        documentsLayoutsList.remove(view.getParent());
    }

    private void submitData() {

        if (edt_insurancecompany.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Select Insurance Company");
            return;
        }

        if (edt_clientname.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Select Client");
            return;
        }

        if (edt_insurername.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Select Insurer");
            return;
        }

//        if (edt_policytype.getText().toString().trim().equals("")) {
//            Utilities.showSnackBar(ll_parent, "Please Select Policy Type");
//            return;
//        }

        if (edt_startdate.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Select Start Date");
            return;
        }

        if (edt_frequency.getText().toString().trim().equals("")) {
            Utilities.showSnackBar(ll_parent, "Please Select Frequency");
            return;
        }

        if (edt_description.getText().toString().trim().equals("")) {

            if (!companyAliasName.equals("") ||
                    !edt_insurepolicyno.getText().toString().trim().equals("") ||
                    !edt_insurername.getText().toString().trim().equals("")) {

                edt_description.setText(companyAliasName
                        + " - " + edt_insurepolicyno.getText().toString().trim()
                        + " - " + edt_insurername.getText().toString().trim()
                );
                edt_description.setSelection(edt_description.getText().length());

            }
        }

        if (rb_individual.isChecked()) {
            insurerType = "R";
        } else if (rb_firm.isChecked()) {
            insurerType = "F";
        }

        JsonArray maturityDatesJSONArray = new JsonArray();

        ArrayList<LifeGeneralInsuranceMainListPojo.DocumentListPojo> documentsList = new ArrayList<>();
        for (int i = 0; i < documentsLayoutsList.size(); i++) {

            if (!((EditText) documentsLayoutsList.get(i).findViewById(R.id.edt_selectdocuments)).getText().toString().trim().equals("")) {

                LifeGeneralInsuranceMainListPojo.DocumentListPojo documentObj = new LifeGeneralInsuranceMainListPojo.DocumentListPojo();

                documentObj.setDocument(((EditText) documentsLayoutsList.get(i).findViewById(R.id.edt_selectdocuments)).getText().toString().trim());

                documentsList.add(documentObj);
            }
        }

        JsonArray documentJSONArray = new JsonArray();

        for (int i = 0; i < documentsList.size(); i++) {
            JsonObject documentJSONObj = new JsonObject();
            documentJSONObj.addProperty("photos", documentsList.get(i).getDocument());
            documentJSONArray.add(documentJSONObj);
        }

        JsonObject mainObj = new JsonObject();

        mainObj.addProperty("type", "update");
        mainObj.addProperty("insurance_type", "1");    // 1 for General insurance
        mainObj.addProperty("client_name", clientId);
        mainObj.addProperty("description", edt_description.getText().toString().trim());
        mainObj.addProperty("policyNo", edt_insurepolicyno.getText().toString().trim());
        mainObj.addProperty("sum_insured", edt_suminsured.getText().toString().trim());
        mainObj.addProperty("premium_amount", edt_premiumamt.getText().toString().trim());
        mainObj.addProperty("frequency", frequencyId);
        mainObj.addProperty("start_date", changeDateFormat("dd/MM/yyyy",
                "yyyy/MM/dd",
                edt_startdate.getText().toString().trim()));
        mainObj.addProperty("end_date", changeDateFormat("dd/MM/yyyy",
                "yyyy/MM/dd",
                edt_enddate.getText().toString().trim()));
        mainObj.addProperty("link", edt_link.getText().toString().trim());
        mainObj.addProperty("policy_type", policyTypeID);
        mainObj.addProperty("insurance_company", companyId);
        mainObj.add("documents", documentJSONArray);
        mainObj.addProperty("remark", edt_remark.getText().toString().trim());
        mainObj.add("maturity_date_details", maturityDatesJSONArray);
        mainObj.addProperty("policy_status", "0");
        mainObj.addProperty("insurer_name_id", insurerId);
        mainObj.addProperty("insurer_type", insurerType);
        mainObj.addProperty("user_id", user_id);
        mainObj.addProperty("id", id);

        Log.i("GeneralInsuranceJson", mainObj.toString());

        if (Utilities.isInternetAvailable(context)) {
            new EditGeneralInsuranceDetails().execute(mainObj.toString());
        } else {
            Utilities.showSnackBar(ll_parent, "Please Check Internet Connection");
        }


    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        img_save = findViewById(R.id.img_save);
        mToolbar.setTitle("Edit General Insurance");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(EditGeneralInsurance_Activity.this);
            }
            if (requestCode == CAMERA_REQUEST) {
                CropImage.activity(photoURI).setGuidelines(CropImageView.Guidelines.ON).start(EditGeneralInsurance_Activity.this);
            }

            if (requestCode == FilePickerConst.REQUEST_CODE_DOC) {
                Uri fileUri = data.getData();
                ArrayList<String> filePath = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);
                File fileToBeUploaded = new File(filePath.get(0));
                new UploadProductPhoto().execute(fileToBeUploaded);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                savefile(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    void savefile(Uri sourceuri) {
        Log.i("sourceuri1", "" + sourceuri);
        String sourceFilename = sourceuri.getPath();
        String destinationFilename = Environment.getExternalStorageDirectory() + "/Insurance/"
                + "/General Insurance/" + File.separatorChar + "img.png";

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File photoFileToUpload = new File(destinationFilename);
        new UploadProductPhoto().execute(photoFileToUpload);
//        doc_image_uri = Uri.fromFile(imageFile);
    }

    public class GetCompanyList extends AsyncTask<String, Void, String> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context, R.style.CustomDialogTheme);
            pd.setMessage("Please Wait. . .");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String res = "[]";
            List<ParamsPojo> param = new ArrayList<ParamsPojo>();
            param.add(new ParamsPojo("type", "getAllCompany"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("insurance_type_id", params[1]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
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
                    companyList = new ArrayList<>();
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                InsuranceCompanyListPojo companyMainObj = new InsuranceCompanyListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                companyMainObj.setId(jsonObj.getString("id"));
                                companyMainObj.setCompany_name(jsonObj.getString("company_name"));
                                companyMainObj.setCompany_alias(jsonObj.getString("company_alias"));
                                companyList.add(companyMainObj);
                            }
                            companyListDialog(companyList);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("Please add general insurance company");
                        builder.setIcon(R.drawable.ic_alert_red_24dp);
                        builder.setTitle("No Record Found");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(context, AddInsuranceCompany_Activity.class);
                                intent.putExtra("TYPE", "1");
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

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

    public class GetClientList extends AsyncTask<String, Void, String> {

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
            param.add(new ParamsPojo("type", "getAllLIClients"));
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
                    clientList = new ArrayList<ClientMainListPojo>();
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                ClientMainListPojo clientMainObj = new ClientMainListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);
                                clientMainObj.setId(jsonObj.getString("id"));
                                clientMainObj.setFirst_name(jsonObj.getString("first_name"));
                                clientMainObj.setAlias(jsonObj.getString("alias"));
                                clientMainObj.setMobile(jsonObj.getString("mobile"));

                                clientList.add(clientMainObj);
                            }
                        }

                        if (clientList.size() != 0) {
                            clientListDialog(clientList);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setTitle("No Record Found");
                        builder.setMessage("Please add client details");
                        builder.setIcon(R.drawable.ic_alert_red_24dp);
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(context, AddClientDetails_Activity.class));
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

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

    public class GetInsurerList extends AsyncTask<String, Void, String> {

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
            param.add(new ParamsPojo("type", "getAllInsurer"));
            param.add(new ParamsPojo("client_id", params[0]));
            param.add(new ParamsPojo("insurance_type_id", params[1]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
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
                    familyInsurerList = new ArrayList<FamilyInsurerNameListPojo>();
                    firmInsurerList = new ArrayList<FirmInsurerNameListPojo>();
                    if (type.equalsIgnoreCase("success")) {
                        if (insurerTypeId.equals("2")) {

                            JSONArray jsonarr = mainObj.getJSONArray("result");
                            if (jsonarr.length() > 0) {
                                for (int i = 0; i < jsonarr.length(); i++) {

                                    FamilyInsurerNameListPojo insurerMainObj = new FamilyInsurerNameListPojo();
                                    JSONObject jsonObj = jsonarr.getJSONObject(i);
                                    insurerMainObj.setId(jsonObj.getString("id"));
                                    insurerMainObj.setName(jsonObj.getString("name"));
                                    insurerMainObj.setRelation(jsonObj.getString("relation"));

                                    familyInsurerList.add(insurerMainObj);
                                }
                            }

                            if (familyInsurerList.size() != 0) {
                                familyInsurerListDialog(familyInsurerList);
                            }
                        } else if (insurerTypeId.equals("1")) {

                            JSONArray jsonarr = mainObj.getJSONArray("result");
                            if (jsonarr.length() > 0) {
                                for (int i = 0; i < jsonarr.length(); i++) {

                                    FirmInsurerNameListPojo insurerMainObj = new FirmInsurerNameListPojo();
                                    JSONObject jsonObj = jsonarr.getJSONObject(i);
                                    insurerMainObj.setId(jsonObj.getString("id"));
                                    insurerMainObj.setFirm_name(jsonObj.getString("firm_name"));

                                    firmInsurerList.add(insurerMainObj);
                                }
                            }

                            if (firmInsurerList.size() != 0) {
                                firmInsurerListDialog(firmInsurerList);
                            }

                        }


                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setTitle("No Record Found");

                        if (insurerTypeId.equals("2")) {
                            builder.setMessage("Please add family details for this client");
                        } else if (insurerTypeId.equals("1")) {
                            builder.setMessage("Please add firm details for this client");
                        }
                        builder.setIcon(R.drawable.ic_alert_red_24dp);
                        builder.setCancelable(false);
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

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

    public class GetPolicyTypeList extends AsyncTask<String, Void, String> {
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
            param.add(new ParamsPojo("type", "getAllLICTYpe"));
            param.add(new ParamsPojo("user_id", params[0]));
            param.add(new ParamsPojo("company_id", params[1]));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
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
                    policyTypeList = new ArrayList<>();
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                PolicyTypeListPojo policyTypeMainObj = new PolicyTypeListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);

                                if (jsonObj.getString("insurance_type").equals("1")) {
                                    policyTypeMainObj.setId(jsonObj.getString("id"));
                                    policyTypeMainObj.setCompany_name(jsonObj.getString("company_name"));


                                    ArrayList<PolicyTypeListPojo.Policy_details> policyTypesList = new ArrayList<>();

                                    for (int j = 0; j < jsonObj.getJSONArray("policy_details").length(); j++) {

                                        if (!jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("type").equals("")) {
                                            PolicyTypeListPojo.Policy_details policyTypeObj = new PolicyTypeListPojo.Policy_details();
                                            policyTypeObj.setId(jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("id"));
                                            policyTypeObj.setType(jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("type"));
                                            policyTypeObj.setAlias(jsonObj.getJSONArray("policy_details").getJSONObject(j).getString("alias"));
                                            policyTypesList.add(policyTypeObj);
                                        }
                                    }
                                    policyTypeMainObj.setPolicy_details(policyTypesList);
                                    policyTypeList.add(policyTypeMainObj);
                                }

                            }
                            if (policyTypeList.get(0).getPolicy_details().size() != 0) {
                                policyTypeListDialog(policyTypeList);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                                builder.setTitle("No Record Found");
                                builder.setMessage("Please add policy type for this insurance company");
                                builder.setIcon(R.drawable.ic_alert_red_24dp);
                                builder.setCancelable(false);
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(context, AddPolicyType_Activity.class);
                                        intent.putExtra("TYPE", "1");
                                        startActivity(intent);
                                    }
                                });
                                AlertDialog alertD = builder.create();
                                alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                                alertD.show();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetFrequenctList extends AsyncTask<String, Void, String> {
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
            param.add(new ParamsPojo("type", "getAllFreq"));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.INSURANCEAPI, param);
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
                    frequencyList = new ArrayList<>();
                    if (type.equalsIgnoreCase("success")) {
                        JSONArray jsonarr = mainObj.getJSONArray("result");
                        if (jsonarr.length() > 0) {
                            for (int i = 0; i < jsonarr.length(); i++) {

                                FrequencyListPojo frequencyMainObj = new FrequencyListPojo();
                                JSONObject jsonObj = jsonarr.getJSONObject(i);

                                frequencyMainObj.setId(jsonObj.getString("id"));
                                frequencyMainObj.setFrequency(jsonObj.getString("frequency"));
                                frequencyList.add(frequencyMainObj);

                            }
                            if (frequencyList.size() != 0) {
                                frequencyListDialog(frequencyList);
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

    private class UploadProductPhoto extends AsyncTask<File, Integer, String> {
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
        protected String doInBackground(File... params) {
            String res = "";
            try {
                MultipartUtility multipart = new MultipartUtility(ApplicationConstants.UPLOADFILEAPI, "UTF-8");

                multipart.addFormField("request_type", "uploadFile");
                multipart.addFormField("user_id", user_id);
                multipart.addFilePart("document", params[0]);

                List<String> response = multipart.finish();
                for (String line : response) {
                    res = res + line;
                }
                return res;
            } catch (IOException ex) {
                return ex.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                pd.dismiss();

                if (result != null && result.length() > 0 && !result.equalsIgnoreCase("[]")) {
                    JSONObject mainObj = new JSONObject(result);
                    String type = mainObj.getString("type");
                    String message = mainObj.getString("message");
                    if (type.equalsIgnoreCase("Success")) {
                        JSONObject Obj1 = mainObj.getJSONObject("result");
                        String document_name = Obj1.getString("name");
                        edt_selectdocuments.setText(document_name);

                    } else {
                        Utilities.showSnackBar(ll_parent, message);
                    }
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class EditGeneralInsuranceDetails extends AsyncTask<String, Void, String> {
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
            res = WebServiceCalls.JSONAPICall(ApplicationConstants.INSURANCEAPI, params[0]);
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

                        new GeneralInsurance_Fragment.GetGeneralInsurance().execute(user_id);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                        builder.setMessage("General Insurance Details Saved Successfully");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
                    builder.setTitle("Alert");
                    builder.setIcon(R.drawable.ic_alert_red_24dp);
                    builder.setMessage("Please provide permission for Camera and Gallery");
                    builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.getPackageName(), null)));
                        }
                    });
                    builder.create();
                    AlertDialog alertD = builder.create();
                    alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
                    alertD.show();
                }
            }

        }
    }


}
