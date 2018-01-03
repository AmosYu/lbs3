package com.ctl.lbs.utils;

import android.util.Base64;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 2016/8/9.
 */
public class Gps2BaiDu {
    //google 坐标转百度链接   //http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=116.32715863448607&y=39.990912172420714&callback=BMap.Convertor.cbk_3694
    //gps坐标的type=0
    //google坐标的type=2
    //baidu坐标的type=4
    String path = "";

    public String[] get_convert(double x, double y, int type) {
        Log.d("get_convert", "get_convert");
        String[] outstr = new String[2];
        outstr[0] = "";
        outstr[1] = "";
        try {
            //使用http请求获取转换结果
            if (type == 0) {
                path = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=" + x + "+&y=" + y;
            } else {
                path = "http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=" + x + "+&y=" + y;
            }
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            //得到返回的结果
            String res = outStream.toString();
            //处理结果
            Log.d("res", res);
            if (res.indexOf("{") >= 0 && res.indexOf("}") > 0 && res.length() > 12) {
                String err = res.substring(res.indexOf("error") + 7, res.indexOf("error") + 8);
                Log.d("err", err);
                if ("0".equals(err)) {
                    int lonindex = res.indexOf(",\"y\":");
                    String lonstr = res.substring(res.indexOf("x\":\"") + 4, lonindex - 1);
                    String latstr = res.substring(lonindex + 6, res.indexOf("}") - 1);
                    //编码转换
                    String xlat = new String(Base64.decode(latstr, Base64.DEFAULT));
                    String ylon = new String(Base64.decode(lonstr, Base64.DEFAULT));
                    outstr[0] = xlat;
                    outstr[1] = ylon;
                }
            }
        } catch (Exception e) {
            Log.d("get_convert", "erro");
            e.printStackTrace();
        }
        return outstr;
    }

    public String[] GetBaidu(String lat_str, String lon_str) {
        LatLng sourceLatLng = new LatLng(Double.valueOf(lat_str).doubleValue(), Double.valueOf(lon_str).doubleValue());
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.COMMON);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        String[] msg = new String[2];
        msg[0] = Double.toString(desLatLng.latitude);
        msg[1] = Double.toString(desLatLng.longitude);
        return msg;
    }

    public static LatLng gpsToBaidu(double latitude, double logitude) {
        LatLng sourceLatLng = new LatLng(latitude, logitude);
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        return desLatLng;
    }

    public static LatLng baiduToGps(double lat, double lon) {
        LatLng sourceLatLng = new LatLng(lat, lon);
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        LatLng desLatLng = converter.convert();
        LatLng sourcePoint = new LatLng(2 * lat - desLatLng.latitude, 2 * lon - desLatLng.longitude);
        return sourcePoint;
    }

}
