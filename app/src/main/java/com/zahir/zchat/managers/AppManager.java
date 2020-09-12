package com.zahir.zchat.managers;

import android.app.Application;
import android.content.Context;

public class AppManager extends Application {

    private static volatile AppManager appManager;
    private InAppNotifier inAppNotifier;
    private AppNetworkManager appNetworkController;

    public AppManager(){

        //Prevent form the reflection api.
        if (appManager != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        appManager = this;

        //MediaManager.init(this);
    }

    public static AppManager getAppManager(){
        if(appManager ==null){
            synchronized (AppManager.class){
                if(appManager == null) appManager = new AppManager();
            }
        }
        return appManager;
    }

    public InAppNotifier getInAppNotifier(){
        if(inAppNotifier==null){
            inAppNotifier = InAppNotifier.getInstance(getApplicationContext());
        }

        return inAppNotifier;
    }

    public AppNetworkManager getAppNetworkManager() {
        if(appNetworkController==null){
            appNetworkController = AppNetworkManager.getInstance(getApplicationContext());
        }

        return appNetworkController;
    }
}
