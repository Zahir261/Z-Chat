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

import com.a2z.zchat.R;
import com.a2z.zchat.constants.AppConstants;
import com.a2z.zchat.constants.ServerConstants;
import com.a2z.zchat.helpers.CustomProgressDialog;
import com.a2z.zchat.helpers.NukeSSLCerts;
import com.a2z.zchat.helpers.Validator;
import com.a2z.zchat.managers.AppManager;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    private boolean forgotPasswordModeEnabled, changePasswordModeEnabled, emailSent;
    private LinearLayout llSendToken, llPassword;
    private TextInputEditText etOldPassword, etNewPassword, etConfirmPassword, etEmail;
    private Button btnSendToken, btnChangePassword;
    private String email;
    private LinearLayout llOldPasswordError, llNewPasswordError, llConfirmPasswordError, llEmailError;
    private TextView tvOldPasswordErrorMessage, tvNewPasswordErrorMessage, tvConfirmPasswordErrorMessage, tvEmailErrorMessage;
    private TextInputLayout tilOldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        NukeSSLCerts.nuke();

        llPassword = findViewById(R.id.fp_change_password_ll);
        llSendToken = findViewById(R.id.fp_send_token_layout);
        llOldPasswordError = findViewById(R.id.fp_old_password_error_ll);
        llNewPasswordError = findViewById(R.id.fp_new_password_error_ll);
        llConfirmPasswordError = findViewById(R.id.fp_confirm_password_error_ll);
        llEmailError = findViewById(R.id.fp_email_error_ll);

        etOldPassword = findViewById(R.id.fp_old_password_et);
        etNewPassword = findViewById(R.id.fp_new_password_et);
        etConfirmPassword = findViewById(R.id.fp_confirm_password_et);
        etEmail = findViewById(R.id.fp_email_et);

        tilOldPassword = findViewById(R.id.fp_old_password_til);

        tvOldPasswordErrorMessage = findViewById(R.id.fp_old_password_error_messages_tv);
        tvNewPasswordErrorMessage = findViewById(R.id.fp_new_password_error_messages_tv);
        tvConfirmPasswordErrorMessage = findViewById(R.id.fp_confirm_password_error_messages_tv);
        tvEmailErrorMessage = findViewById(R.id.fp_email_error_messages_tv);

        btnSendToken = findViewById(R.id.fp_send_token_btn);
        btnChangePassword = findViewById(R.id.fp_change_password_btn);

        Intent intent = getIntent();
        forgotPasswordModeEnabled = intent.getBooleanExtra("forgot_password", false);
        changePasswordModeEnabled = intent.getBooleanExtra("change_password", false);
        emailSent = intent.getBooleanExtra("mail_sent", false);
        if(intent.hasExtra("email")){
            email = intent.getStringExtra("email");
        }

        if(forgotPasswordModeEnabled && emailSent){
            llSendToken.setVisibility(View.GONE);
            tilOldPassword.setVisibility(View.GONE);
        }else if(forgotPasswordModeEnabled){
            llPassword.setVisibility(View.GONE);
        }else if(changePasswordModeEnabled){
            llSendToken.setVisibility(View.GONE);
        }

        etOldPassword.addTextChangedListener(new ValidationTextWatcher(etOldPassword));
        etNewPassword.addTextChangedListener(new ValidationTextWatcher(etNewPassword));
        etConfirmPassword.addTextChangedListener(new ValidationTextWatcher(etConfirmPassword));
        etEmail.addTextChangedListener(new ValidationTextWatcher(etEmail));

        btnSendToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!emailIsValid(etEmail.getText().toString())){
                    AppManager.getAppManager().getInAppNotifier().showToast("Input error exists.");
                    return;
                }
                Intent intent = new Intent(ForgotPasswordActivity.this, EmailConfirmationActivity.class);
                intent.putExtra("resend_flag", true); //since a password request change request is issued a confirmation code should be sent to the user
                intent.putExtra("email", etEmail.getText().toString());
                startActivity(intent);
                finish();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasInputError()){
                    AppManager.getAppManager().getInAppNotifier().showToast("Input error exists.");
                    return;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.User.EMAIL, email);
                map.put("new_passphrase", etNewPassword.getText().toString());
                if(forgotPasswordModeEnabled){
                    map.put("mode", "forgot");
                }else if(changePasswordModeEnabled){
                    map.put("old_passphrase", etOldPassword.getText().toString());
                    map.put("mode", "change");
                }
                final CustomProgressDialog progressDialog = new CustomProgressDialog(ForgotPasswordActivity.this);
                progressDialog.showProgressDialog("Updating Password...");
                AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.UPDATE_PASSWORD_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            progressDialog.dismissDialog();
                            JSONObject object = new JSONObject(response);
                            if(!object.getBoolean("error")){
                                if(object.getBoolean("pass_updated")){
                                    AppManager.getAppManager().getInAppNotifier().showToast("Password updated successfully.");
                                    if(forgotPasswordModeEnabled){ // forgot password, restore it
                                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                                    }else if(changePasswordModeEnabled){ // knows password, wants to update it
                                        startActivity(new Intent(ForgotPasswordActivity.this, ChatActivity.class));
                                    }
                                }else{
                                    AppManager.getAppManager().getInAppNotifier().showToast(object.getString("message"));
                                }
                            }else{
                                AppManager.getAppManager().getInAppNotifier().showToast("Response error.");
                                AppManager.getAppManager().getInAppNotifier().showToast(object.getString("message"));
                            }
                        } catch (JSONException e) {
                            progressDialog.dismissDialog();
                            AppManager.getAppManager().getInAppNotifier().showToast("JSONException.");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismissDialog();
                        AppManager.getAppManager().getInAppNotifier().showToast("VolleyError.");
                    }
                }, map);
            }
        });
    }

    private boolean hasInputError() {
        boolean flag = false;
        if(changePasswordModeEnabled){
            if(Validator.isNullOrEmpty(etOldPassword.getText().toString())){
                tvOldPasswordErrorMessage.setText("Old password is required.");
                llOldPasswordError.setVisibility(View.VISIBLE);
                flag = true;
            }
        }
        if(Validator.isNullOrEmpty(etNewPassword.getText().toString())){
            tvNewPasswordErrorMessage.setText("New password is required.");
            llNewPasswordError.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(Validator.isNullOrEmpty(etConfirmPassword.getText().toString())){
            tvConfirmPasswordErrorMessage.setText("Please fill in the confirm password.");
            llConfirmPasswordError.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(etOldPassword.getText().toString())){
            if(etOldPassword.length() <8){
                tvOldPasswordErrorMessage.setText("Password must be at least 8 characters.");
                llOldPasswordError.setVisibility(View.VISIBLE);
            }
        }

        if(!Validator.isNullOrEmpty(etNewPassword.getText().toString())){
            if(etNewPassword.length() <8){
                tvNewPasswordErrorMessage.setText("Password must be at least 8 characters.");
                llNewPasswordError.setVisibility(View.VISIBLE);
            }
        }

        if(!Validator.isNullOrEmpty(etNewPassword.getText().toString()) && !Validator.isNullOrEmpty(etConfirmPassword.getText().toString())){
            if (!etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())){
                llConfirmPasswordError.setVisibility(View.VISIBLE);
                tvConfirmPasswordErrorMessage.setText("Passwords don't match.");
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

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch(view.getId()){
                case R.id.fp_old_password_et:
                   validateOldPasswordLength();
                    break;
                case R.id.fp_new_password_et:
                    validateNewPasswordLength();
                    break;
                case R.id.fp_confirm_password_et:
                    validateConfirmPassword();
                    break;
                case R.id.fp_email_et:
                    validateEmail(etEmail.getText().toString());
                    break;
                default:
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {

        }
    }

    private void validateConfirmPassword() {
        if(etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString()) || etConfirmPassword.getText().toString().length()==0){
            llConfirmPasswordError.setVisibility(View.GONE);
        }else{
            llConfirmPasswordError.setVisibility(View.VISIBLE);
            tvConfirmPasswordErrorMessage.setText("Passwords don't match.");
        }
    }

    private void validateOldPasswordLength() {
        String enteredPassword = etOldPassword.getText().toString();
        if(enteredPassword.length() == 0 || enteredPassword.length()>=8){
            llOldPasswordError.setVisibility(View.GONE);
        }else{
            tvOldPasswordErrorMessage.setText("Password must be at least 8 characters.");
            llOldPasswordError.setVisibility(View.VISIBLE);
        }
    }

    private void validateNewPasswordLength() {
        String enteredPassword = etNewPassword.getText().toString();
        if(enteredPassword.length() == 0 || enteredPassword.length()>=8){
            llNewPasswordError.setVisibility(View.GONE);
        }else{
            tvNewPasswordErrorMessage.setText("Password must be at least 8 characters.");
            llNewPasswordError.setVisibility(View.VISIBLE);
        }
    }

    private void validateEmail(String enteredEmail) {

        if(emailIsValid(enteredEmail) || etEmail.length() == 0){
            llEmailError.setVisibility(View.GONE);
        }else{
            tvEmailErrorMessage.setText("Please enter a valid email address.");
            llEmailError.setVisibility(View.VISIBLE);
        }
    }

    private boolean emailIsValid(String enteredEmail){
        String emailRegExp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern pattern = Pattern.compile(emailRegExp);
        return pattern.matcher(enteredEmail).matches();
    }
}