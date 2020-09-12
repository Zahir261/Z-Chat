package com.zahir.zchat.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.zahir.zchat.constants.AppConstants;

public class SharedPreferenceManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    public SharedPreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(AppConstants.spName, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(AppConstants.SharedPref.LOG_IN_STATUS, false);
    }

    public void saveUserId(String userId){
        spEditor = sharedPreferences.edit();

        spEditor.putString(AppConstants.SharedPref.USER_ID, userId);
        spEditor.apply();
    }

    public void setLoginStatus(Boolean loginStatus){
        spEditor = sharedPreferences.edit();

        spEditor.putBoolean(AppConstants.SharedPref.LOG_IN_STATUS, loginStatus);
        spEditor.apply();
    }

    public void clearData(){
        sharedPreferences.edit().clear().apply();
    }
}
