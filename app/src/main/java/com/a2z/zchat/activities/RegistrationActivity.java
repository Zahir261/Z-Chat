package com.a2z.zchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private String firstName, lastName, email, password, confirmPassword;
    private List<String> errorMessages = new ArrayList<>();
    private int genderId = 0;
    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp, btnLogin;
    private TextView tvNameErrorMessages, tvEmailErrorMessage, tvPasswordErrorMessage, tvConfirmPasswordErrorMessage;
    private LinearLayout llName, llEmail, llPassword, llConfirmPassword;
    private RadioGroup rgGender;
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
        rgGender = findViewById(R.id.reg_gender_rg);

        llName = findViewById(R.id.reg_name_error_ll);
        llEmail = findViewById(R.id.reg_email_error_ll);
        llPassword = findViewById(R.id.reg_password_error_ll);
        llConfirmPassword = findViewById(R.id.reg_confirm_password_error_ll);

        tvNameErrorMessages = findViewById(R.id.reg_name_error_messages_tv);
        tvEmailErrorMessage = findViewById(R.id.reg_email_error_messages_tv);
        tvPasswordErrorMessage = findViewById(R.id.reg_password_error_messages_tv);
        tvConfirmPasswordErrorMessage = findViewById(R.id.reg_confirm_password_error_messages_tv);

        btnSignUp = findViewById(R.id.reg_sign_up_btn);
        btnLogin = findViewById(R.id.reg_login_btn);

        etFirstName.addTextChangedListener(new ValidationTextWatcher(etFirstName));
        etLastName.addTextChangedListener(new ValidationTextWatcher(etLastName));
        etEmail.addTextChangedListener(new ValidationTextWatcher(etEmail));
        etPassword.addTextChangedListener(new ValidationTextWatcher(etPassword));
        etConfirmPassword.addTextChangedListener(new ValidationTextWatcher(etConfirmPassword));

        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.reg_male_rb:
                        genderId = 1;
                        break;
                    case R.id.reg_female_rb:
                        genderId = 2;
                        break;
                    default:
                        break;
                }
            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if (emailIsValid(etEmail.getText().toString())) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(AppConstants.User.EMAIL, etEmail.getText().toString());
                        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.CHECK_USER_EXISTENCE_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    if(!object.getBoolean("error")){
                                        if(object.getBoolean("user_exists")){
                                            tvEmailErrorMessage.setText("User already exists with this email. Please use a different email.");
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
            @Override
            public void onClick(View v) {
                firstName = etFirstName.getText().toString().trim();
                lastName = etLastName.getText().toString().trim();
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString();
                confirmPassword = etConfirmPassword.getText().toString();

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

    private boolean hasErrorInput() {
        boolean flag = false;

        if(Validator.isNullOrEmpty(firstName)){
            tvNameErrorMessages.setText("Please fill in both name.");
            llName.setVisibility(View.VISIBLE);
            flag = true;
        }

        if (Validator.isNullOrEmpty(lastName)) {
            tvNameErrorMessages.setText("Please fill in both name.");
            llName.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(Validator.isNullOrEmpty(email)){
            tvEmailErrorMessage.setText("Please fill in the email.");
            llEmail.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(email)){
            if(!emailIsValid(etEmail.getText().toString())){
                tvEmailErrorMessage.setText("Please enter a valid email address.");
                llEmail.setVisibility(View.VISIBLE);
                flag = true;
            }
        }

        if(Validator.isNullOrEmpty(password)){
            tvPasswordErrorMessage.setText("Please fill in the password.");
            llPassword.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(password)){
            if(password.length() <8){
                tvPasswordErrorMessage.setText("Password must be at least 8 characters.");
                llPassword.setVisibility(View.VISIBLE);
            }
        }

        if(Validator.isNullOrEmpty(confirmPassword)){
            tvConfirmPasswordErrorMessage.setText("Please fill in the confirm password.");
            llConfirmPassword.setVisibility(View.VISIBLE);
            flag = true;
        }

        if(!Validator.isNullOrEmpty(password) && !Validator.isNullOrEmpty(confirmPassword)){
            if (!password.equals(confirmPassword)){
                llConfirmPassword.setVisibility(View.VISIBLE);
                tvConfirmPasswordErrorMessage.setText("Passwords don't match.");
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
        map.put(AppConstants.User.GENDER_ID, String.valueOf(genderId));
        final CustomProgressDialog progressDialog = new CustomProgressDialog(RegistrationActivity.this);
        progressDialog.showProgressDialog("Registering User...");
        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.USER_REG_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean("error")){
                               if(object.getInt(AppConstants.User.ID) > 0) {
                                   progressDialog.dismissDialog();
                                   Intent intent = new Intent(RegistrationActivity.this, EmailConfirmationActivity.class);
                                   intent.putExtra("user_id", object.getInt(AppConstants.User.ID));
                                   intent.putExtra("mail_sent", false); //false because the token is just generated
                                   intent.putExtra("email", etEmail.getText().toString());
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

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            switch(view.getId()){
                case R.id.reg_email_et:
                    validateEmail(etEmail.getText().toString());
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

    private void validateNames() {
        if((etFirstName.getText().toString().length() == 0 && etLastName.getText().toString().length() == 0) ||
                (etFirstName.getText().toString().length() > 0 && etLastName.getText().toString().length() > 0)){
            llName.setVisibility(View.GONE);
        }else{
            tvNameErrorMessages.setText("Please fill in both name.");
            llName.setVisibility(View.VISIBLE);
        }
    }

    private void validateConfirmPassword() {
        if(etPassword.getText().toString().equals(etConfirmPassword.getText().toString()) || etConfirmPassword.getText().toString().length()==0){
            llConfirmPassword.setVisibility(View.GONE);
        }else{
            llConfirmPassword.setVisibility(View.VISIBLE);
            tvConfirmPasswordErrorMessage.setText("Passwords don't match.");
        }
    }

    private void validatePasswordLength() {
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
}