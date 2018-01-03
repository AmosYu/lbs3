package com.ctl.lbs.cell;

import com.baidu.mapapi.model.LatLng;
import com.ctl.lbs.utils.Gps2BaiDu;

/**
 * Created by yu on 2016/11/10.
 */

public class WifiInfo {
    private String mac="";
    private String chanel="";
    private String type="";
    private String rssi="";
    private double latitude,longitude;
    private String mark;

    public double getBaiduLatitude(){
        return Gps2BaiDu.gpsToBaidu(latitude,longitude).latitude;
    }
    public double getBaiduLongitude(){
        return Gps2BaiDu.gpsToBaidu(latitude,longitude).longitude;
    }

    public LatLng getBaiduPoint(){
        return Gps2BaiDu.gpsToBaidu(latitude,longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getChanel() {
        return chanel;
    }

    public void setChanel(String chanel) {
        this.chanel = chanel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return BtsType.WIFI+","+"MAC="+mac+",类型="+type+",强度="+rssi+"\r\n";
    }
}
