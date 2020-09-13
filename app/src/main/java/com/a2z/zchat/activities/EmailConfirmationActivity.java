package com.a2z.zchat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.a2z.zchat.helpers.Validator;
import com.a2z.zchat.managers.SharedPreferenceManager;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.textfield.TextInputEditText;
import com.a2z.zchat.R;
import com.a2z.zchat.constants.AppConstants;
import com.a2z.zchat.constants.ServerConstants;
import com.a2z.zchat.helpers.CustomProgressDialog;
import com.a2z.zchat.helpers.NukeSSLCerts;
import com.a2z.zchat.managers.AppManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EmailConfirmationActivity extends AppCompatActivity {

    private TextInputEditText etConfirmationCode;
    private Button btnConfirm, btnResendCode;
    private TextView tvMessage;
    private LinearLayout llConfirmation;
    private String email;
    private boolean mailShouldBeSent, isConfirmationCounterRunning = false, isResendCounterRunning = false, userVerificationMode = false;
    private CountDownTimer confirmationCounter, resendCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confirmation);

        NukeSSLCerts.nuke();

        etConfirmationCode = findViewById(R.id.ce_confirmation_code_et);
        btnConfirm = findViewById(R.id.ce_confirm_btn);
        btnResendCode = findViewById(R.id.ce_resend_code_btn);
        tvMessage = findViewById(R.id.ce_message_tv);
        llConfirmation = findViewById(R.id.ce_confirmation_ll);

        Intent intent = getIntent();
        if(intent != null){
            mailShouldBeSent = intent.getBooleanExtra("resend_flag", false); //(false: send first mail, true: already sent once and the token expired, now resend) -> it is to determine if token needs to be updated
            email = intent.getStringExtra("email");
            if (intent.hasExtra("user_verification_mode")){
                userVerificationMode = intent.getBooleanExtra("user_verification_mode", false);
            }
        }

        confirmationCounter = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnConfirm.setText("Confirm " + formatTime(millisUntilFinished/1000));
                isConfirmationCounterRunning = true;
            }

            @Override
            public void onFinish() {
                tvMessage.setText("Confirmation code was expired. Press RESEND CODE to send the code again.");
                llConfirmation.setVisibility(View.GONE);
                isConfirmationCounterRunning = false;
            }
        };

        resendCounter = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnResendCode.setText("RESEND CODE " + formatTime(millisUntilFinished/1000));
                isResendCounterRunning = true;
            }

            @Override
            public void onFinish() {
                btnResendCode.setEnabled(true);
                btnResendCode.setText("Resend Code");
                btnResendCode.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                isResendCounterRunning = false;
            }
        };

        sendConfirmationCode();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Validator.isNullOrEmpty(etConfirmationCode.getText().toString())){
                    AppManager.getAppManager().getInAppNotifier().showToast("Please enter confirmation code.");
                    return;
                }
                HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.User.EMAIL, email);
                map.put(AppConstants.User.TOKEN, etConfirmationCode.getText().toString());
                map.put("user_verification", String.valueOf(userVerificationMode));
                final CustomProgressDialog progressDialog = new CustomProgressDialog(EmailConfirmationActivity.this);
                progressDialog.showProgressDialog("Confirming User...");
                AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.CONFIRM_EMAIL_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if(!object.getBoolean("error")){
                                if(object.getBoolean("token_exists")){
                                    if(userVerificationMode){
                                        if(object.getBoolean("verification_status_updated")){
                                            progressDialog.dismissDialog();
                                            AppManager.getAppManager().getInAppNotifier().showToast("Your account was successfully verified.");
                                            SharedPreferenceManager spManager = new SharedPreferenceManager(getApplicationContext());
                                            spManager.setUserId(String.valueOf(object.getInt(AppConstants.User.ID)));
                                            spManager.setLoginStatus(true);
                                            stopCounters();
                                            startActivity(new Intent(EmailConfirmationActivity.this, ChatActivity.class));
                                            finish();
                                        }else{
                                            progressDialog.dismissDialog();
                                            AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Please try again.");
                                        }
                                    }else{
                                        Intent intent = new Intent(EmailConfirmationActivity.this, ForgotPasswordActivity.class);
                                        intent.putExtra("forgot_password", true);
                                        intent.putExtra("change_password", false);
                                        intent.putExtra("mail_sent", true);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                        finish();
                                    }
                                }else{
                                    progressDialog.dismissDialog();
                                    AppManager.getAppManager().getInAppNotifier().showToast("Token does not match. Please provide a valid token.");
                                }
                            }
                        } catch (JSONException e) {
                            progressDialog.dismissDialog();
                            AppManager.getAppManager().getInAppNotifier().showToast(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismissDialog();
                        AppManager.getAppManager().getInAppNotifier().showToast(error.toString());
                    }
                }, map);
            }
        });

        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mailShouldBeSent = true;
                sendConfirmationCode();
            }
        });
    }

    private void resetCounters() {
        stopCounters();
        confirmationCounter.start();
        resendCounter.start();
    }

    private void stopCounters() {
        if(isConfirmationCounterRunning){
            confirmationCounter.cancel();
            isConfirmationCounterRunning = false;
        }
        if(isResendCounterRunning){
            resendCounter.cancel();
            isResendCounterRunning = false;
        }
    }

    private void sendConfirmationCode() {
        final CustomProgressDialog progressDialog = new CustomProgressDialog(EmailConfirmationActivity.this);
        HashMap<String, String> map = new HashMap<>();
        map.put("resend_flag", String.valueOf(mailShouldBeSent));
        map.put(AppConstants.User.EMAIL, email);
        AppManager.getAppManager().getInAppNotifier().log("map", map.toString());
        progressDialog.showProgressDialog("Please Wait...");
        AppManager.getAppManager().getAppNetworkManager().makeRequest(ServerConstants.SEND_CODE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    AppManager.getAppManager().getInAppNotifier().log("response", response);
                    progressDialog.dismissDialog();
                    if(!object.getBoolean("error")){
                        if(object.getBoolean("mail_sent")){
                            resetCounters();
                            btnResendCode.setEnabled(false);
                            llConfirmation.setVisibility(View.VISIBLE);
                            if(mailShouldBeSent){
                                tvMessage.setText("Verification code was resent to your email. Enter code to confirm.");
                            }else{
                                tvMessage.setText("Verification code was sent to your email. Enter code to confirm.");
                            }
                        }else{
                            stopCounters();
                            btnResendCode.setEnabled(true);
                            AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Press RESEND CODE to send the code again.");
                        }
                    }else{
                        stopCounters();
                        btnResendCode.setEnabled(true);
                        AppManager.getAppManager().getInAppNotifier().showToast("Something went wrong. Press RESEND CODE to send the code again.");
                    }
                } catch (JSONException e) {
                    AppManager.getAppManager().getInAppNotifier().log("JSON", e.toString());
                    progressDialog.dismissDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppManager.getAppManager().getInAppNotifier().log("Volley", error.toString());
                progressDialog.dismissDialog();
            }
        }, map);
    }

    private String formatTime(long seconds) {
        String output;
        long minutes = seconds / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        String secondsD = String.valueOf(seconds);
        String minutesD = String.valueOf(minutes);

        if (seconds < 10)
            secondsD = "0" + seconds;
        if (minutes < 10)
            minutesD = "0" + minutes;

        output = minutesD + ":" + secondsD;

        return output;
    }
}