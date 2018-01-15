package com.ctl.lbs.cell;

import com.baidu.mapapi.model.LatLng;
import com.ctl.lbs.utils.Gps2BaiDu;

import java.io.Serializable;
import java.lang.invoke.LambdaConversionException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by yu on 17/12/15.
 * @since 2017.12.15
 * @version 1.0.1
 * （1）arfcn A 代表频点  B 代表 pci或bsic或psc
 */
public class LuceCellInfo implements Serializable{

    /**
     * GPS纬度
     */
    private double latitudeGps;

    /**
     * GPS经度
     */
    private double longitudeGps;



    public double getBaiduLatitude(){
        return Gps2BaiDu.gpsToBaidu(latitudeGps,longitudeGps).latitude;
    }
    public double getBaiduLongitude(){
        return Gps2BaiDu.gpsToBaidu(latitudeGps,longitudeGps).longitude;
    }

    public LatLng getBaiduPoint(){
        return Gps2BaiDu.gpsToBaidu(latitudeGps,longitudeGps);
    }

    /**
     * 寻呼区 通俗称 大区
     */
    private int lac;

    /**
     * 基站ID 通俗称 小区
     */
    private int cellId;

    /**
     * 基站频点
     */
    private int arfcnA;

    /**
     * 基站PCI或BSIC或PSC
     */
    private int arfcnB;

    /**
     * 基站制式
     */
    private BtsType btsType;

    /**
     * 小区类型 主小区0或邻小区 1,2,3,4......
     */
    private int cellType;

    /**
     * 基站场强
     */
    private int rssi;

    /**
     * 采集时间 精度毫秒
     */
    private String time="";

    /**
     * CDMA基站系统识别码 同城市唯一
     */
    private int sid;

    /**
     * CDMA基站 网络识别码  一个城市仅1-2个
     */
    private int nid;

    /**
     * CDMA基站 基站ID 与其它制式的cellID同功能
     */
    private int bid;


    private String mark="";

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getMark() {
        return mark;
    }

    public double getLatitudeGps() {
        return latitudeGps;
    }

    public void setLatitudeGps(double latitudeGps) {
        this.latitudeGps = latitudeGps;
    }

    public double getLongitudeGps() {
        return longitudeGps;
    }

    public void setLongitudeGps(double longitudeGps) {
        this.longitudeGps = longitudeGps;
    }

    public int getLac() {
        return lac;
    }

    public String getLacStr(){
        if(btsType == BtsType.CDMA)
            return sid+","+nid;
        else return String.valueOf(lac);
    }
    public String getCiStr(){
        if(btsType == BtsType.CDMA)
            return String.valueOf(bid);
        else return String.valueOf(cellId);
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getArfcnA() {
        return arfcnA;
    }

    public void setArfcnA(int arfcnA) {
        this.arfcnA = arfcnA;
    }

    public int getArfcnB() {
        return arfcnB;
    }

    public void setArfcnB(int arfcnB) {
        this.arfcnB = arfcnB;
    }

    public BtsType getBtsType() {
        return btsType;
    }

    public  void setBtsType(BtsType btsType) {
        this.btsType = btsType;
    }

    public String getCellType() {
        if(cellType==0)return "主小区";
        else if(cellType>0) return "邻区";
        return null;
    }

    public int getCellTypeNum(){
        return cellType;
    }
    public void setCellType(int cellType) {
        this.cellType = cellType;
    }
    public void setCellType(String cellType) {
        if(cellType.equals("主小区"))

        this.cellType = 0;
        else
            this.cellType =1;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSid()  {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }


    public LuceCellInfo(){

//        Random random = new Random();
//
//        int s = random.nextInt(1000)%(1000-10+1) + 10;
//
//        lac = s;cellId=s;rssi=s;arfcnA=s;arfcnB=s;
    }

    public static String getCurrentTime(){
        SimpleDateFormat formatter   =   new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date curDate   =   new Date(System.currentTimeMillis());//获取当前时间
        String timeStr   =   formatter.format(curDate);
        return timeStr;
    }

    public boolean saveDb(){
        if(btsType==BtsType.CDMA){
            if(sid!=0&&nid!=0&&bid!=0){
                return true;
            }
        }
        else {
            if(lac!=0&&cellId!=0)
                return true;
        }

        return false;
    }


    @Override
    public String toString() {
        String cellTypeStr = "";
        if(cellType==0){
            cellTypeStr = "主小区";
        }else {
            cellTypeStr= "邻区"+0;
        }

        if(btsType==BtsType.CDMA)
            return btsType.toString()+","+cellTypeStr+","+"大区(sid)="+sid+",大区(nid)="+nid+",小区(bid)="+bid+",频点="+arfcnA+",扰码="+arfcnB+",强度="+rssi+",经度="+latitudeGps+",纬度="+longitudeGps+"\r\n";
        else
            return btsType.toString()+","+cellTypeStr+","+"大区(lac)="+lac+",小区(cellid)="+cellId+",频点="+arfcnA+",扰码="+arfcnB+",强度="+rssi+",经度="+latitudeGps+",纬度="+longitudeGps+"\r\n";
    }
}
