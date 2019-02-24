package com.insurance.todojee.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.insurance.todojee.R;
import com.insurance.todojee.adapters.ContactListRVAapter;
import com.insurance.todojee.models.ContactListPojo;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Contacts_Activity extends Activity {

    private Context context;
    private LinearLayout ll_parent;
    private UserSessionManager session;
    private SearchView searchView;
    String user_id;
    private String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS};

    private List<ContactListPojo> contactList;
    RecyclerView rv_contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        context = Contacts_Activity.this;
        init();
        setEventHandler();
        getSessionData();
        setDefault();
        setupToolbar();
    }

    private void init() {
        session = new UserSessionManager(context);
        ll_parent = findViewById(R.id.ll_parent);
        rv_contacts = findViewById(R.id.rv_contacts);
        contactList = new ArrayList<ContactListPojo>();
        rv_contacts.setLayoutManager(new LinearLayoutManager(context));
        searchView = findViewById(R.id.searchView);
        searchView.setFocusable(false);

    }

    private void setDefault() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            askPermission();
        } else {
            new contactList().execute();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void askPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(PERMISSIONS, 1);
            return;
        } else {
            new contactList().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new contactList().execute();
                } else {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
                    builder.setTitle("Alert");
                    builder.setMessage("Please provide permission to allow SendBuzz to access your contacts");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.getPackageName(), null)));
//                            askPermission();
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

    private void setEventHandler() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                ArrayList<ContactListPojo> contactsSearchedList = new ArrayList<>();
                for (ContactListPojo contacts : contactList) {
                    String contactToBeSearched = contacts.getName().toLowerCase() + contacts.getPhoneNo().toLowerCase();
                    if (contactToBeSearched.contains(query.toLowerCase())) {
                        contactsSearchedList.add(contacts);
                    }
                }

                if (contactsSearchedList.size() == 0) {
                    Utilities.showAlertDialog(context, "Fail", "No Such Contact Found", false);
                    //  searchView.setQuery("", false);
                    // bindRecyclerview(contactList);
                } else {
                    bindRecyclerview(contactsSearchedList);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    ArrayList<ContactListPojo> contactsSearchedList = new ArrayList<>();
                    for (ContactListPojo contacts : contactList) {
                        String contactToBeSearched = contacts.getName().toLowerCase() + contacts.getPhoneNo().toLowerCase();
                        if (contactToBeSearched.contains(newText.toLowerCase())) {
                            contactsSearchedList.add(contacts);
                        }
                    }
                    if (contactsSearchedList.size() == 0) {
                        Utilities.showMessageString(context, "No Such Contact Found");
                        //   searchView.setQuery("", false);
                        //   bindRecyclerview(contactList);
                    } else {
                        bindRecyclerview(contactsSearchedList);
                    }
                    return true;
                } else if (newText.equals("")) {
                    bindRecyclerview(contactList);
                }
                return true;
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

    private void setupToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Select Contact");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class contactList extends AsyncTask<Void, Void, List> {
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
        protected List doInBackground(Void... voids) {
            List contactList = getContactList2();
            return contactList;
        }

        @Override
        protected void onPostExecute(List list) {
            pd.dismiss();
            super.onPostExecute(list);
            bindRecyclerview(list);
        }
    }

    private void bindRecyclerview(List<ContactListPojo> contactList) {

        rv_contacts.setAdapter(new ContactListRVAapter(context, contactList));

    }

    private List getContactList2() {

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String contact_id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String emailIdOfContact = "";
/*
            context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + " = ?", new String[] { contact_id }, null);
*/
            Cursor emails = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contact_id, null, null);
            while (emails.moveToNext()) {
                emailIdOfContact = emails.getString(emails
                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                // Log.i(TAG,"...COntact Name ...."
                // + contactName + "...contact Number..."
                // + emailIdOfContact);
                //  emailType = emails.getInt(emails
                //        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));


            }
            contactList.add(new ContactListPojo(String.valueOf(name.charAt(0)), name, phoneNumber.replaceAll("\\s+", ""), emailIdOfContact));

        }
        phones.close();

        Set<ContactListPojo> s = new HashSet<ContactListPojo>();
        s.addAll(contactList);
        contactList = new ArrayList<ContactListPojo>();
        contactList.addAll(s);

        Collections.sort(contactList, new Comparator<ContactListPojo>() {
            @Override
            public int compare(ContactListPojo o1, ContactListPojo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return contactList;
    }
}
