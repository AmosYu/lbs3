package com.ctl.lbs.cell;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctl.lbs.bluetooth.BluetoothConn;
import com.ctl.lbs.database.DbAcessImpl;
import com.ctl.lbs.database.SdCardOperate;
import com.ctl.lbs.ui.MainActivity;
import com.ctl.lbs.utils.Gps2BaiDu;
import com.ctl.lbs.utils.Utils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by yu on 17/12/16.
 */

public class ProcessBtsData extends Observable{

    private Context context;

    private int number;

    DbAcessImpl dbAcess;
    private BluetoothConn bluetoothConn;

    public  static String mark = "test";

    public ProcessBtsData(Context context, BluetoothConn bluetoothConn){
        this.context = context;
        dbAcess = DbAcessImpl.getDbInstance(context);
        this.bluetoothConn = bluetoothConn;

    }

    private BtsType currentType = BtsType.GSM_MOBILE;

    public BtsType getCurrentType() {
        return currentType;
    }

    public void setCurrentType(BtsType currentType) {
        this.currentType = currentType;
    }

    private ArrayList<LuceCellInfo> gsmMobileList = new ArrayList<>();
    private ArrayList<LuceCellInfo> gsmUnicomList = new ArrayList<>();
    private ArrayList<LuceCellInfo> wcdmaUnicomList = new ArrayList<>();
    private ArrayList<LuceCellInfo> cdmaTelecomList = new ArrayList<>();
    private ArrayList<LuceCellInfo> tdMobileList = new ArrayList<>();
    private ArrayList<LuceCellInfo> lteMobileList = new ArrayList<>();
    private ArrayList<LuceCellInfo> lteUnicomList = new ArrayList<>();
    private ArrayList<LuceCellInfo> lteTelecomList = new ArrayList<>();
    public ArrayList<WifiInfo>  wifiList = new ArrayList<>();

    public ArrayList<LuceCellInfo> getLuceInfoList(){

        switch (currentType){

            case GSM_MOBILE:
                return gsmMobileList;

            case GSM_UNICOM:
                return gsmUnicomList;

            case LTE_MOBILE:
                return lteMobileList;

            case LTE_UNICOM:
                return lteUnicomList;

            case LTE_TELECOM:
                return lteTelecomList;

            case CDMA:
                return cdmaTelecomList;

            case WCDMA:
                return wcdmaUnicomList;

            case TDSCDMA:
                return tdMobileList;
        }
        return new ArrayList<LuceCellInfo>();
    }

    private double latitude=0,longitude=0;

    private LatLng lastPoint = null;

    /**
     * GPS 是否有效的标识 true 有效
     */
    private boolean gpsEffective= false;

