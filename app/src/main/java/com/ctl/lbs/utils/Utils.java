package com.ctl.lbs.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by yuxingyu on 17/10/24.
 */

public class Utils {
    public static void alertDialog(Context context, String title, String alertMsg, String btnStr) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(android.R.drawable.btn_star_big_on)
                .setMessage(alertMsg)
                .setPositiveButton(btnStr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }


    public static void delay(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
