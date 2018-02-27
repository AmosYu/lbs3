package com.ctl.lbs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.ctl.lbs.cell.BtsType;
import com.ctl.lbs.cell.LuceCellInfo;
import com.ctl.lbs.cell.WifiInfo;
import com.ctl.lbs.utils.Gps2BaiDu;
import com.ctl.lbs.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;


public class DbAcessImpl implements DbAccess {

    private static final String TAG = "DbAcessImpl";
    private DetectiveHelper dHelper;

    private static DbAcessImpl dbInstance;

    private SQLiteDatabase dbWrite;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase rizhi = null;

    private DbAcessImpl(Context context) {
        dHelper = DetectiveHelper.getInstance(context);
        dbRead = dHelper.getReadableDatabase();
        dbWrite = dHelper.getWritableDatabase();
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "db" + File.separator, "db.db");
        if (file.exists()) {
            rizhi = openOrCreateDatabase(file, null);
        }
    }

    public static DbAcessImpl getDbInstance(Context context) {
        if (dbInstance == null) {
            dbInstance = new DbAcessImpl(context);
        }
        return dbInstance;
    }

    @Override
    public synchronized void insertCellMapInfo(LuceCellInfo luceCellInfo) {

        long d = dbWrite.insert(BACKUP_DATA, null, getContentValues(luceCellInfo));
        System.out.println(d);

    }

    @Override
    public boolean cellInDbBackup(LuceCellInfo luceCellInfo, boolean isCdma) {
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean result = false;
        if (isCdma) {
            cursor = dbRead.query(BACKUP_DATA, new String[]{SID, NID, BID},
                    SID + "=? and " + NID + "=? and " + BID + "=?",
                    new String[]{String.valueOf(luceCellInfo.getSid()),
                            String.valueOf(luceCellInfo.getNid()),
                            String.valueOf(luceCellInfo.getBid())},
                    null, null, null);
        } else {
            cursor = dbRead.query(BACKUP_DATA, new String[]{LAC, CI},
                    LAC + "=? and " + CI + "=? ",
                    new String[]{String.valueOf(luceCellInfo.getLac()),
                            String.valueOf(luceCellInfo.getCellId())},
                    null, null, null);
        }
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                result = true;
            }
        }
        cursor.close();
        return result;
    }

    @Override
    public void insertWifiInfo(WifiInfo wifiInfo) {
        ContentValues cv = new ContentValues();
        cv.put(MARK,wifiInfo.getMark());
        cv.put(MAC,wifiInfo.getMac());
        cv.put(TYPE,wifiInfo.getType());
        cv.put(RSSI,wifiInfo.getRssi());
        cv.put(LATITUDE,wifiInfo.getLatitude());
        cv.put(LONGITUDE,wifiInfo.getLongitude());
        cv.put(BAIDULATITUDE,wifiInfo.getBaiduLatitude());
        cv.put(BAIDULONGITUDE,wifiInfo.getBaiduLongitude());
        cv.put(RSSI,wifiInfo.getRssi());
        dbWrite.insert(WIFI_DATA,null,cv);

    }

    @Override
    public void deleteCellMapInfo(LuceCellInfo luceCellInfo, boolean isCdma, int type) {

    }

