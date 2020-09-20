package com.a2z.zchat.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.a2z.zchat.R;
import com.a2z.zchat.constants.AppConstants;
import com.a2z.zchat.constants.ServerConstants;
import com.a2z.zchat.helpers.CustomProgressDialog;
import com.a2z.zchat.helpers.GoogleSignInHelper;
import com.a2z.zchat.helpers.NukeSSLCerts;
import com.a2z.zchat.helpers.Validator;
import com.a2z.zchat.managers.AppManager;
import com.a2z.zchat.managers.SharedPreferenceManager;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class PasswordForSocialLoginActivity extends AppCompatActivity {

    private GoogleSignInHelper googleSignInHelper;
    private TextInputEditText etPassword, etConfirmPassword;
    private TextView tvPasswordErrorMessage, tvConfirmPasswordErrorMessage;
    private LinearLayout llPasswordError, llConfirmPasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_for_social_login);

        NukeSSLCerts.nuke();
        googleSignInHelper = new GoogleSignInHelper(this);
        googleSignInHelper.connect();

        etPassword = findViewById(R.id.psl_password_et);
        etConfirmPassword = findViewById(R.id.psl_confirm_password_et);
        tvPasswordErrorMessage = findViewById(R.id.psl_password_error_messages_tv);
        tvConfirmPasswordErrorMessage = findViewById(R.id.psl_confirm_password_error_messages_tv);
        llPasswordError = findViewById(R.id.psl_password_error_ll);
        llConfirmPasswordError = findViewById(R.id.psl_confirm_password_error_ll);
        Button btnSetPassword = findViewById(R.id.psl_set_password_btn);

        etPassword.addTextChangedListener(new ValidationTextWatcher(etPassword));
        etConfirmPassword.addTextChangedListener(new ValidationTextWatcher(etConfirmPassword));

        btnSetPassword.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(hasInputError()){
                    AppManager.getAppManager().getInAppNotifier().showToast("Input error exists.");
                }else{
                    final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                    if(account != null){
                        HashMap<String, String> map = new HashMap<>();
                        map.put(AppConstants.User.FIRST_NAME, account.getGivenName());
                        map.put(AppConstants.User.LAST_NAME, account.getFamilyName());
                        map.put(AppConstants.User.EMAIL, account.getEmail());
                        map.put(AppConstants.User.PASSPHRASE, Objects.requireNonNull(etPassword.getText()).toString());
                        map.put(AppConstants.User.VERIFIED, String.valueOf(1));
                        final CustomProgressDialog progressDialog = new CustomProgressDialog(PasswordForSocialLoginActivity.this);
                        progressDialog.showProgressDialog("Signing In...");
                        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.USER_REG_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                progressDialog.dismissDialog();
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if(!object.getBoolean("error")){
                                        if(object.getInt(AppConstants.User.ID) > 0) {
                                            SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
                                            sharedPreferenceManager.setLoginStatus(true);
                                            sharedPreferenceManager.setUserId(String.valueOf(object.getInt(AppConstants.User.ID)));
                                            sharedPreferenceManager.setUserEmail(account.getEmail());
                                            startActivity(new Intent(PasswordForSocialLoginActivity.this, ChatActivity.class));
                                            finish();
                                        }else{
                                            AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Please try again.");
                                        }
                                    }else{
                                        AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Please try again.");
                                    }
                                } catch (JSONException e) {
                                    progressDialog.dismissDialog();
                                    AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Please try again.");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismissDialog();
                                AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Please try again.");
                            }
                        }, map);
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        googleSignInHelper.signOut();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean hasInputError() {
        boolean flag = false;

        if(Validator.isNullOrEmpty(Objects.requireNonNull(etPassword.getText()).toString())){
            tvPasswordErrorMessage.setText(R.string.requirePassText);
            llPasswordError.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(Validator.isNullOrEmpty(Objects.requireNonNull(etConfirmPassword.getText()).toString())){
            tvConfirmPasswordErrorMessage.setText(R.string.requireConfirmPassText);
            llConfirmPasswordError.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(etPassword.getText().toString())){
            if(etPassword.length() <8){
                tvPasswordErrorMessage.setText(R.string.validPassLengthText);
                llPasswordError.setVisibility(View.VISIBLE);
            }
        }

        if(!Validator.isNullOrEmpty(etPassword.getText().toString()) && !Validator.isNullOrEmpty(etConfirmPassword.getText().toString())){
            if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())){
                llConfirmPasswordError.setVisibility(View.VISIBLE);
                tvConfirmPasswordErrorMessage.setText(R.string.passMatchText);
                flag = true;
            }
        }
        return flag;
    }

    private class ValidationTextWatcher implements TextWatcher {

        private View view;

        private ValidationTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch(view.getId()){
                case R.id.psl_password_et:
                    validatePasswordLength(Objects.requireNonNull(etPassword.getText()).toString());
                    break;
                case R.id.psl_confirm_password_et:
                    validateConfirmPassword(Objects.requireNonNull(etPassword.getText()).toString(), Objects.requireNonNull(etConfirmPassword.getText()).toString());
                    break;
                default:
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {

        }
    }

    private void validateConfirmPassword(String password, String confirmPassword) {
        if(password.equals(confirmPassword) || confirmPassword.length()==0){
            llConfirmPasswordError.setVisibility(View.GONE);
        }else{
            llConfirmPasswordError.setVisibility(View.VISIBLE);
            tvConfirmPasswordErrorMessage.setText(R.string.passMatchText);
        }
    }

    private void validatePasswordLength(String password) {
        if(password.length() == 0 || password.length()>=8){
            llPasswordError.setVisibility(View.GONE);
        }else{
            tvPasswordErrorMessage.setText(R.string.validPassLengthText);
            llPasswordError.setVisibility(View.VISIBLE);
        }
    }
}