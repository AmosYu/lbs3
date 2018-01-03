
package com.ctl.lbs.utils;

/**
 *
 * @author Administrator
 */
public class GPSUtils {

    static double pi = 3.14159265358979324;
    static double a = 6378245.0;

    static double ee = 0.00669342162296594323;

    public final static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;


       private static double[] delta (double lat,double  lon) {

        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        return new double[]{dLat, dLon};
    }

    public static double[] wgs2bd(double lat, double lon) {
        double[] wgs2gcj = wgs2gcj(lat, lon);
        double[] gcj2bd = gcj2bd(wgs2gcj[0], wgs2gcj[1]);
        return gcj2bd;
    }

    public static double[] gcj2bd(double lat, double lon) {
        double x = lon, y = lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[]{bd_lat, bd_lon};
    }
    private static boolean outOfChina(double gcjLat, double gcjLon){

        if (gcjLon < 72.004 || gcjLon > 137.8347)
            return true;
        if (gcjLat < 0.8293 || gcjLat > 55.8271)
            return true;
        return false;

    }
    private static double[] gcj2wgs_(double gcjLat, double gcjLon){

        if (outOfChina(gcjLat, gcjLon))
            return new double[]{gcjLat, gcjLon};

        double[] delta = delta(gcjLat, gcjLon);
        return new double[]{gcjLat - delta[0],gcjLon - delta[1]};
    }

    public static double[] gcj2wgs(double gcjLat, double gcjLon) {
        double initDelta = 0.01;
        double threshold = 0.000000001;
        double dLat = initDelta, dLon = initDelta;
        double mLat = gcjLat - dLat, mLon = gcjLon - dLon;
        double pLat = gcjLat + dLat, pLon = gcjLon + dLon;
        double wgsLat, wgsLon, i = 0;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLon = (mLon + pLon) / 2;
            double[] gcj2wgs_ = gcj2wgs_(wgsLat, wgsLon);
            dLat = gcj2wgs_[0] - gcjLat;
            dLon = gcj2wgs_[1] - gcjLon;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold))
                break;

            if (dLat > 0) pLat = wgsLat; else mLat = wgsLat;
            if (dLon > 0) pLon = wgsLon; else mLon = wgsLon;

            if (++i > 10000) break;
        }
        //console.log(i);
        return new double[]{wgsLat, wgsLon};
    }

    public static double[] bd2gcj(double lat, double lon) {
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[]{gg_lat, gg_lon};
    }

    public static double[] wgs2gcj(double lat, double lon) {
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        double[] loc = {mgLat, mgLon};
        return loc;
    }

    private static double transformLat(double lat, double lon) {
        double ret = -100.0 + 2.0 * lat + 3.0 * lon + 0.2 * lon * lon + 0.1 * lat * lon + 0.2 * Math.sqrt(Math.abs(lat));
        ret += (20.0 * Math.sin(6.0 * lat * pi) + 20.0 * Math.sin(2.0 * lat * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lon * pi) + 40.0 * Math.sin(lon / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lon / 12.0 * pi) + 320 * Math.sin(lon * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double lat, double lon) {
        double ret = 300.0 + lat + 2.0 * lon + 0.1 * lat * lat + 0.1 * lat * lon + 0.1 * Math.sqrt(Math.abs(lat));
        ret += (20.0 * Math.sin(6.0 * lat * pi) + 20.0 * Math.sin(2.0 * lat * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * pi) + 40.0 * Math.sin(lat / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lat / 12.0 * pi) + 300.0 * Math.sin(lat / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }


    public static void main(String[] args){
        double[] wgs2bd = wgs2bd(40.016937,116.356179);
        System.out.println(wgs2bd[1]+","+wgs2bd[0]);


    }

}
