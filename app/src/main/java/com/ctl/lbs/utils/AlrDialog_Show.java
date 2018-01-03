package com.ctl.lbs.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;

import com.ctl.lbs.R;

/**
 * Created by admin on 2016/8/11.
 */
public class AlrDialog_Show {
    public static void alertDialog(Context context, String title, String alertMsg, String btnStr)
    {
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.dialog))
                .setTitle(title)
                .setIcon(android.R.drawable.btn_star_big_on)
                .setMessage(alertMsg)
                .setPositiveButton(btnStr,new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();//密码错误弹出的警告对话框
    }
}
