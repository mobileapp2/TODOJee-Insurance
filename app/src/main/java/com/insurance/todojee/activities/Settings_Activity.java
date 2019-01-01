package com.insurance.todojee.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.insurance.todojee.R;

public class Settings_Activity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
        setUpToolbar();
    }

    private void init() {
        context = Settings_Activity.this;
    }

    public void openBirthdayWhatsappSettings(View view) {
        startActivity(new Intent(context, WhatsappBirthdaySettings_Activity.class));
    }

    public void openBirthdaySMSSettings(View view) {
        startActivity(new Intent(context, SMSBirthdaySettings_Activity.class));
    }

    public void openAnniversaryWhatsappSettings(View view) {
        startActivity(new Intent(context, WhatsappAnniversarySettings_Activity.class));
    }

    public void openAnniversarySMSSettings(View view) {
        startActivity(new Intent(context, SMSAnniversarySettings_Activity.class));
    }

    public void openPremiumDueMessageSettings(View view) {
        startActivity(new Intent(context, PremiumDueMessageSetting_Activity.class));
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Settings");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
