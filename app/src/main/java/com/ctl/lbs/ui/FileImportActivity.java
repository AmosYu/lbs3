package com.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ctl.lbs.DataUpload;
import com.ctl.lbs.R;
import com.ctl.lbs.cell.LuceCellInfo;
import com.ctl.lbs.database.DbAcessImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;




/**
 * 文件导入，文件导出，文件上传全部使用此界面，区分功能
 * 使用intent.getStringExtra("类型")
 * 文件导入功能：导入
 * 文件导出功能：导出
 * 文件上传功能：上传
 */
public class FileImportActivity extends Activity {
    private final String CHECK = "CHECK";
    private final String DIR_NAME ="DIR_NAME";

    private SimpleAdapter fileNameAdapter;
    private SimpleAdapter logAdapter;
    private ArrayList<HashMap<String,Object>> dirNameList;
//    private ReadExterSdFileSdFile readExterSdFile;
    private ListView dirListView,logListView;
    private CheckBox allCheckBox;
    private Button actionBtn;
    private Context context;
    /**
     * 无论导入、导出、上传都需要选择文件或目录，此列表存储已选择的文件或目录名称
     */
    private ArrayList<String> selectDirNameList = new ArrayList<>();

    //点击action按钮后弹出对话框title是对话框标题
    private String title = "";
    //点击action按钮后弹出对话框leftBtnStr是对话框左按钮的功能描述
    private String leftBtnStr = "";
    //点击action按钮后弹出对话框rightBtnStr是对话框右按钮的功能描述
    private String rightBtnStr= "";

