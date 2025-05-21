package com.group06.music_app_mobile.app_utils;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.group06.music_app_mobile.R;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.group06.music_app_mobile.app_utils.Constants;


public class GoogleSignInHelper {
    private final Activity activity;
    private final GoogleSignInClient signInClient;
    private final int RC_SIGN_IN = 1000;

    public interface Callback {
        void onSuccess(GoogleSignInAccount account);
        void onError(Exception e);
    }

    private Callback callback;

    public GoogleSignInHelper(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.GOOGLE_CLIENT_ID) // nếu bạn cần
                .build();

        signInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void startSignIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void handleResult(int requestCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (callback != null) callback.onSuccess(account);
            } catch (ApiException e) {
                if (callback != null) callback.onError(e);
            }
        }
    }
}
