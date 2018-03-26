package com.ctl.lbs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by yuxingyu on 16/5/23.
 */
public class DetectiveHelper extends SQLiteOpenHelper {

    public static final String DB_NAME =  SDBHelper.DB_DIR + File.separator + "LUCE.db";

    private static final int VERSION = 1;

    private  static DetectiveHelper detectiveHelper = null;

    private  DetectiveHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DetectiveHelper getInstance(Context context){
        if(detectiveHelper==null){
            detectiveHelper = new DetectiveHelper(context);
        }
        return detectiveHelper;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table userMark(mark text,upload Integer,latitude Double,longitude Double)");
        db.execSQL("create table wifiData(mark text,MAC text,TYPE,text,RSSI Integer,latitude Double,longitude Double,baiduLatitude Double,baiduLongitude Double)");
        db.execSQL("create table backupData(" +
                "LAC Integer," +
                "SID Integer," +
                "CI Integer," +
                "NID Integer," +
                "BID Integer," +
                "PCI Integer," +
                "latitude Double," +
                "longitude Double," +
                "arfcn Integer," +
                "rssi Integer," +
                "time text," +
                "btsType text," +
                "mark text,"+
                "CELL_TYPE text,"+
                "baiduLatitude Double,"+
                "baiduLongitude Double"+")");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS backup");
        onCreate(db);
    }
}
