package com.ctl.lbs.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;


/**
 * Created by yuxingyu on 17/10/26.
 */

public class MyDialog extends Dialog {
    TextView textView;

    public MyDialog(Context context) {
        super(context);
        textView = new TextView(context);

        textView.setTextSize(20);
        textView.setText("ddfdasfsad");

        setContentView(textView);

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    message.arg1 = 2000;
                    message.arg2 = 123;
                    handler.sendMessage(message);
                }
            }
        }.start();

    }

    int num=0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1==2000){
                textView.setText(""+msg.arg2+"ds"+num++);
            }
        }
    };
}
