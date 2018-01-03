package com.ctl.lbs.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * Created by admin on 2016/8/9.
 */
public class dataCode {
    private Context context;
    public static int ip_index=0;//连接服务器失败!
    private static dataCode instance;
//    public static DatabaseHelper helper ;//= DatabaseHelper.getInstance(this);
    public String username="";
    public String password="";
    public String imei="";
    public String key_code="";
//    public static Config config_obj=new Config();
    public static int APP_TYPE=0; //0 用户本地APP  1 CTL用户APP
    public static int Local=0; //0 用户本地APP
    public static int Ctl=1;  //1 CTL用户APP
    public static Gps2BaiDu convert_obj=new Gps2BaiDu();
//    public  TopWang top_obj=null;
    private Handler mHandler;

    public static String con_erro="0";//连接服务器失败!
    public static String chek_erro="1";//认证失败
    public static String login_erro="2";//登录失败!
    public static String user_erro="3";//用户名不存在!
    public static String password_erro="4";//密码错误
    public static String device_erro="5";//设备未授权
    public static String date_erro="6";//设备授权过期
    public static String data_erro="7";//未找到数据
    public static String login_ok="ok";//登录成功

    public static int china_mobile=0;//移动2g
    public static int china_unicom=1;//联通2g
    public static int cdma=2;//电信
    public static int wcdma=3;//联通3G
    public static int td_swcdma=4;//移动3G
    public static int fdd_lte=5;//联通4G
    public static int tdd_lte=6;//移动4G
//    public LocationService locationService;
    public Vibrator mVibrator;
//    public  WebService loc_obj=null;

    public dataCode() {
        instance = this;
//        loc_obj = WebService.getInstance();
//        top_obj=TopWang.getInstance();

    }

    public void initData(Context context, Handler mHandler)
    {
       //dataObj=dataCode.getInstance();
        setContext(context);
        setMainHandler(mHandler);
//        InitWebService();

    }

    public static dataCode getInstance()
    {
        if (dataCode.instance == null) {
            dataCode.instance = new dataCode();
        }
        return dataCode.instance;
    }

    public String getUser()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getImei()
    {
        return imei;
    }

    public void  setContext(Context context)
    {
        this.context=context;
    }

    private String Get_imei()
    {
        TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
       // String IMEI =android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
        String imei=telephonyManager.getDeviceId();
        Log.d("IMEI:", imei);
        return imei;
    }

    public void setMainHandler(Handler handler){
        mHandler=handler;
    }

    public Handler GetMainHandler(){
        return mHandler;
    }

//    public void SetContext(Context obj)
//    {
//        config_obj.SetContext(obj);
//    }


//    public void InitWebService()
//    {
//        loc_obj.Init();
//        config_obj.SetContext(context);
//        config_obj.copyFileToSD();
//        imei=Get_imei();
//        loc_obj.imei=imei;
//        ArrayList<String> msg=config_obj.readSDcard();
//        if(msg!=null)
//        {
//            String line;
//            int len;
//            for(int i=0;i<msg.size();i++)
//            {
//                line=msg.get(i);
//                len=line.length();
//                if(line.contains("IP"))
//                {
//                    loc_obj.ip_str=line.substring(5,len);
//                }
//                else if(line.contains("name"))
//                {
//                    loc_obj.user_str=line.substring(5,len);
//                    username=loc_obj.user_str;
//                }
//                else if(line.contains("code"))
//                {
//                    loc_obj.password=line.substring(5,len);
//                    password=loc_obj.password;
//                }
//                else if(line.contains("key"))
//                {
//                    loc_obj.key_code=line.substring(5,len);
//                }
//            }
//        }
//    }
}
