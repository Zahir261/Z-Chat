package com.zahir.zchat.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zahir.zchat.R;

public class CustomProgressDialog {
    private AlertDialog alertDialog;
    private Activity activity;

    public CustomProgressDialog(Activity mActivity){
        activity = mActivity;
    }

    public void showProgressDialog(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View customDialogView = inflater.inflate(R.layout.custom_progress_dialog, null);
        TextView textView = customDialogView.findViewById(R.id.cpd_text_tv);
        textView.setText(text);
        builder.setView(customDialogView);
        builder.setCancelable(false);
        alertDialog = builder.create();
        if(!alertDialog.isShowing()){
            alertDialog.show();
        }
    }

    public void dismissDialog(){
        if (alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }
}
