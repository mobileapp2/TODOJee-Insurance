package com.insurance.todojee.ccavenue;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.insurance.todojee.R;
import com.insurance.todojee.utilities.ApplicationConstants;
import com.insurance.todojee.utilities.UserSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlanBuySuccess_Activity extends Activity {

    private Context context;
    private Button btn_done;
    private TextView tv_orderid, tv_validity, tv_space, tv_whatsapp, tv_textsms, tv_clients, tv_policies;
    private String JSONString, user_id;
    private LottieAnimationView animationView;
    private UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planbuy_success);

        init();
        getSessionData();
        setDefaults();
        setEventHandler();
    }

    private void init() {
        context = PlanBuySuccess_Activity.this;
        session = new UserSessionManager(context);
        animationView = findViewById(R.id.animation_view);
        animationView.playAnimation();

        tv_orderid = findViewById(R.id.tv_orderid);
        tv_validity = findViewById(R.id.tv_validity);
        tv_space = findViewById(R.id.tv_space);
        tv_whatsapp = findViewById(R.id.tv_whatsapp);
        tv_textsms = findViewById(R.id.tv_textsms);
        tv_clients = findViewById(R.id.tv_clients);
        tv_policies = findViewById(R.id.tv_policies);

        btn_done = findViewById(R.id.btn_done);

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
        JSONString = getIntent().getStringExtra("JSONString");

        try {
            JSONObject jsonObject = new JSONObject(JSONString);

            tv_orderid.setText(jsonObject.getString("order_id"));
            tv_validity.setText(getIntent().getStringExtra("validity"));
            tv_space.setText(jsonObject.getString("space"));
            tv_whatsapp.setText(jsonObject.getString("whatsApp_msg"));
            tv_textsms.setText(jsonObject.getString("sms"));
            tv_clients.setText(getIntent().getStringExtra("clients"));
            tv_policies.setText(getIntent().getStringExtra("policies"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setEventHandler() {
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
