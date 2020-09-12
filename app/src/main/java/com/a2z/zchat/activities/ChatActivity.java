package com.a2z.zchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.a2z.zchat.R;
import com.a2z.zchat.managers.SharedPreferenceManager;

public class ChatActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        btnLogout = findViewById(R.id.chat_logout_btn);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceManager spManager = new SharedPreferenceManager(getApplicationContext());
                spManager.clearData();
                spManager.setLoginStatus(false);
                startActivity(new Intent(ChatActivity.this, LoginActivity.class));
                finishAffinity();
            }
        });
    }
}