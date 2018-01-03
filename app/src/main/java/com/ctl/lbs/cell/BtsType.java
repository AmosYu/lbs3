package com.ctl.lbs.cell;

/**
 * Created by yu on 17/12/15.
 */

public enum BtsType {
    GSM_MOBILE,
    GSM_UNICOM,
    CDMA,
    TDSCDMA,
    WCDMA,
    WIFI,
    LTE_MOBILE,
    LTE_UNICOM,
    LTE_TELECOM,
    GPS;

    private  static String btsTypeStr[] = {"移动GSM","联通GSM","电信C","移动TD","联通W","WIFI","移动LTE","联通LTE","电信TE"};


    public String toString() {
        return btsTypeStr[this.ordinal()];
    }

    public static BtsType strToBtsType(String btsStr){

        if(btsStr.equals(btsTypeStr[0])){
            return GSM_MOBILE;
        }
        else if(btsStr.equals(btsTypeStr[1])){
            return GSM_UNICOM;
        }
        else if(btsStr.equals(btsTypeStr[2])){
            return CDMA;
        }
        else if(btsStr.equals(btsTypeStr[3])){
            return TDSCDMA;
        }
        else if(btsStr.equals(btsTypeStr[4])){
            return WCDMA;
        }
        else if(btsStr.equals(btsTypeStr[5])){
            return WIFI;
        }
        else if(btsStr.equals(btsTypeStr[6])){
            return LTE_MOBILE;
        }
        else if(btsStr.equals(btsTypeStr[7])){
            return LTE_UNICOM;
        }
        else if(btsStr.equals(btsTypeStr[8])){
            return LTE_TELECOM;
        }
        return GPS;
    }
}
