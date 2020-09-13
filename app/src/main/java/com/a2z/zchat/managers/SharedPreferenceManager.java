package com.a2z.zchat.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.a2z.zchat.constants.AppConstants;

public class SharedPreferenceManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    public SharedPreferenceManager(Context context){
        sharedPreferences = context.getSharedPreferences(AppConstants.spName, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(AppConstants.SharedPref.LOG_IN_STATUS, false);
    }

    public String getSavedEmail(){
        return sharedPreferences.getString(AppConstants.SharedPref.USER_EMAIL, "");
    }

    public void setUserId(String userId){
        spEditor = sharedPreferences.edit();

        spEditor.putString(AppConstants.SharedPref.USER_ID, userId);
        spEditor.apply();
    }

    public void setUserEmail(String email){
        spEditor = sharedPreferences.edit();

        spEditor.putString(AppConstants.SharedPref.USER_EMAIL, email);
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
