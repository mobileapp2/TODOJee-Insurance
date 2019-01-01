package com.insurance.todojee.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.insurance.todojee.R;

public class Masters_Activity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masters);

        init();
        setUpToolbar();
    }

    private void init() {
        context = Masters_Activity.this;
    }

    public void openInsuranceCompany(View v) {
        startActivity(new Intent(context, MastersInsuranceCompany_Activity.class));
    }

    public void openPolicyType(View v) {
        startActivity(new Intent(context, MastersPolicyType_Activity.class));
    }

    public void openNewPoducts(View v) {
        startActivity(new Intent(context, MastersProductList_Activity.class));
    }

    public void openFamilyCode(View v) {
        startActivity(new Intent(context, MastersFamiliyCode_Activity.class));
    }

    private void setUpToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Masters");
        mToolbar.setNavigationIcon(R.drawable.icon_backarrow_16p);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
