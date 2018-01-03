package com.ctl.lbs.utils;

/**
 * Created by admin on 2016/8/11.
 */
public class NumCheck {
    //判断是否是双精度字符串
    public static boolean isdouble(String str) {
        if(str.equals("")==true)
        {
            return false;
        }
        try {
            Double.valueOf(str).doubleValue();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //判断是否0-9数字
    public static boolean isNumeric(String str){
        if(str.equals("")==true)
        {
            return false;
        }
        for (int i = 0; i < str.length(); i++){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    //判断是否是十六进制字符串
    public static boolean isTrueHexDigit(String str) {
        if(str.equals("")==true)
        {
            return false;
        }
        try {
            Long.parseLong(str,16);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
