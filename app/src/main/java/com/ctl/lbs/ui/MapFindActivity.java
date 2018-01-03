package com.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.ctl.lbs.BaiduMap.BaiduMapUtil;
import com.ctl.lbs.BaiduMap.ConvexHull;
import com.ctl.lbs.BaiduMap.LocationService;
import com.ctl.lbs.R;
import com.ctl.lbs.WebServiceConn;
import com.ctl.lbs.cell.BtsType;
import com.ctl.lbs.cell.LuceCellInfo;
import com.ctl.lbs.database.DbAcessImpl;
import com.ctl.lbs.utils.AlrDialog_Show;
import com.ctl.lbs.utils.GPSUtils;
import com.ctl.lbs.utils.LocationPoint;
import com.ctl.lbs.utils.NumCheck;
import com.ctl.lbs.utils.Point;
import com.ctl.lbs.utils.dataCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MapFindActivity extends Activity {
    private Context context;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private LocationService locationService=null;
    private LocationPoint point=null;

    private Spinner mncSpinner;
    private String[] modes={"中国移动","中国联通","中国电信2G","中国电信4G"};
    private ArrayAdapter mncAdapter;

    private TextView lac_text;
    private TextView cell_text;
    private LinearLayout bid_liner;
    /**
     * 查询按钮
     */
    private Button qure_Btn;

    private Button daohang_Btn;
    private Button clear_Btn;
    private Button pos_Btn;
    private ListView listView;
    private EditText lac_edit;
    private EditText cell_edit;
    private EditText bid_edit;
    private CheckBox hex_mode;

    private PopupWindow mPopWindow;

    int mode_index=0;
    String mcc="460";
    static String mnc="00";
    int mode=0;//网络类型 0移动 1联通 2电信
    int net=0;//制式 0:2g 、1:3g 、2:4g

    String lac="";
    static String cellid="";
    String sid="";
    String nid="";
    String bid="";
    public static boolean hex_flg = false;//十六进制

    WebServiceConn conn;
    BaiduMapUtil baiduMapUtil;
    public static List<OverlayOptions> showList=new ArrayList<>();
    private int[] color={0xffff0000,0xff00ff00,0xff0000ff,0xffffff00,0xff00ffff,0xffff00ff};
//    private List<LuceCellInfo> luceCellInfos=new ArrayList<>();
    private LatLng end_latLng=null;
    private DbAcessImpl impl=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapfind_layout);

        MyApplication.getInstance().addActivity(this);
        context=this;
        conn=new WebServiceConn(context,0);
        Intent intent=getIntent();
        point = (LocationPoint) intent.getSerializableExtra("point");
        impl=DbAcessImpl.getDbInstance(context);

        initView();
        initMap();
    }

    private void initMap() {
        mBaiduMap = mMapView.getMap();
        baiduMapUtil=new BaiduMapUtil(context,mBaiduMap);
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Button button = new Button(context);
                button.setBackgroundResource(R.drawable.popup);
                button.setTextColor(Color.BLACK);
                final LatLng ll = marker.getPosition();
                Bundle bundle=marker.getExtraInfo();
                final String info= (String) bundle.getSerializable("info");
                button.setText(info);
                InfoWindow mInfoWindow = new InfoWindow(button, ll, -47);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }
    private void initView() {
        mMapView=(MapView) findViewById(R.id.find_bmapView);
        initModeSpinner();
        lac_text=(TextView)findViewById(R.id.lac_text_id);
        cell_text=(TextView)findViewById(R.id.cell_text_id);
        lac_edit = (EditText)findViewById(R.id.lac_str);
        cell_edit = (EditText)findViewById(R.id.cellid_str);
        bid_edit = (EditText)findViewById(R.id.Bid_str);
        bid_liner=(LinearLayout)findViewById(R.id.bid_liner);
//        listView = (ListView) findViewById(R.id.cell_list);
//        lacDataAdapter=new LacDataAdapter(context,luceCellInfos,R.layout.cell_listview_item);
//        listView.setAdapter(lacDataAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });

        hex_mode = (CheckBox)findViewById(R.id.Hex);
        hex_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                {
                    //editext只显示数字和小数点
                    lac_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    cell_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    bid_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else
                {
                    //editext显示文本
                    lac_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    cell_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                    bid_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }
        });
        qure_Btn = (Button) findViewById(R.id.qure_btn);
        qure_Btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    //基站查询
                    boolean start_flg=false;
                    start_flg=GetRequestParam();
                    if(start_flg==true)
                    {
                        new Thread(new Runnable() {
                            public void run() {
                                Request_Gps(0);
                            }
                        }).start();
                    }
            }
        });
        qure_Btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //长按查询本地数据
                boolean start_flg=false;
                start_flg=GetRequestParam();
                if(start_flg==true)
                {
                    new Thread(new Runnable() {
                        public void run() {
                            Request_Gps(1);
                        }
                    }).start();
                }
                return true;
            }
        });

        daohang_Btn=(Button)findViewById(R.id.daohang_btn);
        daohang_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng star=new LatLng(point.getLat(),point.getLon());
                baiduMapUtil.launchNavigator(point.getAddress(),star,end_latLng);
            }
        });

        clear_Btn = (Button) findViewById(R.id.clear_btn);
        clear_Btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mBaiduMap!=null)
                {
                    mBaiduMap.clear();
                    showList.clear();
//                    luceCellInfos.clear();
//                    lacDataAdapter.notifyDataSetChanged();
                }
            }
        });

        //当前位置
        pos_Btn = (Button) findViewById(R.id.pos_btn);
        pos_Btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try {
                    baiduMapUtil.addMarker(point.getLat(),point.getLon(),point.getAddress(),255f,0f,0f,1f);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    private void initModeSpinner(){
        mncSpinner = (Spinner)findViewById(R.id.set_mnc_mode);
        mncAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,modes);
        //设置下拉列表的风格
        mncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        mncSpinner.setAdapter(mncAdapter);
        mncSpinner.setSelection(0,true);
        //添加事件Spinner事件监听
        mncSpinner.setOnItemSelectedListener(dModeSelectLis);
    }
    private AdapterView.OnItemSelectedListener dModeSelectLis = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // btsCtrl.setDetectMode(position);
            mode_index=position;
            if(mode_index==2)
            {
                lac_text.setText("SID");
                cell_text.setText("NID");
                bid_liner.setVisibility(View.VISIBLE);
            }
            else
            {
                lac_text.setText("LAC");
                cell_text.setText("CELL");
                bid_liner.setVisibility(View.GONE);

            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    String[] parameters=new String[3];
    private void Request_Gps(int s)//查询经纬度
    {
        int flag_int=s;
//        String[] parameters=new String[3];
        String[] get_msg=new String[2];
        get_msg[0]="";
        get_msg[1]="";
        LuceCellInfo luceCellInfo=new LuceCellInfo();
        for(int i=0;i<3;i++)
        {
            parameters[i]="";
        }
        if(mode== dataCode.cdma)
        {
            parameters[0]=sid;
            parameters[1]=bid;
            parameters[2]=nid;
            luceCellInfo.setSid(Integer.valueOf(sid));
            luceCellInfo.setNid(Integer.valueOf(nid));
            luceCellInfo.setBid(Integer.valueOf(bid));
        }
        else
        {
            parameters[0]=lac;
            parameters[1]=cellid;
            luceCellInfo.setLac(Integer.valueOf(lac));
            luceCellInfo.setCellId(Integer.valueOf(cellid));
        }
        Local_Qur(luceCellInfo,flag_int);
    }
    private void Local_Qur(LuceCellInfo luceCellInfo,int flag)
    {
        if (flag==0){//单击
            //查询后台服务器
            new FindPos(luceCellInfo).execute();
        }else if (flag==1){//长按
                    findDbImpl();
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1==1){
                Toast.makeText(context, "本地没有数据，请先采集数据后再进行查询", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private void findDbImpl() {
        List<LuceCellInfo> list_luces=new ArrayList<>();
        List<Integer> list_rssi=new ArrayList<>();
        List<Point> list_point=new ArrayList<>();
        List<LatLng> latLngs=new ArrayList<>();
        if (mode==dataCode.cdma){
            list_luces=impl.findBtsUseId(lac,cellid,bid);
        }else {
            list_luces.addAll(impl.findBtsUseId(lac,cellid));
//            list_luces.addAll(impl.findBtsUseId(lac,cellid, BtsType.TDSCDMA));
//            list_luces.addAll(impl.findBtsUseId(lac,cellid, BtsType.LTE));
        }
// else if (mode==dataCode.china_unicom){
//            list_luces.addAll(impl.findBtsUseId(lac,cellid,CellType.GSM_U));
//            list_luces.addAll(impl.findBtsUseId(lac,cellid,CellType.WCDMA));
//        }
        if (list_luces.size()>0){
            for (int i=0;i<list_luces.size();i++){
                list_rssi.add(list_luces.get(i).getRssi());
            }
            for (int i=0;i<list_luces.size();i++){
                Point point=new Point();
                point.setLat(list_luces.get(i).getLatitudeGps()+"");
                point.setLon(list_luces.get(i).getLongitudeGps()+"");
//                point.setAddr(list_luces.get(i).getAddress()+"");
                point.setRssi(list_luces.get(i).getRssi()+"");
                list_point.add(point);
            }
            int max_rssi=0;
            int min_rssi=0;
            //按强度渐变添加覆盖物
            if (list_rssi.size()>0){
                max_rssi= Integer.valueOf(list_rssi.get(0));
                min_rssi= Integer.valueOf(list_rssi.get(0));
                for (int i=0;i<list_rssi.size()-1;i++){
                    int current= Integer.valueOf(list_rssi.get(i));
                    if (current>max_rssi){
                        max_rssi=current;
                    } else if (current<min_rssi){
                        min_rssi=current;
                    }
                }
            }
//            List<Map<String,String>> list_num=new ArrayList<>();
//            for (int i=0;i<list_luces.size();i++){
//                LuceCellInfo cellInfo=list_luces.get(i);
//                String lat_lon=cellInfo.getLatitude()+","+cellInfo.getLongitude();
//                if (list_num==null||list_num.size()==0){
//
//                }
//            }
            for (int i=0;i<list_luces.size();i++){
                int rssi_rgb=(Integer.valueOf(list_luces.get(i).getRssi())-min_rssi)*100/(max_rssi-min_rssi);
                final double[] latlon1= GPSUtils.wgs2bd(Double.valueOf(list_luces.get(i).getLatitudeGps()), Double.valueOf(list_luces.get(i).getLongitudeGps()));
                if (!((int)latlon1[0]==0&&(int)latlon1[1]==0)){
                    baiduMapUtil.addMarker(latlon1[0],latlon1[1],"基站信息："+parameters[0]+","+parameters[1]+","+parameters[2]+"\n"
                            + Double.valueOf(list_luces.get(i).getRssi())+"",(100-rssi_rgb)*255/100 ,rssi_rgb*255/100,0f,1.0f);
                }
            }
            for (int i=0;i<list_point.size();i++){
                Point point= list_point.get(i);
                double lat= Double.valueOf(point.getLat());
                double lon= Double.valueOf(point.getLon());
                if ((int)lat==0&&(int)lon==0){
                    list_point.remove(point);
                    i--;
                }
            }
            //垃框查询
            ConvexHull convexHull=new ConvexHull(list_point);
            List<Point> list_po=convexHull.calculateHull();
            if (list_po.size()>=3){
                for (int i=0;i<list_po.size();i++){
                    Point point=list_po.get(i);
                    final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(point.getLat()), Double.valueOf(point.getLon()));
                    LatLng latLng=new LatLng(latlon[0],latlon[1]);
                    latLngs.add(latLng);
                }
                if (showList.size()==0){
                    baiduMapUtil.draw_find(latLngs);
                }else if (showList.size()==1||showList.size()>1){
                    OverlayOptions polygonOption=null;
                    if (showList.size()<7){
                        polygonOption = new PolygonOptions()
                                .points(latLngs)
                                .stroke(new Stroke(5, color[showList.size()-1]))//color[showList.size()-1]
                                .fillColor(0x80ffffff);
                    }else {
                        polygonOption = new PolygonOptions()
                                .points(latLngs)
                                .stroke(new Stroke(5, color[showList.size()-7]))//color[showList.size()-7]
                                .fillColor(0x80ffffff);
                    }
                    showList.add(polygonOption);
                    baiduMapUtil.add_more_overlay(showList);
                }
            }else {

            }
        }else {
            Message msg=new Message();
            msg.arg1=1;
            handler.sendMessage(msg);
        }
    }

    /**
     * 查询基站位置
     */
    private class FindPos extends AsyncTask<Object, Void, Object> {
        private LuceCellInfo cellInfo;
        private FindPos(LuceCellInfo luceCellInfo){
            this.cellInfo= luceCellInfo;
        }

        @Override
        protected Object doInBackground(Object... params) {
            JSONObject object=null;
            //查询轨迹
            if (conn.getToken()) {
                try {
                    if(mode==dataCode.cdma)
                    {
                        object=conn.submitMsg(conn.FindPos(mcc,mnc,sid,nid,bid));
                    }else {
                        object=conn.submitMsg(conn.FindPos(mcc,mnc,lac,cellid));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return object;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(Object object) {
            JSONObject object1=(JSONObject) object;
            if (conn.getResult(object1)==0){
//                luceCellInfos.add(cellInfo);
                try {
                    JSONArray array=object1.getJSONArray("datalist");
                    List<Point> list=new ArrayList<>();
                    List<String> list_rssi=new ArrayList<>();
                    List<LatLng> latLngs=new ArrayList<>();
                    for (int i=0;i<array.length();i++){
                        JSONObject jsonObject= (JSONObject) array.opt(i);
                        Point point=new Point();
                        String lon=jsonObject.getString("lon");
                        String lat=jsonObject.getString("lat");
                        point.setLat(lat);
                        point.setLon(lon);
                        final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(lat), Double.valueOf(lon));
                        if (jsonObject.has("acc")){
                            if (!(jsonObject.getString("acc").equals("")||jsonObject.getString("acc")==null)){
                                point.setAcc(jsonObject.getString("acc"));
                            }
                        }
                        if (jsonObject.has("rssi")){
                            if (!(jsonObject.getString("rssi").equals("")||jsonObject.getString("rssi")==null)){
                                point.setRssi(jsonObject.getString("rssi"));
                            }
                        }
                        if (jsonObject.has("addr")){
                            if (!(jsonObject.getString("addr").equals("")||jsonObject.getString("addr")==null)){
                                point.setAddr(jsonObject.getString("addr"));
                            }
                        }
                        double lat1= Double.valueOf(lat);
                        double lon1= Double.valueOf(lon);
                        if (jsonObject.has("acc")){
                            if (!((int)lat1==0&&(int)lon1==0)){
                                end_latLng=new LatLng(latlon[0],latlon[1]);
                                baiduMapUtil.addMarker(latlon[0],latlon[1],"基站信息："+parameters[0]+","+parameters[1]+","+parameters[2]+"\n"+"第三方数据",0f,0f,255f,1.0f);
                            }
                        }
                        else if (jsonObject.has("rssi")){
                            list_rssi.add(point.getRssi());
                            list.add(point);
                        }
                    }
                    //按强度渐变添加覆盖物
                    int max_rssi= Integer.valueOf(list_rssi.get(0));
                    int min_rssi= Integer.valueOf(list_rssi.get(0));
                    for (int i=0;i<list_rssi.size()-1;i++){
                        int current= Integer.valueOf(list_rssi.get(i));
                        if (current>max_rssi){
                            max_rssi=current;
                        } else if (current<min_rssi){
                            min_rssi=current;
                        }
                    }
                    //根据最大小值对每个数算一个百分比，对百分比的区域进行比较，然后区分不同的颜色
                    // "("+(100-this._rssi)*255/100 + ","+this._rssi*255/100 +",0)"
                    // var rssi = (json[i].data[0].power - minrssi)*100/(maxrssi-minrssi);
                    Log.e("rssi",max_rssi+"   "+min_rssi+"");
                    for (int i=0;i<list.size();i++){
                        int rssi_rgb=(Integer.valueOf(list.get(i).getRssi())-min_rssi)*100/(max_rssi-min_rssi);
                        final double[] latlon1= GPSUtils.wgs2bd(Double.valueOf(list.get(i).getLat()), Double.valueOf(list.get(i).getLon()));
                        if (!((int)latlon1[0]==0&&(int)latlon1[1]==0)){
                            baiduMapUtil.addMarker(latlon1[0],latlon1[1],"基站信息："+parameters[0]+","+parameters[1]+","+parameters[2]+"\n"+ Double.valueOf(list.get(i).getRssi())+"",(100-rssi_rgb)*255/100 ,rssi_rgb*255/100,0f,1.0f);
                        }
                    }
                    for (int i=0;i<list.size();i++){
                        Point point= list.get(i);
                        double lat= Double.valueOf(point.getLat());
                        double lon= Double.valueOf(point.getLon());
                        if ((int)lat==0&&(int)lon==0){
                            list.remove(point);
                            i--;
                        }
                    }
                    ConvexHull convexHull=new ConvexHull(list);
                    List<Point> list_po=convexHull.calculateHull();
                    //垃框查询
                    if (list_po.size()>=3){
                        for (int i=0;i<list_po.size();i++){
                            Point point=list_po.get(i);
                            final double[] latlon= GPSUtils.wgs2bd(Double.valueOf(point.getLat()), Double.valueOf(point.getLon()));
                            LatLng latLng=new LatLng(latlon[0],latlon[1]);
                            latLngs.add(latLng);
                        }
                        if (showList.size()==0){
                            baiduMapUtil.draw_find(latLngs);
                        }else if (showList.size()==1||showList.size()>1){
                            OverlayOptions polygonOption=null;
                            if (showList.size()<7){
                                polygonOption = new PolygonOptions()
                                        .points(latLngs)
                                        .stroke(new Stroke(5, color[showList.size()-1]))//color[showList.size()-1]
                                        .fillColor(0x80ffffff);
                            }else {
                                polygonOption = new PolygonOptions()
                                        .points(latLngs)
                                        .stroke(new Stroke(5, color[showList.size()-7]))//color[showList.size()-7]
                                        .fillColor(0x80ffffff);
                            }
                            showList.add(polygonOption);
                            baiduMapUtil.add_more_overlay(showList);
                        }
                    }else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                //查询手机数据库数据
                List<String> list=new ArrayList<>();
                list=impl.FindPos(mnc,lac,cellid);
                if(!list.isEmpty()) {
//                    luceCellInfos.add(cellInfo);
                    baiduMapUtil.addMarker(Double.valueOf(list.get(0)), Double.valueOf(list.get(1)),
                            "基站信息："+parameters[0]+","+parameters[1]+","+parameters[2]+"\n"+"手机数据库数据",
                            251f, 93f, 255f, 1f);
                }else {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("温馨提示");
                    builder.setMessage("查询不到该基站的位置信息");
                    builder.setPositiveButton("确定", null);
                    builder.show();
                }
            }
//            lacDataAdapter.notifyDataSetChanged();
        }
    }
    private boolean GetRequestParam() {
        boolean start_flg = false;
        mcc = "460";
        mnc = "00";
        lac = lac_edit.getText().toString().trim();
        cellid = cell_edit.getText().toString().trim();
        sid = lac_edit.getText().toString().trim();
        nid = cell_edit.getText().toString().trim();
        bid = bid_edit.getText().toString().trim();
        if(mode_index== dataCode.cdma)
        {
//            lac_text.setText("SID:");
            if(net==2)
            {
                mnc = "11";
            }
            else
            {
                mnc = "03";
            }
        }
        if (net == 0)
        {
            mode = mode_index;
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mnc = "01";
            }
        }
        else if (net == 1)//3g
        {
            if (mode_index == dataCode.china_mobile) {
                mnc = "00";
                mode = dataCode.td_swcdma;
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.wcdma;
                mnc = "01";
            }
        }
        else if (net == 2)//4g
        {
            if (mode_index == dataCode.china_mobile) {
                mode = dataCode.tdd_lte;
                mnc = "00";
            } else if (mode_index == dataCode.china_unicom) {
                mode = dataCode.fdd_lte;
                mnc = "01";
            } else {
                mode = dataCode.fdd_lte;
                mnc = "01";
            }

        }

        if(NumCheck.isNumeric(mcc)==true)
        {
            if(NumCheck.isNumeric(mnc)==true)
            {
                if(hex_mode.isChecked())
                {
                    hex_flg = true;
                }
                else
                {
                    hex_flg = false;
                }
                if(mode==dataCode.cdma)//电信
                {
                    if("".equals(sid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置SID!","确定");
                    }
                    else if("".equals(nid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置NID!","确定");
                    }
                    else if("".equals(bid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置BID!","确定");
                    }
                    else
                    {

                        if(hex_flg==true)//十六进制
                        {
                            boolean x_flg=false;
                            //检查x_flg是否为16进制  如果是则转换为10进制
                            x_flg= NumCheck.isTrueHexDigit(sid);
                            if(x_flg==true)
                            {
                                long value = Long.parseLong(sid,16);
                                sid= Long.toString(value);
                                x_flg=NumCheck.isTrueHexDigit(nid);
                                if(x_flg==true)
                                {
                                    value = Long.parseLong(nid,16);
                                    nid= Long.toString(value);
                                    x_flg=NumCheck.isTrueHexDigit(bid);
                                    if(x_flg==true)
                                    {
                                        value = Long.parseLong(bid,16);
                                        bid= Long.toString(value);
                                        start_flg=true;
                                    }
                                    else
                                    {
                                        AlrDialog_Show.alertDialog(context,"提示","请设置正确格式BID!","确定");
                                    }
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式NID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式SID!","确定");
                            }
                        }
                        else//十进制
                        {
                            if(NumCheck.isNumeric(bid)==true)
                            {
                                if(NumCheck.isNumeric(nid)==true)
                                {
                                    if(NumCheck.isNumeric(sid)==true)
                                    {
                                        start_flg=true;
                                    }
                                    else
                                    {
                                        AlrDialog_Show.alertDialog(context,"提示","请设置正确格式SID!","确定");
                                    }
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式NID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式BID!","确定");
                            }
                        }
                    }
                }
                else
                {
                    if("".equals(lac))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置LAC!","确定");
                    }
                    else if("".equals(cellid))
                    {
                        AlrDialog_Show.alertDialog(context,"提示","请设置CELLID!","确定");
                    }
                    else
                    {
                        if(hex_flg==true)
                        {
                            if(NumCheck.isTrueHexDigit(lac)==true)
                            {
                                long value = Long.parseLong(lac,16);
                                lac= Long.toString(value);
                                if(NumCheck.isTrueHexDigit(cellid)==true)
                                {
                                    value = Long.parseLong(cellid,16);
                                    cellid= Long.toString(value);
                                    start_flg=true;
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式CELLID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式LAC!","确定");
                            }
                        }
                        else
                        {
                            if(NumCheck.isNumeric(lac)==true)
                            {
                                if(NumCheck.isNumeric(cellid)==true)
                                {
                                    start_flg=true;
                                }
                                else
                                {
                                    AlrDialog_Show.alertDialog(context,"提示","请设置正确格式CELLID!","确定");
                                }
                            }
                            else
                            {
                                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式LAC!","确定");
                            }
                        }
                    }
                }
            }
            else
            {
                AlrDialog_Show.alertDialog(context,"提示","请设置正确格式mnc!","确定");
            }
        }
        else
        {
            AlrDialog_Show.alertDialog(context,"提示","请设置正确格式mcc!","确定");
        }
        Log.v("sid1", sid);
        Log.v("nid1", nid);
        Log.v("bid1", bid);
        return start_flg;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