    /**
     *
     * @param cmd 消息类型
     * @param revMsg 消息内容
     * @throws ArrayIndexOutOfBoundsException
     */
    public void revData(String cmd,String revMsg) throws ArrayIndexOutOfBoundsException{

        String revMsgSplit[] = revMsg.split(":");
        if("CX0".equals(cmd)){
            this.number = Integer.parseInt(revMsgSplit[0]);
            setChanged();
            notifyObservers("CLEAR");
            bluetoothConn.sendCmd("BC+SETGPS=116.327187,39.975637,4.9E-324");
            Utils.delay(500);
            gsmMobileList.clear();
            lteUnicomList.clear();
            gsmUnicomList.clear();
            wifiList.clear();
            cdmaTelecomList.clear();
            wcdmaUnicomList.clear();
            tdMobileList.clear();
            lteTelecomList.clear();
            lteMobileList.clear();
            LatLng latLng = Gps2BaiDu.gpsToBaidu(latitude,longitude);
            if(DistanceUtil.getDistance(latLng,lastPoint)>10){
                lastPoint = latLng;
            }

            if(revMsgSplit.length==2){
                if(revMsgSplit[1]!=null&&revMsgSplit[1].length()>0){
                    setRemark(revMsgSplit[1]);
                    remarkNumber = this.number;
                }
            }

        }
        else if("WX1".equals(cmd)){
            int number = Integer.parseInt(revMsgSplit[0]);
            if(number!=this.number)return;
            String wifi = revMsg.substring(6);
            String wifiArray[] = wifi.split(" ");
            if(wifiArray.length==9){
                WifiInfo wifiInfo = new WifiInfo();
                wifiInfo.setType(wifiArray[0]);
                wifiInfo.setChanel(wifiArray[2]);
                wifiInfo.setMac(wifiArray[5]);
                wifiInfo.setRssi(wifiArray[8]);
                wifiInfo.setLatitude(latitude);
                wifiInfo.setLongitude(longitude);
                wifiInfo.setMark(mark);
                setChanged();
                notifyObservers(wifiInfo);
                wifiList.add(wifiInfo);
                if(saveToDb){
                    if(gpsEffective){

                        LatLng latLng = Gps2BaiDu.gpsToBaidu(latitude,longitude);
                        if(DistanceUtil.getDistance(latLng,lastPoint)>10){
                            dbAcess.insertWifiInfo(wifiInfo);
                        }
                    }
                }
                if(number==remarkNumber){
                    SdCardOperate sdCardOperate = new SdCardOperate(dirPath);
                    sdCardOperate.writeMsgToFile(wifiInfo.toString(),remark);
                }
            }
        }
        else if ("GX1".equals(cmd)){
            int number = Integer.parseInt(revMsgSplit[0]);
            if(number!=this.number)return;
            String gpsArray[]=revMsgSplit[1].split(",");
            if("GPRMC".equals(gpsArray[0])){
                if("A".equals(gpsArray[2])){
                    gpsEffective = true;
                    if (gpsArray[3].length() >= 8 && gpsArray[5].length() >= 9) {
                        double latDu = Double.parseDouble(gpsArray[3].substring(0, 2));
                        double latFen = Double.parseDouble(gpsArray[3].substring(2)) / 60;
                        double lngDu = Double.parseDouble(gpsArray[5].substring(0, 3));
                        double lngFen = Double.parseDouble(gpsArray[5].substring(3)) / 60;
                        BigDecimal lat = new BigDecimal(latDu + latFen);
                        latitude = lat.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                        BigDecimal lng = new BigDecimal(lngDu + lngFen);
                        longitude = lng.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }
                else{
                    gpsEffective = false;
                    latitude = 0;
                    longitude = 0;
                    if(MainActivity.baiduLatitude!=0&&MainActivity.baiduLongitude!=0){
                        LatLng latLng =Gps2BaiDu.baiduToGps(MainActivity.baiduLatitude,MainActivity.baiduLongitude);
                        latitude = latLng.latitude;
                        longitude = latLng.longitude;
//                        gpsEffective = true;
                    }
                }
                setChanged();
                notifyObservers(gpsEffective);
            }


        }
        else {

            if(revMsgSplit.length<2) return;

            int number = Integer.parseInt(revMsgSplit[0]);

            if(this.number != number) return;

            LuceCellInfo lbsCellInfo = null;

            String btsMsg[] = revMsgSplit[1].split(",");

            if ("MX1".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.GSM_MOBILE,btsMsg);
                gsmMobileList.add(lbsCellInfo);
            }
            else if ("MX2".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.GSM_UNICOM,btsMsg);
                gsmUnicomList.add(lbsCellInfo);

            } else if ("MX3".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.LTE_MOBILE,btsMsg);
                lteMobileList.add(lbsCellInfo);

            } else if ("MX4".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.WCDMA,btsMsg);
                wcdmaUnicomList.add(lbsCellInfo);

            } else if ("MX5".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.TDSCDMA,btsMsg);
                tdMobileList.add(lbsCellInfo);

            } else if ("MX6".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.LTE_UNICOM,btsMsg);
                lteUnicomList.add(lbsCellInfo);

            } else if ("MX7".equals(cmd)) {
                lbsCellInfo = getLbsCellInfo(BtsType.LTE_TELECOM,btsMsg);
                lteTelecomList.add(lbsCellInfo);

            } else if ("MX8".equals(cmd)) {
                lbsCellInfo = getCdmaCellInfo(btsMsg);
                cdmaTelecomList.add(lbsCellInfo);
            }
            if(!"MX8".equals(cmd)){
                if(lbsCellInfo.getLac()==0&&lbsCellInfo.getCellId()==0&&lbsCellInfo.getArfcnA()==0){
                    return;
                }
            }

            if(lbsCellInfo == null) return;