//    @Override
//    public synchronized void updateBackup(LuceCellInfo luceCellInfo, boolean isCdma) {
//
//        if(isCdma) {
//            dbWrite.update(BACKUP_DATA, getContentValues(luceCellInfo),
//                    LAC_SID + "=? and " + CI_NID + "=? and " + BID + "=? and "+RSSI + "<?",
//                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
//                            String.valueOf(luceCellInfo.getCi_nid()),
//                            String.valueOf(luceCellInfo.getBid()),
//                            String.valueOf(luceCellInfo.getRssi())});
//        }
//        else{
//            dbWrite.update(BACKUP_DATA, getContentValues(luceCellInfo),
//                    LAC_SID + "=? and " + CI_NID + "=? and "+RSSI + "<?" ,
//                    new String[]{String.valueOf(luceCellInfo.getLac_sid()),
//                            String.valueOf(luceCellInfo.getCi_nid()),
//                            String.valueOf(luceCellInfo.getRssi())});
//        }
//
//
//    }

    @Override
    public synchronized void insertMark(String mark) {
        Cursor cursor = dbWrite.query(USER_MARK, new String[]{MARK}, MARK + "=?",
                new String[]{mark}, null, null, null);
        if (cursor.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(MARK, mark);
            dbWrite.insert(USER_MARK, null, cv);
        }
        cursor.close();

    }

    @Override
    public ArrayList<String> loadMark() {
        ArrayList<String> markList = new ArrayList<>();
        Cursor cursor = dbRead.query(USER_MARK, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String mark = cursor.getString(cursor.getColumnIndex(MARK));
            markList.add(mark);
        }

        return markList;
    }

    public void upDateMark(String mark, int upload) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MARK, mark);
        contentValues.put("upload", upload);
        dbWrite.update(USER_MARK, contentValues, MARK + "=?", new String[]{mark});
    }

    public ArrayList<String> loadUploadMark() {
        ArrayList<String> markList = new ArrayList<>();
        Cursor cursor = dbRead.query(USER_MARK, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String mark = cursor.getString(cursor.getColumnIndex(MARK));
            int upload = cursor.getInt(cursor.getColumnIndex("upload"));
            if (upload != 1)
                markList.add(mark);
        }
        return markList;
    }


    @Override
    public void deleteAllData() {
//        SQLiteDatabase db = dHelper.getWritableDatabase();
        dbWrite.delete(BACKUP_DATA, null, null);
//        dbW.close();
    }

    @Override
    public int startExportDb(ArrayList<String> markList, boolean isDelete) {
        return 0;
    }

    public synchronized void deleteMarkData(String mark) {

        dbWrite.delete(BACKUP_DATA, MARK + "=?", new String[]{mark});

    }


    public int startExportDb(ArrayList<String> markList, boolean isdelete, Handler handler) {

        SdCardOperate sdCardOperate = new SdCardOperate(getDirPath());

        if (!sdCardOperate.sdCardIsReady()) return -1;

//        SQLiteDatabase db = dHelper.getReadableDatabase();
        int sum = 0;
        for (String mark : markList) {
//            Log.i(TAG, "MARK ----" +mark);
            Cursor cursor = dbRead.query(BACKUP_DATA, null, MARK + "=?",
                    new String[]{mark}, null, null, null, null);
            String fileMsg = getFileHead();
            while (cursor.moveToNext()) {
                String cellType = cursor.getString(cursor.getColumnIndex(CELL_TYPE));
                int lac = cursor.getInt(cursor.getColumnIndex(LAC));
                int ci = cursor.getInt(cursor.getColumnIndex(CI));
                int sid = cursor.getInt(cursor.getColumnIndex(SID));
                int nid = cursor.getInt(cursor.getColumnIndex(NID));
                int bid = cursor.getInt(cursor.getColumnIndex(BID));
                int arfcn = cursor.getInt(cursor.getColumnIndex(ARFCN));
                int pci = cursor.getInt(cursor.getColumnIndex(PCI));
                int rssi = cursor.getInt(cursor.getColumnIndex(RSSI));
                double latitute = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
                double longitute = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
                String time = cursor.getString(cursor.getColumnIndex(TIME));
                String btsType = cursor.getString(cursor.getColumnIndex(BTS_TYPE));

                fileMsg = fileMsg + btsType + "," + cellType + "," + lac + "," + ci + "," + sid + "," + nid + "," + bid + "," + latitute + "," + longitute + "," + arfcn + "," + pci + "," +
                        rssi + "," + time + "\r\n";
                sdCardOperate.writeMsgToFile(fileMsg, mark);
//                Log.i(TAG, "导出的数据：----" +fileMsg);
                sum++;
                if (handler != null) {
                    Message msg = new Message();
                    msg.arg2 = 1000;
                    msg.obj = "成功导出" + sum + "条基站数据";
                    handler.sendMessage(msg);
                }
            }
        }

//        if(isdelete) deleteUserdata(markList);
        Message msg = new Message();
        msg.arg2 = 1000;
        msg.arg1 = 1001;
        msg.obj = "导出完成,导出文件位于本机默认存储空间的基站数据文件夹内。默认存储空间位置见注意事项";
        handler.sendMessage(msg);
        return sum;
    }

    private String getFileHead() {
        return "制式" + "," + "小区类型"+ "," + LAC+","+CI + ","+SID + ","+NID + "," + BID + "," + "纬度" + "," + "经度" + "," + "频点" + "," +
                "扰码" + "," + "场强" + "," + "时间" +"\r\n";
    }

    /**
     * 根据文件名查询所有数据
     *
     * @param filename
     * @return
     */
    public List<Map<String, Object>> selectByFile(String filename) {
        List<Map<String, Object>> list = new ArrayList<>();
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = dbRead.rawQuery("select LAC,CI,SID,NID,BID,CELL_TYPE,latitude,longitude,arfcn,rssi,time,btsType from backupData where mark=?",
                new String[]{filename});
        Log.e("mytag", cursor.getCount() + "");
        while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<>();
            map.put("lac", cursor.getInt(0));
            map.put("ci", cursor.getInt(1));

            map.put("bid", cursor.getInt(4));
            map.put("type", cursor.getString(5));
            map.put("latitude", cursor.getDouble(6));
            map.put("longitude", cursor.getDouble(7));
            map.put("address", "");
            map.put("arfcn", cursor.getInt(8));
            map.put("rssi", cursor.getInt(9));
            map.put("time", cursor.getString(10));
            map.put("zhishi", cursor.getString(11));
            String zhishi = cursor.getString(11);
            if(zhishi.equals("电信C")){
                map.put("lac", cursor.getInt(2));
                map.put("ci", cursor.getInt(3));
            }

            list.add(map);
        }
