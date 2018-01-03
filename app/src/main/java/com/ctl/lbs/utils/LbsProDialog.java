package com.ctl.lbs.utils;//package lbs.ctl.lbs.utils;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.NonNull;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import lbs.ctl.lbs.DataUpload;
//import lbs.ctl.lbs.R;
//import lbs.ctl.lbs.ui.MyApplication;
//
///**
// * Created by yuxingyu on 17/10/26.
// */
//
//public class LbsProDialog extends Dialog {
//
//
//    private Button startBtn;
//
//    private EditText ipEd;
//
//    private EditText portEd;
//
//    private TextView  tvTotal;
//
//    private ProgressBar proBar;
//
//    public LbsProDialog(final Context context, String filename, final List<Map<String,Object>> list) {
//        super(context);
//        setContentView(R.layout.layout);
//        setTitle("上传任务:"+filename);
//        setCanceledOnTouchOutside(false);
//
//        SharedPreferences sharedPreferences= context.getSharedPreferences("LBS", Activity.MODE_PRIVATE);
//        String ipB = sharedPreferences.getString("IP","192.168.1.105");
//        String portB = sharedPreferences.getString("PORT","80");
//
//        tvTotal = (TextView)findViewById(R.id.number_tv);
//        tvTotal.setText("本任务共"+list.size()+"条数据");
//        ipEd = (EditText) findViewById(R.id.sever_ip_edit);
//        ipEd.setText(ipB);
//        portEd = (EditText) findViewById(R.id.sever_port_edit);
//        portEd.setText(portB);
//
//        startBtn = (Button)findViewById(R.id.upload_action_btn);
//        startBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String ipN = ipEd.getText().toString();
//                String portN = portEd.getText().toString();
//                if(MyApplication.isIPAddress(ipN)&&portN.length()>0){
//                    SharedPreferences sharedPreferences= context.getSharedPreferences("AIRINTERFACE", Activity.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("IP",ipN);
//                    editor.putString("PORT",portN);
//                    editor.commit();
//                    DataUpload dataUpload = new DataUpload(context,list,handler);
//                    dataUpload.start();
//                }
//                else{
//                    Utils.alertDialog(context,"温馨提示","IP地址或端口号非法","知道了");
//                    return;
//                }
//            }
//        });
//
//    }
//
//
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if(msg.arg1==1000){
//                proBar.setProgress(msg.arg2);
//            }
//        }
//    };
//
//
//
//
//}
