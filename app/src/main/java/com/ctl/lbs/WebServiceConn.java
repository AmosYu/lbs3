package com.ctl.lbs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import com.ctl.lbs.utils.MD5Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


/**
 * Created by yu on 15-11-26.
 */
public class WebServiceConn {
    private final String NAME_SPACE  = "http://webservice.collect.luce.ctl.com/";
    private final String METHOD_NAME = "message";
    private final String SOAP_ACTION = "\"http://webservice.collect.luce.ctl.com/message\"";

    /**
     * 192.168.0.185
     */
    private String serverAddress;
    private String devId = "000000000000000";
    private String key;

    private Context context;
    public WebServiceConn(Context context, int num) {
        if (num==0){
            this.context = context;
            SharedPreferences sharedPreferences= context.getSharedPreferences("AIRINTERFACE", Activity.MODE_PRIVATE);
            String ip = sharedPreferences.getString("IP","222.128.36.167");//222.128.36.167
            String port = sharedPreferences.getString("PORT","89");
            this.serverAddress = ip+":"+port;
            this.context = context;
            TelephonyManager telephonyManager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //根据接口文档验证登陆的时候的devId 和 key 值
            //分别为手机IMEI和经过MD5加密的key值
            devId = "000000000000000";
            key = MD5Utils.toMD5("terminal" + ":" + devId + ":LUCE_SYSTEM_CHECK");
        }
    }
    @SuppressLint("MissingPermission")
    public WebServiceConn(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences= context.getSharedPreferences("AIRINTERFACE", Activity.MODE_PRIVATE);
        String ip = sharedPreferences.getString("IP","192.168.1.105");
        String port = sharedPreferences.getString("PORT","80");
        this.serverAddress = ip+":"+port;
        this.context = context;
        TelephonyManager telephonyManager= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        devId = telephonyManager.getDeviceId();
        devId= "000000000000000";
        key = MD5Utils.toMD5("terminal" + ":" + devId + ":LUCE_SYSTEM_CHECK");
    }

    /**
     * 从服务器返回的消息中解析出tooken
     * @return
     */
    public boolean getToken(){
        JSONObject json = submitMsg(createJson());
        if(json==null) return false;
        try {
            String result = json.getString("result");
            if(result.equals("SUCCESS")){
                token = json.getString("token");
                System.out.println(token);
                return true;
            }
            else if(result.equals("ERROR")) {
                String ecode = json.getString("code");
//                if(ecode.equals("i0001")){
//                    Intent intent = new Intent();
//                    intent.setAction(TipHelper.ACTION_UI_REV_MESSAGE);
//                    intent.putExtra(TipHelper.EXTRA_UI_REV_MESSAGE, TipHelper.MSG_DEV_UNALLOWED);
//                    context.sendBroadcast(intent);
//                    DataUpload.terminate=true;
//                }
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String token;


    private String createJson() {
        JSONObject json = new JSONObject();
        try{
            json.put("type", "CHECK");
            json.put("device", "terminal");
            json.put("id", devId);
            json.put("key", key);
        }catch(JSONException ex){}
        return json.toString();
    }

    public String createJson(String fileName, int index, String msg){

        JSONObject json = new JSONObject();
        try {
            json.put("type", "UPLOAD");
            json.put("device", "terminal");
            json.put("index", index);
            json.put("name", fileName);
            json.put("content", msg);
            json.put("token",token);
        }catch (JSONException ex){}
        return json.toString();
    }

    /**
     * 查询基站位置
     * 除CDMA以外的基站位置
     * @return
     */
    public String FindPos(String mcc, String mnc, String lac, String cell){
        JSONArray array=new JSONArray();
        try {
            array.put(0,mcc);
            array.put(1,mnc);
            array.put(2,lac);
            array.put(3,cell);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject();
        try {
            json.put("type", "BSLOC");
            json.put("parameters", array);
            json.put("token",token);
        }catch (JSONException ex){}
        return json.toString();
    }

    /**
     * 查询CDMA基站位置
     * @param mcc
     * @param mnc
     * @param sid
     * @param nid
     * @param bid
     * @return
     */
    public String FindPos(String mcc, String mnc, String sid, String nid, String bid){
        JSONArray array=new JSONArray();
        try {
            array.put(0,mcc);
            array.put(1,mnc);
            array.put(2,sid);
            array.put(3,nid);
            array.put(4,bid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject json = new JSONObject();
        try {
            json.put("type", "BSLOC");
            json.put("parameters", array);
            json.put("token",token);
        }catch (JSONException ex){}
        return json.toString();
    }
    /**
     * 向服务器提交数据,并返回响应消息
     * @param  jsonMsg
     * @return
     */
    public JSONObject submitMsg(String jsonMsg) {
        String url = "http://"+serverAddress+"/collect/CommonWebService?wxdl";
        try {
            SoapObject rpc = new SoapObject(NAME_SPACE, METHOD_NAME);
            rpc.addProperty("json", jsonMsg);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.bodyOut = rpc;
            envelope.dotNet = true;

            envelope.setOutputSoapObject(rpc);
            HttpTransportSE ht = new HttpTransportSE(url,4000);
            ht.debug = true;
            ht.call(SOAP_ACTION, envelope);
            return new JSONObject(envelope.getResponse().toString());
        } catch (Exception e) {
            System.out.print("");
        }
        return null;
    }


    /**
     * 解析获取结果
     * @param json
     * @return 0 成功 1 token失效 2 异常 3 json解析异常 4其它异常
     */
    public int  getResult(JSONObject json){
        if(json==null) return 4;
        try {
            String result = json.getString("result");
            if(result.equals("SUCCESS")){
                return 0;
            }
            else if(result.equals("ERROR")) {
                String errCode = json.getString("code");
                if(errCode.equals("e0004")){
                    getToken();
                    return 1;
                }else if(errCode.equals("i0006")) {
                    return 1;
                }
                return 2;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 3;
        }
        return 4;
    }


//    public static String[] readServerIp(){
//       String filePath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"serverip.txt";
//        try {
//            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
//            String line = br.readLine();
//            String ip[] = line.split(",");
//            return ip;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public static void writeIpToFile(String ip){
//        String filePath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"serverip.txt";
//        BufferedWriter out = null;
//        try {
//            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath,false)));
//            out.write(ip);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                out.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}
