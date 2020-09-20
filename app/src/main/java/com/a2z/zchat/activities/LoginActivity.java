package com.a2z.zchat.activities;

import androidx.annotation.Nullable;
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

import com.a2z.zchat.helpers.GoogleSignInHelper;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;
import com.a2z.zchat.R;
import com.a2z.zchat.constants.AppConstants;
import com.a2z.zchat.constants.ServerConstants;
import com.a2z.zchat.helpers.CustomProgressDialog;
import com.a2z.zchat.helpers.NukeSSLCerts;
import com.a2z.zchat.helpers.Validator;
import com.a2z.zchat.managers.AppManager;
import com.a2z.zchat.managers.SharedPreferenceManager;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements GoogleSignInHelper.OnGoogleSignInListener {

    private TextInputEditText etEmail, etPassword;
    private TextView tvEmailErrorMessage, tvPasswordErrorMessage;
    private LinearLayout llEmail, llPassword;
    private String email, password;
    private GoogleSignInHelper googleSignInHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NukeSSLCerts.nuke();

        googleSignInHelper = new GoogleSignInHelper(this, this);
        googleSignInHelper.connect();

        etEmail = findViewById(R.id.login_email_et);
        etPassword = findViewById(R.id.login_password_et);
        Button btnLogin = findViewById(R.id.login_login_btn);
        Button btnSignUp = findViewById(R.id.login_sign_up_btn);
        Button btnForgotPassword = findViewById(R.id.login_forgot_password_btn);

        llEmail = findViewById(R.id.login_email_error_ll);
        llPassword = findViewById(R.id.login_password_error_ll);

        tvEmailErrorMessage = findViewById(R.id.login_email_error_messages_tv);
        tvPasswordErrorMessage = findViewById(R.id.login_password_error_messages_tv);

        etEmail.addTextChangedListener(new ValidationTextWatcher(etEmail));
        etPassword.addTextChangedListener(new ValidationTextWatcher(etPassword));

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                email = Objects.requireNonNull(etEmail.getText()).toString();
                password = Objects.requireNonNull(etPassword.getText()).toString();
                if(!hasInputError()){
                    proceedToLogin();
                }else{
                    AppManager.getAppManager().getInAppNotifier().showToast("Input error exists.");
                }
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                intent.putExtra("forgot_password", true);
                intent.putExtra("change_password", false);
                intent.putExtra("mail_sent", false);
                startActivity(intent);
            }
        });

        GoogleSignInButton googleSignInButton = findViewById(R.id.login_google_sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInHelper.signIn();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleSignInHelper.onStart();
    }

    private void proceedToLogin() {
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.User.EMAIL, email);
        map.put(AppConstants.User.PASSPHRASE, password);
        final CustomProgressDialog progressDialog = new CustomProgressDialog(LoginActivity.this);
        progressDialog.showProgressDialog("Logging In");
        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.LOG_IN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if(!object.getBoolean("error")){
                        progressDialog.dismissDialog();
                        if(object.getBoolean("logged_in")) {
                            if (object.getInt("verified") == 1) {
                                SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
                                sharedPreferenceManager.setLoginStatus(true);
                                sharedPreferenceManager.setUserId(String.valueOf(object.getInt(AppConstants.User.ID)));
                                sharedPreferenceManager.setUserEmail(email);
                                startActivity(new Intent(LoginActivity.this, ChatActivity.class));
                            }else{
                                Intent intent = new Intent(LoginActivity.this, EmailConfirmationActivity.class);
                                intent.putExtra("email", object.getString(AppConstants.User.EMAIL));
                                intent.putExtra("resend_flag", true);
                                intent.putExtra("user_verification_mode", true);
                                startActivity(intent);
                            }
                            finish();
                        }else{
                           AppManager.getAppManager().getInAppNotifier().showToast("Email or password is not correct.");
                        }
                    }else{
                        progressDialog.dismissDialog();
                      AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Please try again.");
                    }
                } catch (JSONException e) {
                    progressDialog.dismissDialog();
                    AppManager.getAppManager().getInAppNotifier().log("Exc", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismissDialog();
                AppManager.getAppManager().getInAppNotifier().log("volleyError", error.toString());
            }
        }, map);
    }

    private boolean hasInputError() {
        boolean flag = false;

        if(Validator.isNullOrEmpty(email)){
            tvEmailErrorMessage.setText(R.string.requireEmailText);
            llEmail.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(Validator.isNullOrEmpty(password)){
            tvPasswordErrorMessage.setText(R.string.requirePassText);
            llPassword.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(email)){
            if(!Validator.isEmailValid(email)){
                tvEmailErrorMessage.setText(R.string.requireValidEmailText);
                llEmail.setVisibility(View.VISIBLE);
                flag = true;
            }
        }

        if(!Validator.isNullOrEmpty(password)){
            if(password.length() < 8){
                tvPasswordErrorMessage.setText(R.string.validPassLengthText);
                llPassword.setVisibility(View.VISIBLE);
                flag = true;
            }
        }

        return flag;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void validatePassword() {
        String enteredPassword = Objects.requireNonNull(etPassword.getText()).toString();
        if(enteredPassword.length() == 0 || enteredPassword.length()>=8){
            llPassword.setVisibility(View.GONE);
        }else{
            tvPasswordErrorMessage.setText(R.string.validPassLengthText);
            llPassword.setVisibility(View.VISIBLE);
        }
    }

    private void validateEmail(String enteredEmail) {

        if(Validator.isEmailValid(enteredEmail) || etEmail.length() == 0){
            llEmail.setVisibility(View.GONE);
        }else{
            tvEmailErrorMessage.setText(R.string.requireValidEmailText);
            llEmail.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void OnGSignInSuccess(final GoogleSignInAccount googleSignInAccount) {
        final SharedPreferenceManager spManager = new SharedPreferenceManager(getApplicationContext());
        HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.User.EMAIL, googleSignInAccount.getEmail());
        final CustomProgressDialog progressDialog = new CustomProgressDialog(this);
        progressDialog.showProgressDialog("Signing In...");
        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.GET_USER_ID_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismissDialog();
                try {
                    JSONObject object = new JSONObject(response);
                    if(!object.getBoolean("error")){
                        if(object.getBoolean("email_exists")){
                            spManager.setLoginStatus(true);
                            spManager.setUserEmail(googleSignInAccount.getEmail());
                            spManager.setUserId(String.valueOf(object.getInt(AppConstants.User.ID)));
                            startActivity(new Intent(LoginActivity.this, ChatActivity.class));
                            finish();
                        }else{
                            startActivity(new Intent(LoginActivity.this, PasswordForSocialLoginActivity.class));
                        }
                    }
                } catch (JSONException e) {
                    progressDialog.dismissDialog();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismissDialog();
            }
        }, map);
    }

    @Override
    public void OnGSignInError(String error) {
        AppManager.getAppManager().getInAppNotifier().showToast("Error in signing in. Try again.");
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
                case R.id.login_email_et:
                    validateEmail(Objects.requireNonNull(etEmail.getText()).toString());
                    break;
                case R.id.login_password_et:
                    validatePassword();
                    break;
                default:
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {

        }
    }
}