//            setChanged();
//            notifyObservers(lbsCellInfo);

            if(saveToDb){
                if(gpsEffective&&lbsCellInfo.saveDb()){
                    lbsCellInfo.setLongitudeGps(longitude);
                    lbsCellInfo.setLatitudeGps(latitude);
                    lbsCellInfo.setMark(mark);
                    LatLng latLng = Gps2BaiDu.gpsToBaidu(latitude,longitude);
//                    if(DistanceUtil.getDistance(latLng,lastPoint)>10){
                        dbAcess.insertCellMapInfo(lbsCellInfo);
//                    }
                }
            }

            if(number==remarkNumber){
                SdCardOperate sdCardOperate = new SdCardOperate(dirPath);
                sdCardOperate.writeMsgToFile(lbsCellInfo.toString(),remark);
            }

        }

    }

    public String dirPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "手动采集";

    private boolean saveToDb = false;

    public boolean isSaveToDb() {
        return saveToDb;
    }

    public void setSaveToDb(boolean saveToDb) {
        this.saveToDb = saveToDb;
    }

    private LuceCellInfo getCdmaCellInfo(String btsMsg[]){
        try {

            int sid = Integer.parseInt(btsMsg[2]);

            int nid = Integer.parseInt(btsMsg[3]);

            int bid = Integer.parseInt(btsMsg[4]);

            int arfcnA = Integer.parseInt(btsMsg[5]);

            int arfcnB = Integer.parseInt(btsMsg[6]);

            int rssi = Integer.parseInt(btsMsg[7]);

            LuceCellInfo lbsCellInfo = new LuceCellInfo();
            lbsCellInfo.setCellType(Integer.parseInt(btsMsg[0]));
            lbsCellInfo.setSid(sid);
            lbsCellInfo.setNid(nid);
            lbsCellInfo.setBid(bid);
            lbsCellInfo.setArfcnA(arfcnA);
            lbsCellInfo.setArfcnB(arfcnB);
            lbsCellInfo.setRssi(rssi);
            lbsCellInfo.setBtsType(BtsType.CDMA);


            return lbsCellInfo;
        }catch (Exception ex){
            Log.e("CDMA","异常");
            return null;
        }
    }

    private LuceCellInfo getLbsCellInfo(BtsType btsType,String[] btsMsg){

        try {
            int lac = 0;
            int ci = 0;
            if (btsMsg[4].equals("FFFF")) {
                lac = 0;
            } else {

                lac = Integer.parseInt(btsMsg[4], 16);
            }

            if (btsMsg[5].equals("FFFFFFFF")) {
                ci = 0;
            } else {
                ci = Integer.parseInt(btsMsg[5], 16);
            }

            int arfcnA = Integer.parseInt(btsMsg[6]);
            if (arfcnA == 65535) arfcnA = 0;

            int arfcnB = Integer.parseInt(btsMsg[7]);

            int rssi = Integer.parseInt(btsMsg[8]);

            LuceCellInfo lbsCellInfo = new LuceCellInfo();
            lbsCellInfo.setCellType(Integer.parseInt(btsMsg[0]));
            lbsCellInfo.setLac(lac);
            lbsCellInfo.setCellId(ci);
            lbsCellInfo.setArfcnA(arfcnA);
            lbsCellInfo.setArfcnB(arfcnB);
            lbsCellInfo.setRssi(rssi);
            lbsCellInfo.setBtsType(btsType);
            lbsCellInfo.setTime(LuceCellInfo.getCurrentTime());
            return lbsCellInfo;
        }catch(Exception ex){
            return null;
        }

    }

    private String remark ;

    private int remarkNumber=0;

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void proCmdResponse(String revCmd){

        if(revCmd.equals("+SETINF=OK")){

        }
        else if(revCmd.equals("+TRIGES=OK")){

            setChanged();
            notifyObservers(1000);

            new Thread(){
                @Override
                public void run() {
                    Utils.delay(3000);
                    bluetoothConn.sendCmd("BC+SETDUR=03");

                }
            }.start();

        }

    }

}