    /**
     * 进度提示列表的标题
     */
    private TextView logTvTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_file_import);
        MyApplication.getInstance().addActivity(this);
        context = this;
        dirNameList = new ArrayList<>();
        Intent intent= getIntent();
        final String type = intent.getStringExtra("类型");
        allCheckBox = (CheckBox)findViewById(R.id.dir_allcheck);
        actionBtn = (Button)findViewById(R.id.file_action_btn);
        logTvTitle = (TextView)findViewById(R.id.file_log_title);
        if(type.equals("导入")){
           initImport();
        }
        else if(type.equals("导出")){
            initExport();
        }
        else if(type.equals("上传")){
            initUpload();
        }


        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.equals("数据上传")&&!MyApplication.isNetworkConnected(context)){
                    MyApplication.toastTip(context,"当前无网络，不能上传", Toast.LENGTH_LONG);
                    return;
                }
                if(selectDirNameList.size()==0){
                    MyApplication.toastTip(context,"至少选择一个文件或目录", Toast.LENGTH_SHORT);
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final EditText input = new EditText(context);
                final String userMark = LuceCellInfo.getCurrentTime();

                LayoutInflater netAddress = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final LinearLayout layout = (LinearLayout) netAddress.inflate(R.layout.ip_port, null);
                final EditText ipEdit = (EditText) layout.findViewById(R.id.sever_ip_edit);
                final EditText portEdit = (EditText) layout.findViewById(R.id.sever_port_edit);
                SharedPreferences sharedPreferences= context.getSharedPreferences("AIRINTERFACE", Activity.MODE_PRIVATE);
                String ipB = sharedPreferences.getString("IP","192.168.1.105");
                String portB = sharedPreferences.getString("PORT","80");
                ipEdit.setText(ipB);
                portEdit.setText(portB);
                builder.setTitle(title)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setNegativeButton(leftBtnStr, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                                if(title.equals("数据导入")){
                                    String lastMark = (input.getText().toString()).replaceAll(" ","");
                                    if(lastMark==null||lastMark.length()==0){
                                        lastMark = userMark;
                                    }
                                    dbAcess.insertMark(lastMark);
//                                    readExterSdFile.startImport(lastMark,selectDirNameList,true,handler);
                                    actionBtn.setEnabled(false);
                                }
                                else if(title.equals("数据导出")){
                                    dbAcess.startExportDb(selectDirNameList,false,handler);
                                    actionBtn.setEnabled(false);
                                }
                                else if(title.equals("数据上传")){
                                    String ipN = ipEdit.getText().toString();
                                    String portN = portEdit.getText().toString();
                                    if(MyApplication.isIPAddress(ipN)&&portN.length()>0){
                                        SharedPreferences sharedPreferences= getSharedPreferences("AIRINTERFACE", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("IP",ipN);
                                        editor.putString("PORT",portN);
                                        editor.commit();

                                        DataUpload dataUpload = new DataUpload(context,selectDirNameList,handler);
                                        dataUpload.start();
                                        actionBtn.setEnabled(false);
                                    }
                                }

                            }
                        });
                builder.setPositiveButton(rightBtnStr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });
                if(title.equals("数据导入")){
                    input.setText(userMark);
                    builder.setView(input);
                    input.setText(LuceCellInfo.getCurrentTime());
                    builder.setMessage("请在下框中输入文件名，数据导入后将被存于此文件下,文件名只能包含是中文、字母、数字。\r\n文件名默认为当前时间");
                }else if(title.equals("数据上传")){
                    builder.setView(layout);
                }
                builder.show();
            }
        });

        dirListView = (ListView)findViewById(R.id.dir_listview);
        fileNameAdapter = new SimpleAdapter(this,dirNameList,R.layout.dir_list_item,
                new String[]{CHECK,DIR_NAME},new int[]{R.id.dir_check,R.id.dir_name});
        dirListView.setAdapter(fileNameAdapter);
        dirListView.setOnItemClickListener(dirItemLis);

        /**
         * 全选按钮
         */
        allCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectDirNameList.clear();
                for(int i=0;i<dirNameList.size();i++){
                    HashMap<String,Object> map = dirNameList.get(i);
                    if(isChecked){
                        map.put(CHECK,true);
                    }
                    else {
                        map.put(CHECK,false);
                    }
                    dirNameList.set(i,map);
                    fileNameAdapter.notifyDataSetChanged();
                    selectDir(map.get(DIR_NAME).toString(),isChecked);
                }
            }
        });
        logList= new ArrayList<>();
        logListView = (ListView)findViewById(R.id.file_log_listview);
        logAdapter = new SimpleAdapter(this,logList,R.layout.file_log_item,
                new String[]{DIR_NAME},new int[]{R.id.file_log_list});
        logListView.setAdapter(logAdapter);

    }

    /**
     * 文件或目录列表的点击监听
     */
    AdapterView.OnItemClickListener dirItemLis = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String,Object> map = dirNameList.get((int) id);
            boolean select = !((boolean)map.get(CHECK));
            map.put(CHECK,select);
            fileNameAdapter.notifyDataSetChanged();
            selectDir(map.get(DIR_NAME).toString(),select);
        }
    };


    private void selectDir(String dirName, boolean select){
        if(select){
            for(String name:selectDirNameList){
                if(name.equals(dirName)){
                    return;
                }
            }
            selectDirNameList.add(dirName);
        }else {
            for(int i=0;i<selectDirNameList.size();i++){
                if(dirName.equals(selectDirNameList.get(i))){
                    selectDirNameList.remove(i);
                    return;
                }
            }
        }
    }
    private ArrayList<HashMap<String,Object>> logList;
    /**
     * 数据导出 arg2 == 1000  档arg1==1001时表示导出完成
     * 数据导入 arg2 == 2000  档arg1==2001时表示导入完成
     * 数据上传 arg2 == 3000  档arg1==3001时表示上传完成或因其他问题导致上传终止
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap<String,Object> map = new HashMap<>();
            map.put(DIR_NAME,(String)msg.obj);
            logList.add(map);
            if(logList.size()>8) logList.remove(0);
            logAdapter.notifyDataSetChanged();
            if(msg.arg2 == 1000&&msg.arg1==1001){
                initExport();
                actionBtn.setEnabled(true);
                fileNameAdapter.notifyDataSetChanged();
            }
            else if(msg.arg2 == 2000&&msg.arg1==2001){
                actionBtn.setEnabled(true);
                initImport();
                fileNameAdapter.notifyDataSetChanged();
            }
            else if(msg.arg2 == 3000&&msg.arg1==3001){
                initUpload();
                actionBtn.setEnabled(true);
                fileNameAdapter.notifyDataSetChanged();
            }
        }
    };

    private void initUpload(){
        title = "数据上传";
        leftBtnStr = "开始上传";
        rightBtnStr = "取消";
        actionBtn.setText("开始上传");
        dirNameList.clear();
        DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
        ArrayList<String> markList = dbAcess.loadUploadMark();
        markList.add("test");
        for(String mark:markList){
            HashMap<String,Object> map = new HashMap<>();
            map.put(CHECK,false);
            map.put(DIR_NAME,mark);
            dirNameList.add(map);
        }
        allCheckBox.setText("本机所有未上传的文件");
        logTvTitle.setText("数据上传进度");
    }

    private void initImport(){
//        logTvTitle.setText("数据导入进度");
//        title = "数据导入";
//        actionBtn.setText("开始导入");
//        leftBtnStr = "开始导入";
//        rightBtnStr = "取消";
//        dirNameList.clear();
//        readExterSdFile = new ReadExterSdFile(this);
//        readExterSdFile.getDirList();
//        for(File filedir:readExterSdFile.getDirList()){
//            HashMap<String,Object> map = new HashMap<>();
//            map.put(CHECK,false);
//            map.put(DIR_NAME,filedir.getName());
//            dirNameList.add(map);
//        }
//        allCheckBox.setText("SD卡数据文件夹");
    }

    private void initExport(){
        logTvTitle.setText("数据导出进度");
        title = "数据导出";
        actionBtn.setText("开始导出");
        leftBtnStr = "开始";
        rightBtnStr = "取消";
        dirNameList.clear();
        DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
        ArrayList<String> markList = dbAcess.loadMark();
        markList.add("test");
        for(String mark:markList){
            HashMap<String,Object> map = new HashMap<>();
            map.put(CHECK,false);
            map.put(DIR_NAME,mark);
            dirNameList.add(map);
        }
        allCheckBox.setText("本机所有文件");
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
