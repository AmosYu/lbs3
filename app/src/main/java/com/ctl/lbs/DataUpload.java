package com.ctl.lbs;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ctl.lbs.database.DbAcessImpl;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;




/**
 * Created by yu on 15-11-25.
 */
public class DataUpload extends Thread {


    private Context context;

    public static volatile boolean terminate = true;
    private ArrayList<String> markList;
    private Handler handler;

    /**
     * @param context
     * @param markList 文件名称列表
     * @param handler  向外传递进度消息
     */
    public DataUpload(Context context, ArrayList<String> markList, Handler handler) {
        this.context = context;
        this.markList = markList;
        this.handler = handler;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    @Override
    public void run() {
        terminate = false;
        WebServiceConn conn = new WebServiceConn(context);
        int timesFailed = 0;
        while (!terminate) {
            sendHandlerMsg("准备上传，开始连接服务器...");
            if (conn.getToken()) {//服务器上传之前先取验证token
                timesFailed = 0;
                sendHandlerMsg("服务器连接成功，访问权限验证通过。");
                break;
            }else {
                timesFailed++;      //验证token失败三次停止
                sendHandlerMsg("服务器连接失败，正在重新连接...");;
                if(timesFailed==3){
                    sendHandlerMsg("服务器连接失败，请您检查网络或核实ip地址和端口号");
                }
                if(timesFailed==3) return;
            }
            delay(2);
        }
        int number = 0;
        //取目录名称
        DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
        while (!terminate && markList.size() > 0) {
            String mark = markList.get(0);
            markList.remove(0);
            List<Map<String, Object>> mapList = dbAcess.selectByFile(mark);
            String fileMsg = "";
            int countItem = 0;
            for (Map<String, Object> map : mapList) {
                fileMsg = fileMsg + map.get("lac_sid").toString() + ","
                        + map.get("ci_nid").toString() + ","
                        + map.get("bid").toString() + ","
                        + map.get("arfcn").toString() + ","
                        + map.get("rssi").toString() + ","
                        + map.get("latitude").toString() + ","
                        + map.get("longitude").toString() + ","
                        + map.get("time").toString() + ","
                        + map.get("zhishi").toString() + ","+","+";";
                countItem++;
            }
            commitMsgToServer(conn,fileMsg,mark,countItem,dbAcess);
        }
        sendHandlerMsg("上传完毕");
    }


    private void delay(int sec) {
        try {
            Thread.sleep(1000 * sec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendHandlerMsg(Object msgObject) {
        Message msg = new Message();
        msg.arg2 = 3000;
        msg.arg1 = 3001;
        msg.obj = msgObject;
        handler.sendMessage(msg);
    }
    private void commitMsgToServer(WebServiceConn conn, String fileMsg, String mark, int number, DbAcessImpl dbAcess){
        int times = 2;
        number++;
        while (times > 0 && !terminate) {//开始提交文件
            times--;
            JSONObject result = conn.submitMsg(conn.createJson(mark, number, fileMsg));
            int code = conn.getResult(result);
            if (code == 0) {//成功 删除
                dbAcess.upDateMark(mark, 1);
                sendHandlerMsg(" 文件" + mark + "已上传" + number+ "条基站数据");
                times = -1;
            } else if (code == 1) { //如果token失效，再次获取token
                conn.getToken();
                times = 1;
                sendHandlerMsg( " 文件" + mark + "上传超时");
            } else {
                sendHandlerMsg( " 文件" + mark + "上传失败");
            }
        }
    }
}

