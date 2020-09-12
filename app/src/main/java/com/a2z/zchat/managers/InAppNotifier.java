package com.a2z.zchat.managers;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class InAppNotifier {

    private Context context;

    private InAppNotifier(Context context) {
        this.context = context;
    }

    static InAppNotifier getInstance(Context context) {
        return new InAppNotifier(context);
    }

    private Toast toast = null;
    //private Dialog dialog = null;

//    public void showDialog(boolean show){
//        if (dialog == null){
//            dialog = new Dialog(context);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setCancelable(false);
//            dialog.setContentView(R.layout.layout_progress_dialog);
//            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        }
//
//        if (show){
//            dialog.show();
//        }else {
//            dialog.dismiss();
//        }
//    }

    public void showToast(String text) {
        dismiss();
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showLongToast(String text) {
        dismiss();
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

//    public void showToast(String text, int duration) {
//        dismiss();
//        toast = Toast.makeText(context, text, duration);
//        toast.show();
//    }

    public void showToast(String text, int toastPosition1, int toastPosition2, int xPositionOffset, int yPositionOffset, int duration) {
        dismiss();
        toast = Toast.makeText(context, text, duration);
        toast.setGravity(toastPosition1 | toastPosition2, xPositionOffset, yPositionOffset);
        toast.show();
    }

    public void showToast(String text, int duration, View view) {
        dismiss();
        toast = Toast.makeText(context, text, duration);
        toast.setView(view);
        toast.show();
    }

    private void dismiss() {
        if (toast != null) {
            toast.cancel();
        }
    }

    private static final boolean DEBUG_ON = true;

    public void log(String tag, String msg) {
        if (DEBUG_ON)
            Log.e(tag + "", "msg: " + msg);
    }
}
