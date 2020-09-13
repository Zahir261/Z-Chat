package com.a2z.zchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.a2z.zchat.R;
import com.a2z.zchat.constants.AppConstants;
import com.a2z.zchat.constants.ServerConstants;
import com.a2z.zchat.helpers.CustomProgressDialog;
import com.a2z.zchat.helpers.NukeSSLCerts;
import com.a2z.zchat.helpers.Validator;
import com.a2z.zchat.managers.AppManager;
import com.a2z.zchat.managers.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnSignUp, btnForgotPassword;
    private TextView tvEmailErrorMessage, tvPasswordErrorMessage;
    private LinearLayout llEmail, llPassword;
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NukeSSLCerts.nuke();

        etEmail = findViewById(R.id.login_email_et);
        etPassword = findViewById(R.id.login_password_et);
        btnLogin = findViewById(R.id.login_login_btn);
        btnSignUp = findViewById(R.id.login_sign_up_btn);
        btnForgotPassword = findViewById(R.id.login_forgot_password_btn);

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
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();
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
                                finish();
                            }
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
            tvEmailErrorMessage.setText("Please fill in the email.");
            llEmail.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(Validator.isNullOrEmpty(password)){
            tvPasswordErrorMessage.setText("Please fill in the password.");
            llPassword.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(email)){
            if(!emailIsValid(email)){
                tvEmailErrorMessage.setText("Please enter a valid email address.");
                llEmail.setVisibility(View.VISIBLE);
                flag = true;
            }
        }

        if(!Validator.isNullOrEmpty(password)){
            if(password.length() < 8){
                tvPasswordErrorMessage.setText("Password must be at least 8 characters.");
                llPassword.setVisibility(View.VISIBLE);
                flag = true;
            }
        }

        return flag;
    }

    private void validatePassword() {
        String enteredPassword = etPassword.getText().toString();
        if(enteredPassword.length() == 0 || enteredPassword.length()>=8){
            llPassword.setVisibility(View.GONE);
        }else{
            tvPasswordErrorMessage.setText("Password must be at least 8 characters.");
            llPassword.setVisibility(View.VISIBLE);
        }
    }

    private void validateEmail(String enteredEmail) {

        if(emailIsValid(enteredEmail) || etEmail.length() == 0){
            llEmail.setVisibility(View.GONE);
        }else{
            tvEmailErrorMessage.setText("Please enter a valid email address.");
            llEmail.setVisibility(View.VISIBLE);
        }
    }

    private boolean emailIsValid(String enteredEmail){
        String emailRegExp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern pattern = Pattern.compile(emailRegExp);
        return pattern.matcher(enteredEmail).matches();
    }

    private class ValidationTextWatcher implements TextWatcher {

        private View view;

        private ValidationTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch(view.getId()){
                case R.id.login_email_et:
                    validateEmail(etEmail.getText().toString());
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