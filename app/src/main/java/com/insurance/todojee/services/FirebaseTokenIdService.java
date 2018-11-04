package com.insurance.todojee.services;

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.insurance.todojee.utilities.UserSessionManager;

public class FirebaseTokenIdService extends FirebaseInstanceIdService {

    private Context context;
    private UserSessionManager session;

    @Override
    public void onTokenRefresh() {
        context = FirebaseTokenIdService.this;
        session = new UserSessionManager(context);

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("TokenID", "" + token);

        if (token != null) {
            session.createAndroidToken(token);
        }
    }
}
