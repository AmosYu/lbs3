package com.ctl.lbs.database;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by yu on 2016/9/21.
 * 类功能操作SD卡的文件读写
 */
public class SdCardOperate {

    private static final String TAG = "SdCardOperate";



    private String dirPath;

    /**
     * 构造函数
     * @param dirPath 文件路径+名称
     */
    public SdCardOperate(String dirPath) {
        this.dirPath = dirPath;
        File file  = new File(dirPath);
        if(!file.exists()){
            file.mkdir();
        }
    }

    /**
     * 检查SD卡是否已经准备好
     * @return
     */
    public boolean sdCardIsReady() {
        String sdStatus = Environment.getExternalStorageState();
        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SD 未准备好");
            return false;
        }
        return true;
    }

    /**
     * 检查目标文件是否文在,不存在创建
     * @return true 存在，false 不存在 已创建
     */
//    public boolean createFile(){
//
//        try {
//            if( !file.exists()) {
//                Log.d(TAG, "文件不存在创建文件");
//                file.createNewFile();
//                return true;
//            }
//        } catch(Exception e) {
//
//            e.printStackTrace();
//
//        }
//        return false;
//    }


    /**
     * 向文件中写入数据
     * @param msg 写入的内容
     * @param filename 文件名
     */
    public void writeMsgToFile(String msg, String filename) {

        String filePath = dirPath+ File.separator+filename+".csv";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath),true);
//            byte[] buf = msg.getBytes();
            OutputStreamWriter fos = new OutputStreamWriter(fileOutputStream, "GBK");
            fos.write(msg);
            fos.close();

        } catch(Exception e) {
            Log.e(TAG, "数据写入错误");
            e.printStackTrace();
        }
    }
}
