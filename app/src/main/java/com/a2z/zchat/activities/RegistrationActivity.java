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

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private String firstName, lastName, email, password, confirmPassword;
    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private TextView tvNameErrorMessages, tvEmailErrorMessage, tvPasswordErrorMessage, tvConfirmPasswordErrorMessage;
    private LinearLayout llName, llEmail, llPassword, llConfirmPassword;
    private boolean userExistFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        NukeSSLCerts.nuke();

        etFirstName = findViewById(R.id.reg_first_name_et);
        etLastName = findViewById(R.id.reg_last_name_et);
        etEmail = findViewById(R.id.reg_email_et);
        etPassword = findViewById(R.id.reg_password_et);
        etConfirmPassword = findViewById(R.id.reg_confirm_password_et);

        llName = findViewById(R.id.reg_name_error_ll);
        llEmail = findViewById(R.id.reg_email_error_ll);
        llPassword = findViewById(R.id.reg_password_error_ll);
        llConfirmPassword = findViewById(R.id.reg_confirm_password_error_ll);

        tvNameErrorMessages = findViewById(R.id.reg_name_error_messages_tv);
        tvEmailErrorMessage = findViewById(R.id.reg_email_error_messages_tv);
        tvPasswordErrorMessage = findViewById(R.id.reg_password_error_messages_tv);
        tvConfirmPasswordErrorMessage = findViewById(R.id.reg_confirm_password_error_messages_tv);

        Button btnSignUp = findViewById(R.id.reg_sign_up_btn);
        Button btnLogin = findViewById(R.id.reg_login_btn);

        etFirstName.addTextChangedListener(new ValidationTextWatcher(etFirstName));
        etLastName.addTextChangedListener(new ValidationTextWatcher(etLastName));
        etEmail.addTextChangedListener(new ValidationTextWatcher(etEmail));
        etPassword.addTextChangedListener(new ValidationTextWatcher(etPassword));
        etConfirmPassword.addTextChangedListener(new ValidationTextWatcher(etConfirmPassword));

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if (Validator.isEmailValid(Objects.requireNonNull(etEmail.getText()).toString())) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(AppConstants.User.EMAIL, etEmail.getText().toString());
                        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.CHECK_USER_EXISTENCE_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if(!object.getBoolean("error")){
                                        if(object.getBoolean("user_exists")){
                                            tvEmailErrorMessage.setText(R.string.userExistsMessage);
                                            llEmail.setVisibility(View.VISIBLE);
                                            userExistFlag = true;
                                        }else {
                                            userExistFlag = false;
                                            llEmail.setVisibility(View.GONE);
                                        }
                                    }
                                } catch (JSONException e) {
                                    AppManager.getAppManager().getInAppNotifier().log("JSONError", e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                AppManager.getAppManager().getInAppNotifier().log("VolleyError", error.toString());
                            }
                        }, map);
                    }
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                firstName = Objects.requireNonNull(etFirstName.getText()).toString().trim();
                lastName = Objects.requireNonNull(etLastName.getText()).toString().trim();
                email = Objects.requireNonNull(etEmail.getText()).toString().trim();
                password = Objects.requireNonNull(etPassword.getText()).toString();
                confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString();

                if(!hasErrorInput()){
                    proceedToSignUp();
                }else{
                    AppManager.getAppManager().getInAppNotifier().showToast("Input error exists.");
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean hasErrorInput() {
        boolean flag = false;

        if(Validator.isNullOrEmpty(firstName)){
            tvNameErrorMessages.setText(R.string.requireNameText);
            llName.setVisibility(View.VISIBLE);
            flag = true;
        }

        if (Validator.isNullOrEmpty(lastName)) {
            tvNameErrorMessages.setText(R.string.requireNameText);
            llName.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(Validator.isNullOrEmpty(email)){
            tvEmailErrorMessage.setText(R.string.requireEmailText);
            llEmail.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(email)){
            if(!Validator.isEmailValid(Objects.requireNonNull(etEmail.getText()).toString())){
                tvEmailErrorMessage.setText(R.string.requireValidEmailText);
                llEmail.setVisibility(View.VISIBLE);
                flag = true;
            }
        }

        if(Validator.isNullOrEmpty(password)){
            tvPasswordErrorMessage.setText(R.string.requirePassText);
            llPassword.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(password)){
            if(password.length() <8){
                tvPasswordErrorMessage.setText(R.string.validPassLengthText);
                llPassword.setVisibility(View.VISIBLE);
            }
        }

        if(Validator.isNullOrEmpty(confirmPassword)){
            tvConfirmPasswordErrorMessage.setText(R.string.requireConfirmPassText);
            llConfirmPassword.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(password) && !Validator.isNullOrEmpty(confirmPassword)){
            if (!password.equals(confirmPassword)){
                llConfirmPassword.setVisibility(View.VISIBLE);
                tvConfirmPasswordErrorMessage.setText(R.string.passMatchText);
                flag = true;
            }
        }

        if(userExistFlag){
            flag = true;
        }
        return flag;
    }

    private void proceedToSignUp() {
        final HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.User.FIRST_NAME, firstName);
        map.put(AppConstants.User.LAST_NAME, lastName);
        map.put(AppConstants.User.EMAIL, email);
        map.put(AppConstants.User.PASSPHRASE, password);
        map.put(AppConstants.User.VERIFIED, String.valueOf(0));
        final CustomProgressDialog progressDialog = new CustomProgressDialog(RegistrationActivity.this);
        progressDialog.showProgressDialog("Registering User...");
        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.USER_REG_URL, new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")){
                               if(object.getInt(AppConstants.User.ID) > 0) {
                                   progressDialog.dismissDialog();
                                   Intent intent = new Intent(RegistrationActivity.this, EmailConfirmationActivity.class);
                                   intent.putExtra("resend_flag", false); //false because the token is just generated
                                   intent.putExtra("email", Objects.requireNonNull(etEmail.getText()).toString());
                                   intent.putExtra("user_verification_mode", true);
                                   startActivity(intent);
                                   finish();
                               }else{
                                   progressDialog.dismissDialog();
                                   AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong");
                               }
                            }else{
                                progressDialog.dismissDialog();
                                AppManager.getAppManager().getInAppNotifier().showToast(object.getString("message"));
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
                        AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong");
                    }
                }, map);
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
                case R.id.reg_email_et:
                    validateEmail(Objects.requireNonNull(etEmail.getText()).toString());
                    break;
                case R.id.reg_password_et:
                    validatePasswordLength();
                    break;
                case R.id.reg_confirm_password_et:
                    validateConfirmPassword();
                case (R.id.reg_first_name_et):
                    validateNames();
                    break;
                case R.id.reg_last_name_et:
                    validateNames();
                    break;
                default:
                    break;
            }
        }

        public void afterTextChanged(Editable editable) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void validateNames() {
        if((Objects.requireNonNull(etFirstName.getText()).toString().length() == 0 && Objects.requireNonNull(etLastName.getText()).toString().length() == 0) ||
                (etFirstName.getText().toString().length() > 0 && Objects.requireNonNull(etLastName.getText()).toString().length() > 0)){
            llName.setVisibility(View.GONE);
        }else{
            tvNameErrorMessages.setText(R.string.requireNameText);
            llName.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void validateConfirmPassword() {
        if(Objects.requireNonNull(etPassword.getText()).toString().equals(Objects.requireNonNull(etConfirmPassword.getText()).toString()) || (etConfirmPassword.getText().toString().length() == 0)){
            llConfirmPassword.setVisibility(View.GONE);
        }else{
            llConfirmPassword.setVisibility(View.VISIBLE);
            tvConfirmPasswordErrorMessage.setText(R.string.passMatchText);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void validatePasswordLength() {
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
}