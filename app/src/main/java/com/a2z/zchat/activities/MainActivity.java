package com.a2z.zchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.a2z.zchat.R;
import com.a2z.zchat.managers.SharedPreferenceManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferenceManager spManager = new SharedPreferenceManager(this);
        if(spManager.isLoggedIn()){
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        }else{
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        finish();
    }
}