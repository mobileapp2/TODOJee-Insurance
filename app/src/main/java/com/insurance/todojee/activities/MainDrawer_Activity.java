package com.insurance.todojee.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.insurance.todojee.R;
import com.insurance.todojee.adapters.BotNavViewPagerAdapter;
import com.insurance.todojee.ccavenue.PlanBuySuccess_Activity;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.ParamsPojo;
import com.insurance.todojee.utilities.UserSessionManager;
import com.insurance.todojee.utilities.Utilities;
import com.insurance.todojee.utilities.WebServiceCalls;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainDrawer_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context context;
    private TextView tv_name;
    private ImageView imv_profile;
    private String name, photo, mode;
    private AHBottomNavigation bottomNavigation;
    private AHBottomNavigationItem botNavClent, botNavPolicy, botNavCalendar, botNavReminder, botFilter;
    private Fragment currentFragment;
    private BotNavViewPagerAdapter adapter;
    private AHBottomNavigationViewPager view_pager;
    private UserSessionManager session;
    private ImageView img_todolist, img_notifications;
    public static ArrayList<String> faqLinksList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        tv_name = header.findViewById(R.id.tv_name);
        imv_profile = header.findViewById(R.id.imv_profile);
        navigationView.setNavigationItemSelectedListener(this);

        init();
        setUpToolbar();
        getSessionData();
        setEventHandler();
        setUpBottomNavigation();

        if (Utilities.isInternetAvailable(context)) {

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
            builder.setMessage("Please Check Internet Connection");
            builder.setIcon(R.drawable.ic_alert_red_24dp);
            builder.setTitle("Alert");
            builder.setCancelable(false);
            builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(context, MainDrawer_Activity.class));
                    finish();
                }
            });
            AlertDialog alertD = builder.create();
            alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
            alertD.show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSessionData();
    }

    private void init() {

        context = MainDrawer_Activity.this;
        new GetFaq().execute();
        session = new UserSessionManager(context);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        view_pager = findViewById(R.id.view_pager);
        view_pager.setOffscreenPageLimit(4);
        adapter = new BotNavViewPagerAdapter(getSupportFragmentManager());
        view_pager.setAdapter(adapter);
    }

    private void getSessionData() {
        try {
            JSONArray user_info = new JSONArray(session.getUserDetails().get(
                    ApplicationConstants.KEY_LOGIN_INFO));
            JSONObject json = user_info.getJSONObject(0);
            name = json.getString("name");
            photo = json.getString("photo");
            mode = json.getString("communication_mode");

            if (mode.equals("first_time")) {
                startActivity(new Intent(context, FirstTime_Activity.class));
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        getUpDrawerHeader();
    }

    private void setEventHandler() {
        img_notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, NotificationList_Activity.class));
            }
        });
        img_todolist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, TodoList_Activity.class));
            }
        });
    }

    private void getUpDrawerHeader() {
        tv_name.setText(name);

        if (!photo.equals("")) {
            Picasso.with(context)
                    .load(photo)
                    .placeholder(R.drawable.icon_userprofile)
                    .into(imv_profile);
        }

        Picasso.with(context).setLoggingEnabled(true);
    }

    private void setUpBottomNavigation() {
        // Create items
        botNavClent = new AHBottomNavigationItem("Client", R.drawable.icon_client, R.color.Gunmetal);
        botNavPolicy = new AHBottomNavigationItem("Policy", R.drawable.icon_policytype, R.color.Gunmetal);
        botNavCalendar = new AHBottomNavigationItem("Calendar", R.drawable.icon_calendarview, R.color.Gunmetal);
        botNavReminder = new AHBottomNavigationItem("Reminders", R.drawable.icon_reminder, R.color.Gunmetal);
        botFilter = new AHBottomNavigationItem("Filter", R.drawable.icon_filter, R.color.Gunmetal);

        // Add items
        bottomNavigation.addItem(botNavClent);
        bottomNavigation.addItem(botNavPolicy);
        bottomNavigation.addItem(botNavCalendar);
        bottomNavigation.addItem(botNavReminder);
        bottomNavigation.addItem(botFilter);


        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#ffffff"));

        bottomNavigation.setAccentColor(Color.parseColor("#000000"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

        bottomNavigation.setForceTint(true);

        bottomNavigation.setTranslucentNavigationEnabled(true);

        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

//        bottomNavigation.setColored(true);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (currentFragment == null) {
                    currentFragment = adapter.getCurrentFragment();
                }

                view_pager.setCurrentItem(position, true);

                if (currentFragment == null) {
                    return true;
                }

                currentFragment = adapter.getCurrentFragment();
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            startActivity(new Intent(context, Profile_Activity.class));
        } else if (id == R.id.menu_pro) {
                startActivity(new Intent(context, SelectPremiumPlan_Activity.class));
        } else if (id == R.id.menu_masters) {
            startActivity(new Intent(context, Masters_Activity.class));
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(context, Settings_Activity.class));
        } else if (id == R.id.menu_FAQ) {
            startActivity(new Intent(context, FAQ_Activity.class));
        } else if (id == R.id.menu_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
            builder.setMessage("Are you sure you want to log out?");
            builder.setTitle("Alert");
            builder.setIcon(R.drawable.ic_alert_red_24dp);
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    session.logoutUser();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertD = builder.create();
            alertD.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationTheme;
            alertD.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerlayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        img_notifications = findViewById(R.id.img_notifications);
        img_todolist = findViewById(R.id.img_todolist);
        DrawerLayout drawer = findViewById(R.id.drawerlayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public class GetFaq extends AsyncTask<String, Void, String> {

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
            param.add(new ParamsPojo("type", "getFAQ"));
            res = WebServiceCalls.FORMDATAAPICall(ApplicationConstants.FAQAPI, param);
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
                                if (!jsonObj.getString("faqImageUrl").equals("")) {
                                    String link = "https://todojeeinsurance.in/faqImages/" + jsonObj.getString("faqImageUrl");
                                    faqLinksList.add(link);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
