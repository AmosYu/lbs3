package com.ctl.lbs.bluetooth;

import android.content.Context;
import android.util.Log;

import com.ctl.lbs.cell.ProcessBtsData;

import java.io.IOException;

public class RevThread implements Runnable {

    private BluetoothConn blueConn;

    private ProcessBtsData processBtsData;

    private volatile boolean terminated = true;

    private Context context;

//    DbAcessImpl dbAcess;

    public RevThread(BluetoothConn blueConn, Context context,ProcessBtsData processBtsData) {
        this.blueConn = blueConn;

        this.processBtsData = processBtsData;

//        dbAcess = DbAcessImpl.getDbInstance(context);
        this.context = context;
    }

    public void run() {
        terminated = false;
        while(!terminated){
            String revMsg;
            try {
                revMsg = blueConn.revMsg();
                if (revMsg == null) continue;
                Log.i("接收", revMsg);
                if(revMsg.startsWith("+")){
                    processBtsData.proCmdResponse(revMsg);
                }
                else {
                    if (revMsg.length() < 9) continue;
                    String[] split = revMsg.split("=");
                    if (split.length < 2) continue;
                    try {
                        processBtsData.revData(split[0], split[1].replaceAll("\"", ""));
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        Log.i("越界数据", revMsg);
                    }
                }

            } catch (IOException e) {
                if(!terminated) {
                    terminated = true;
                    blueConn.setState(BluetoothState.ABORTED);
                }
                blueConn.closeConn();
            }
        }
        blueConn.closeConn();
    }


    public void terminated(){
        terminated = true;
    }
    public boolean isTerminated() {
        return terminated;
    }

}