//        db.close();
        return list;
    }

    public List<Map<String, Object>> selectByFile(String filename, String btsType) {
        List<Map<String, Object>> list = new ArrayList<>();
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = dbRead.rawQuery("select LAC_SID,CI_NID,BID,CELL_TYPE,latitude,longitude,address,arfcn,rssi,time,btsType from backupData where mark=? and btsType=?",
                new String[]{filename, btsType});
        Log.e("mytag", cursor.getCount() + "");
        while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<>();
            map.put("lac_sid", cursor.getInt(0));
            map.put("ci_nid", cursor.getInt(1));
            map.put("bid", cursor.getInt(2));
            map.put("type", cursor.getString(3));
            map.put("latitude", cursor.getDouble(4));
            map.put("longitude", cursor.getDouble(5));
            map.put("address", cursor.getString(6));
            map.put("arfcn", cursor.getInt(7));
            map.put("rssi", cursor.getInt(8));
            map.put("time", cursor.getString(9));
            map.put("zhishi", cursor.getString(10));
            list.add(map);
        }
//        db.close();
        return list;
    }


    /**
     * 根据文件名查询所有数据
     *
     * @param filename
     * @return
     */
    public LinkedList<LatLng> selectByFileGetPoint(String filename) {
        LinkedList<LatLng> list = new LinkedList<>();
        Cursor cursor = dbRead.rawQuery("select LAC,CI,BID,CELL_TYPE,latitude,longitude,arfcn,rssi,time,btsType,baiduLatitude,baiduLongitude from backupData where mark=?",
                new String[]{filename});
        Log.e("mytag", cursor.getCount() + "");
        while (cursor.moveToNext()) {
            LatLng latLng = new LatLng(cursor.getDouble(10), cursor.getDouble(11));//Gps2BaiDu.gpsToBaidu(cursor.getDouble(4),cursor.getDouble(5));
            list.add(latLng);
        }
        return list;
    }


    /**
     * 根据文件名和类型进行查询
     *
     * @param filename
     * @param type
     * @return
     */
    public LinkedList<LuceCellInfo> selectByNameAndType(String filename, String type) {
        LinkedList<LuceCellInfo> list = new LinkedList<>();
//        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = dbRead.rawQuery("select LAC,CI,SID,NID,BID,CELL_TYPE,latitude,longitude,arfcn,PCI,rssi,time,btsType from backupData where mark=? AND btsType=?",
                new String[]{filename, type});
        while (cursor.moveToNext()) {
            LuceCellInfo luceCellInfo = new LuceCellInfo();
            luceCellInfo.setLac(cursor.getInt(0));
            luceCellInfo.setCellId(cursor.getInt(1));
            luceCellInfo.setSid(cursor.getInt(2));
            luceCellInfo.setNid(cursor.getInt(3));

            luceCellInfo.setBid(cursor.getInt(4));
            luceCellInfo.setCellType(cursor.getString(5));
            luceCellInfo.setLatitudeGps(cursor.getDouble(6));
            luceCellInfo.setLongitudeGps(cursor.getDouble(7));

            luceCellInfo.setArfcnA(cursor.getInt(8));
            luceCellInfo.setArfcnB(cursor.getInt(9));

            luceCellInfo.setRssi(cursor.getInt(10));
            luceCellInfo.setBtsType(BtsType.strToBtsType(cursor.getString(12)));

            list.add(luceCellInfo);
        }

        return list;
    }

    //从手机文件中查询数据
    @Override
    public ArrayList<String> FindPos(String mnc, String lac, String cellid) {
        ArrayList<String> list = new ArrayList<>();
        if (rizhi!=null) {
            Cursor cursor = rizhi.rawQuery("select lat,lon from cellinfo where mnc =? and lac =? and ci =? ",new String[]{mnc, lac, cellid});
            while (cursor.moveToNext()) {
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                list.add(latitude + "");
                list.add(longitude + "");
            }
        }
        return list;
    }


    public ArrayList<LatLng> findPos(BtsType btsType, String lac, String cellid) {
        String mnc = "00";
        switch (btsType){
            case GSM_MOBILE:
                mnc = "00";
                break;
            case GSM_UNICOM:
                mnc = "01";
                break;
            case LTE_MOBILE:
                mnc = "00";
                break;
            case LTE_UNICOM:
                mnc = "01";
                break;
            case LTE_TELECOM:
                mnc = "11";
                break;
            case WCDMA:
                mnc = "01";
                break;
            case TDSCDMA:
                mnc = "00";
                break;
            case CDMA:
                mnc = "03";
                break;
        }

        ArrayList<LatLng> list = new ArrayList<>();
        if (rizhi!=null) {
            Cursor cursor = rizhi.rawQuery("select lat,lon from cellinfo where mnc =? and lac =? and ci =? ",new String[]{mnc, lac, cellid});
            while (cursor.moveToNext()) {
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                LatLng latLng = Gps2BaiDu.gpsToBaidu(latitude,longitude);
                list.add(latLng);
            }
        }
        return list;
    }

    /**
     * 查询本地数据库
     *
     *
     * @return
     */
    public List<LuceCellInfo> findBtsUseId(String lac, String ci) {
        List<LuceCellInfo> list = new ArrayList<>();

        Cursor cursor = dbRead.rawQuery("select LAC,CI,CELL_TYPE,latitude,longitude,arfcn,PCI,rssi,time,btsType from backupData where LAC=? AND CI=?",
                new String[]{lac, ci});
        while (cursor.moveToNext()) {
            LuceCellInfo lci = new LuceCellInfo();
            lci.setLac(cursor.getInt(0));
            lci.setCellId(cursor.getInt(1));
            lci.setCellType(cursor.getString(2));
            lci.setLatitudeGps(cursor.getDouble(3));
            lci.setLongitudeGps(cursor.getDouble(4));
            lci.setArfcnA(cursor.getInt(5));
            lci.setArfcnB(cursor.getInt(6));
            lci.setRssi(cursor.getInt(7));
            lci.setTime(String.valueOf(cursor.getInt(8)));
            lci.setBtsType(BtsType.strToBtsType(cursor.getString(9)));
            list.add(lci);
        }
        return list;
    }

    /**
     * 查询本地数据库
     *
     * @param lac
     * @param type
     * @return
     */
    public List<LuceCellInfo> findOnlySameLacBts(String lac, String type, double latitude, double longitude, String mark) {
        List<LuceCellInfo> list = new ArrayList<>();
        String cellType = type.toString();
        Cursor cursor = dbRead.rawQuery("select LAC,CI,CELL_TYPE,latitude,longitude,arfcn,PCI,rssi,time,btsType from backupData where LAC=? AND btsType=? AND latitude=? AND longitude=? AND mark=?",
                new String[]{lac, type, String.valueOf(latitude), String.valueOf(longitude), mark});
        while (cursor.moveToNext()) {
            LuceCellInfo lci = new LuceCellInfo();
            lci.setLac(cursor.getInt(0));
            lci.setCellId(cursor.getInt(1));
            lci.setCellType(cursor.getString(2));
            lci.setLatitudeGps(cursor.getDouble(3));
            lci.setLongitudeGps(cursor.getDouble(4));

            lci.setArfcnA(cursor.getInt(5));
            lci.setArfcnB(cursor.getInt(6));
            lci.setRssi(cursor.getInt(7));
            lci.setTime(cursor.getString(8));
            lci.setBtsType(BtsType.strToBtsType(cursor.getString(9)));
            list.add(lci);
        }
        return list;
    }


    /**
     * 查询本地数据库
     *
     *
     * @return
     */
    public List<LuceCellInfo> findBtsUseId(String sid, String nid, String bid) {
        List<LuceCellInfo> list = new ArrayList<>();
        String cellType = BtsType.CDMA.toString();
        Cursor cursor = dbRead.rawQuery("select SID,NID,BID,latitude,longitude,arfcn,PCI,rssi,time,btsType from backupData where SID=? AND NID=? BID=? ",
                new String[]{sid, nid, bid});
        while (cursor.moveToNext()) {
            LuceCellInfo lci = new LuceCellInfo();
            lci.setSid(cursor.getInt(0));
            lci.setNid(cursor.getInt(1));
            lci.setBid(cursor.getInt(2));
            lci.setLatitudeGps(cursor.getDouble(3));
            lci.setLongitudeGps(cursor.getDouble(4));

            lci.setArfcnA(cursor.getInt(5));
            lci.setArfcnB(cursor.getInt(6));

            lci.setRssi(cursor.getInt(7));
            lci.setTime(cursor.getString(8));
            lci.setBtsType(BtsType.strToBtsType(cursor.getString(9)));
            list.add(lci);
        }
        return list;
    }


    private ContentValues getContentValues(LuceCellInfo luceCellInfo) {
        ContentValues insertData = new ContentValues();
        insertData.put(CELL_TYPE, luceCellInfo.getCellType());
        insertData.put(LAC, luceCellInfo.getLac());
        insertData.put(CI, luceCellInfo.getCellId());
        insertData.put(BID, luceCellInfo.getBid());
        insertData.put(SID, luceCellInfo.getSid());
        insertData.put(NID, luceCellInfo.getNid());
        insertData.put(LATITUDE, luceCellInfo.getLatitudeGps());
        insertData.put(LONGITUDE, luceCellInfo.getLongitudeGps());
        insertData.put(PCI, luceCellInfo.getArfcnB());
        insertData.put(RSSI, luceCellInfo.getRssi());
        insertData.put(ARFCN, luceCellInfo.getArfcnA());
        insertData.put(TIME, luceCellInfo.getTime());
        insertData.put(MARK, luceCellInfo.getMark());
        insertData.put(BTS_TYPE, luceCellInfo.getBtsType().toString());
        insertData.put(BAIDULATITUDE, luceCellInfo.getBaiduLatitude());
        insertData.put(BAIDULONGITUDE, luceCellInfo.getBaiduLongitude());
        return insertData;
    }

    public static String getDirPath() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "基站数据";
    }

    public void closeDb() {
        if (dbRead != null) dbRead.close();
        if (dbWrite != null) dbWrite.close();
    }

    public LinkedList<WifiInfo> selectWifiDb(String filename) {
        LinkedList<WifiInfo> list = new LinkedList<>();
        Cursor cursor = dbRead.rawQuery("select MAC,TYPE,RSSI,latitude,longitude from wifiData where mark=?",
                new String[]{filename});
        while (cursor.moveToNext()) {
            WifiInfo wifi = new WifiInfo();
            wifi.setMac(cursor.getString(0));
            wifi.setType(cursor.getString(1));
            wifi.setRssi(cursor.getString(2));
            wifi.setLatitude(cursor.getDouble(3));
            wifi.setLongitude(cursor.getDouble(4));
            list.add(wifi);
        }
        return list;
    }
}

