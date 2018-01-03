package com.ctl.lbs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Text;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctl.lbs.BaiduMap.LocationService;
import com.ctl.lbs.ExpandListView.CellInfoAdapter;
import com.ctl.lbs.ExpandListView.WifiInfoAdapter;
import com.ctl.lbs.InfoWindowHolder;
import com.ctl.lbs.R;
import com.ctl.lbs.bluetooth.BluetoothConn;
import com.ctl.lbs.bluetooth.BluetoothState;
import com.ctl.lbs.bluetooth.RevThread;
import com.ctl.lbs.cell.BtsType;
import com.ctl.lbs.cell.LuceCellInfo;
import com.ctl.lbs.cell.ProcessBtsData;
import com.ctl.lbs.cell.WifiInfo;
import com.ctl.lbs.database.DbAcessImpl;
import com.ctl.lbs.utils.LocationPoint;
import com.ctl.lbs.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer,View.OnClickListener{

    private Context context;

    private ListView mainListView;

    private BluetoothConn bluetoothConn;

    private ProcessBtsData processBtsData;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private LocationService locationService=null;
    private LocationPoint point=null;
    private View mapRelaView;
    private TextView saveDataBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().addActivity(this);
        intiView();
        context = this;
        saveDataBtn = (TextView)findViewById(R.id.save_data_btn);
        saveDataBtn.setOnClickListener(this);
        saveDataBtn.setEnabled(false);

        bluetoothConn = new BluetoothConn("CTL", 8000);
        bluetoothConn.addObserver(this);

        processBtsData = new ProcessBtsData(context,bluetoothConn);

        processBtsData.addObserver(this);

        initView();
        initListViewHeadTv();
        initMap();
        initMapTopView();
        initDataSwitch();
        initButtom();
        mapRelaView.setVisibility(View.INVISIBLE);
        GetMyLocation();

    }

    private void GetMyLocation() {
        new Thread(new Runnable() {
            public void run() {
                locationService = ((MyApplication)context.getApplicationContext()).locationService;
                locationService.registerListener(mListener);
                locationService.setLocationOption(locationService.getDefaultLocationClientOption());
                locationService.start();
            }
        }).start();
    }

    private InfoWindow mInfoWindow;
    private LinearLayout baidumap_infowindow;
    private MarkerOnInfoWindowClickListener markerListener;
    private void initMap() {
        baidumap_infowindow = (LinearLayout) LayoutInflater.from (context).inflate (R.layout.baidu_map_infowindow, null);
        mBaiduMap = mMapView.getMap();
        markerListener = new MarkerOnInfoWindowClickListener ();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(showBtsType.equals("全部")){
                    return false;
                }
                createInfoWindow(baidumap_infowindow,(LuceCellInfo)  marker.getExtraInfo ().get ("marker"));
                final LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow (BitmapDescriptorFactory.fromView (baidumap_infowindow), ll, -47, markerListener);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
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
        if(locationService!=null)
            locationService.registerListener(mListener);
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

    private final class  MarkerOnInfoWindowClickListener implements InfoWindow.OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(){
            mBaiduMap.hideInfoWindow();
        }
    }
    List<LuceCellInfo> cellListData;
    List<WifiInfo> wifiList;
    CellInfoAdapter adapter;
    WifiInfoAdapter wifiInfoAdapter;
    private void intiView() {
        mainListView=(ListView)findViewById(R.id.main_listview);
        wifiList = new ArrayList<>();
        cellListData = new ArrayList<>();
        wifiList.addAll(initWifiList());
        cellListData.addAll(initLuceList());
        adapter=new CellInfoAdapter(this, cellListData,R.layout.extend_listview_item);
        mainListView.setAdapter(adapter);

        wifiInfoAdapter = new WifiInfoAdapter(this, wifiList, R.layout.wifi_item);

    }

    private ArrayList<WifiInfo> initWifiList(){
        ArrayList<WifiInfo> wifiList = new ArrayList<>();
        int i = 10;
        while (i>0){
            WifiInfo wifiInfo = new WifiInfo();
            wifiList.add(wifiInfo);
            i--;
        }
        return wifiList;
    }

    private ArrayList<LuceCellInfo> initLuceList(){
        ArrayList<LuceCellInfo> luceCellInfos = new ArrayList<>();
        int i = 10;
        while (i>0){
            LuceCellInfo luceCellInfo = new LuceCellInfo();
            luceCellInfos.add(luceCellInfo);
            i--;
        }
        return luceCellInfos;
    }


    private void updateDevConState(final BluetoothState state) {
        if (logoView != null) {
            logoView.post(new Runnable() {
                @Override
                public void run() {
                    if(state == BluetoothState.CONNECTED) {
                        logoView.setBackgroundResource(R.drawable.logo_green);
                        saveDataBtn.setEnabled(true);
                        saveDataBtn.setBackgroundResource(R.drawable.btn_green_style);
                    }
                    else {
                        logoView.setBackgroundResource(R.drawable.logo_grey);
                        saveDataBtn.setEnabled(false);
                        saveDataBtn.setBackgroundResource(R.drawable.btn_style);
                    }
                }
            });
        }
    }


    /**
     * 数据接收线程
     */
    private RevThread revThread;
    @Override
    public void update(Observable o, final Object data) {
        if(data instanceof String){
            if("CLEAR".equals((String) data)){
                logoView.post(new Runnable() {
                    @Override
                    public void run() {
                        cellListData.clear();
                        cellListData.addAll(processBtsData.getLuceInfoList());
                        adapter.notifyDataSetChanged();
                        wifiList.clear();
                        wifiList.addAll(processBtsData.wifiList);
                        wifiInfoAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
        else if (data instanceof BluetoothState) {
            BluetoothState state = (BluetoothState) data;
            updateDevConState(state);
            if (data == BluetoothState.CONNECTED) {
                revThread = null;
                revThread = new RevThread( bluetoothConn, context,processBtsData);
                new Thread(revThread).start();
            } else {
                if (revThread != null) {
                    revThread.isTerminated();
                    revThread = null;
                }
            }
        }

        else if(data instanceof Boolean){
            final boolean flag  =(Boolean)data;

            logoView.post(new Runnable() {
                @Override
                public void run() {
                    if(flag)
                        gpsView.setBackgroundResource(R.drawable.gps_green);
                    else
                        gpsView.setBackgroundResource(R.drawable.gps_grey);
                }
            });

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN)
        {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_dialog_exit_confirm))
                    .setMessage(getString(R.string.message_confirm_to_exit))
                    .setPositiveButton(android.R.string.ok,
                            new android.content.DialogInterface.OnClickListener()
                            {
                                public void onClick(final DialogInterface dialog, final int which)
                                {
                                    MyApplication.getInstance().exit();
                                }
                            }).setNegativeButton(android.R.string.cancel, null).show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void createInfoWindow(LinearLayout baidumap_infowindow,LuceCellInfo luceCellInfo){
        InfoWindowHolder holder = null;
//        if(baidumap_infowindow.getTag () == null){
        holder = new InfoWindowHolder ();
        holder.tv_title = (TextView) baidumap_infowindow.findViewById (R.id.map_window_title);
        holder.tv_content = (TextView) baidumap_infowindow.findViewById (R.id.map_window_content);

//        }
//        holder = (InfoWindowHolder) baidumap_infowindow.getTag ();
        String title = null;
        StringBuffer sb = new StringBuffer();
        sb.append("邻区\n");
        if(luceCellInfo.getBtsType()==BtsType.CDMA){
            title = luceCellInfo.getBtsType()+"大区号：("+luceCellInfo.getLac()+","+luceCellInfo.getCellId()+")"+"，小区号："+luceCellInfo.getBid()+"，场强："+luceCellInfo.getRssi();
        }
        else{
            title = luceCellInfo.getBtsType()+"，大区号："+luceCellInfo.getLac()+"，小区号："+luceCellInfo.getCellId()+"，场强："+luceCellInfo.getRssi();
            DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
            ArrayList<LuceCellInfo> list = new ArrayList<>();
            list.addAll(dbAcess.findOnlySameLacBts(String.valueOf(luceCellInfo.getLac()),luceCellInfo.getBtsType().toString(),luceCellInfo.getLatitudeGps(),luceCellInfo.getLongitudeGps(),ProcessBtsData.mark));
            for(LuceCellInfo nCellinfo:list){
                if(nCellinfo.getCellId()!=luceCellInfo.getCellId()){
                    sb.append("小区号："+nCellinfo.getCellId()+",场强："+nCellinfo.getRssi()+"\n");
                }
            }
            holder.tv_content.setText (sb.toString());
        }
        holder.tv_title.setText (title);
    }

    public static double baiduLatitude = 0.0, baiduLongitude = 0.0;
    private BDLocationListener mListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                if (location == null)
                    return;
                // 设置定位 位置信息
                ((MyApplication) context.getApplicationContext()).locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius()).direction(100)// 精度范围
                        .latitude(location.getLatitude())// 经度
                        .longitude(location.getLongitude()).build();// 纬度
                Log.e("MainActivity",MyApplication.getInstance().getLocData().latitude+","+MyApplication.getInstance().getLocData().longitude);
                mBaiduMap.setMyLocationData(MyApplication.getInstance().getLocData());
//                LatLng ll = new LatLng(location.getLatitude(),
//                        location.getLongitude());
//                MapStatus.Builder builder = new MapStatus.Builder();
//                builder.target(ll).zoom(18.0f);
//                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                    if (location == null)
                        return;
                    if (location.getAddrStr() != null) {
                        baiduLatitude = location.getLatitude();
                        baiduLongitude = location.getLongitude();
                    } else {
                        baiduLatitude = 0;
                        baiduLongitude = 0;
                    }
                }
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nerror code : ");
                sb.append(location.getLocType());
                sb.append("\nlatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nradius : ");
                sb.append(location.getRadius());
                sb.append("\nCountryCode : ");
                sb.append(location.getCountryCode());
                sb.append("\nCountry : ");
                sb.append(location.getCountry());
                sb.append("\ncitycode : ");
                sb.append(location.getCityCode());
                sb.append("\ncity : ");
                sb.append(location.getCity());
                sb.append("\nDistrict : ");
                sb.append(location.getDistrict());
                sb.append("\nStreet : ");
                sb.append(location.getStreet());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\nDescribe: ");
                sb.append(location.getLocationDescribe());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(location.getDirection());
                sb.append("\nPoi: ");
                //*****************************
                point=new LocationPoint();
                point.setLat(location.getLatitude());
                point.setLon(location.getLongitude());
                if(location.getAddrStr()!=null){
                    point.setAddress(location.getAddrStr()+" "+location.getLocationDescribe());
                }else{

                }
                Log.e("MainActivity",point.getLat()+","+point.getLon()+","+point.getAddress());
//                NeighborCell.getInstance(context).sendObservableNotice(new NoticeData(MessageType.AUTO_COLLEC_FINISH,""));
                //*********************************
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 单位：米
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    sb.append("\noperationers : ");
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                Log.d("Loc",sb.toString());
            }
        }

    };

    private LinkedList<LatLng>  trackList = new LinkedList<>();
    /**
     * 判断是否能将坐标点添加至轨迹集合，20米内不添加，20米外添加
     * @param latLng
     * @return
     */
    private boolean isEnableAdd(LatLng latLng){
        for(LatLng latLngInList:trackList){
            if(DistanceUtil.getDistance(latLng,latLngInList)<10){
                return false;
            }
        }
        return true;
    }
    private boolean addPointToTrackList(LatLng point){

        if(trackList.size()==0){
            trackList.add(point);
            return true;
        }
        else{
            if(isEnableAdd(point)) {
                trackList.add(point);
                return true;
            }
        }
        return false;
    }
    /**
     * 添加坐标点到地图上
     * @param latLng
     */
    private void addPointToMap(LatLng latLng){
        boolean add = addPointToTrackList(latLng);
        if(add) {
            Message msg = new Message();
            msg.arg1 = 1010;
            msg.obj = latLng;
            handler.sendMessage(msg);
        }
    }
    private Spinner modeSpinner;
    private String showBtsType = "全部";
    private ArrayAdapter<String> typeSpinnerAdapter;
    private void initMapTopView() {

        modeSpinner=(Spinner)findViewById(R.id.spinner_marker_type);
        Resources res = getResources ();
        String[] modes = res.getStringArray(R.array.mode_arrays);
        typeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modes);
        typeSpinnerAdapter.setDropDownViewResource(R.layout.drop_down_item);
        modeSpinner.setAdapter(typeSpinnerAdapter);
        modeSpinner.setOnItemSelectedListener(typeLis);

    }

    AdapterView.OnItemSelectedListener typeLis = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i){
                case 0:
                    showBtsType = "全部";
                    break;
                case 1:
                    showBtsType = BtsType.GSM_MOBILE.toString();
                    break;
                case 2:
                    showBtsType = BtsType.GSM_UNICOM.toString();
                    break;
                case 3:
                    showBtsType = BtsType.WCDMA.toString();
                    break;
                case 4:
                    showBtsType = BtsType.CDMA.toString();
                    break;
                case 5:
                    showBtsType = BtsType.TDSCDMA.toString();
                    break;
                case 6:
                    showBtsType = BtsType.LTE_MOBILE.toString();
                    break;
                case 7:
                    showBtsType = BtsType.LTE_UNICOM.toString();
                    break;
                case 8:
                    showBtsType = BtsType.LTE_TELECOM.toString();
                    break;
            }
            if(showBtsType.equals("全部")){

                initTack();
            }else {
                initBts(showBtsType);
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    /**
     * 任务下的所有制定制式的数据，内层集合中每个List中是同LAC的基站
     */
    private LinkedList<ArrayList<LuceCellInfo>> btsInfoList = new LinkedList<>();

    /**
     * 将基站数据添加至区分LAC的btsInfoList中
     * @param luceCellInfo
     */
    private void addBsInfoToList(LuceCellInfo luceCellInfo){
        for(ArrayList<LuceCellInfo> sameLacList:btsInfoList){
            if(luceCellInfo.getBtsType()==BtsType.CDMA){
                if(sameLacList.get(0).getBid()==luceCellInfo.getBid()){
                    sameLacList.add(luceCellInfo);
                    return;
                }
            }
            else{
                if(sameLacList.get(0).getLac()==luceCellInfo.getLac()){
                    sameLacList.add(luceCellInfo);
                    return;
                }
            }
        }
        ArrayList<LuceCellInfo> newList = new ArrayList<>();
        newList.add(luceCellInfo);
        btsInfoList.add(newList);
    }

    private void  addBtsInfoToMap(){

        int i = 0;
        for(ArrayList<LuceCellInfo> luceCellInfos:btsInfoList){
            for(LuceCellInfo luceCellInfo : luceCellInfos){
                if(!addPointToTrackList(luceCellInfo.getBaiduPoint())){
                    continue;
                }
                Message msg = new Message();
                msg.obj  = luceCellInfo;
                msg.arg1 = 101010;
                msg.arg2 = i;
                handler.sendMessage(msg);
            }
            i++;
        }
    }

    private void initTack(){
        new Thread(){
            @Override
            public void run() {
                btsInfoList.clear();
                trackList.clear();
                mBaiduMap.clear();
                initTrackList(ProcessBtsData.mark);
            }
        }.start();
    }
    private void initTrackList(String taskName){

        DbAcessImpl db=DbAcessImpl.getDbInstance(context);

        LinkedList<LatLng> list = new LinkedList<>();

        list.addAll(db.selectByFileGetPoint(taskName));

        for(LatLng latLng:list){
            addPointToMap(latLng);
        }
    }
    private void initBts(final String btsType ){
        new Thread(){
            public void run() {
                DbAcessImpl db=DbAcessImpl.getDbInstance(context);
                LinkedList<LuceCellInfo> luceCellInfos = new LinkedList<LuceCellInfo>();

                luceCellInfos.addAll(db.selectByNameAndType(ProcessBtsData.mark,btsType));
                btsInfoList.clear();
                trackList.clear();
                mBaiduMap.clear();
                for(LuceCellInfo luceCellInfo:luceCellInfos){
                    addBsInfoToList(luceCellInfo);
                }
                addBtsInfoToMap();
            }
        }.start();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 == 101010){
//                progressDialog.dismiss();
                LuceCellInfo luceCellInfo =(LuceCellInfo) msg.obj;

                LatLng latLng = luceCellInfo.getBaiduPoint();
                String title = luceCellInfo.getLac()+","+luceCellInfo.getCellId()+","+luceCellInfo.getBid();
                BitmapDescriptor bitmap = getBitmap(msg.arg2);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap).title(title);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //在地图上添加Marker，并显示
                Marker marker = (Marker)mBaiduMap.addOverlay(option);
                // 将信息保存
                Bundle bundle = new Bundle ();
                bundle.putSerializable ("marker", luceCellInfo);
                marker.setExtraInfo (bundle);
            }
            else if(msg.arg1==1010){

                LatLng latLng = (LatLng)msg.obj;
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.iconmarka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(option);
            }
        }
    };


    private BitmapDescriptor getBitmap(int id){
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p1);
        switch (id%10){
            case 0:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p1);
                break;
            case 1:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p2);
                break;
            case 2:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p3);
                break;
            case 3:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p4);
                break;
            case 4:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p5);
                break;
            case 5:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p6);
                break;
            case 6:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p7);
                break;
            case 7:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p8);
                break;
            case 8:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p9);
                break;
            case 9:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p10);
                break;
        }
        return bitmap;
    }



    private View bottomLeft,bottomMiddle,bottomRight;

    private TextView bottomLeftImage,bottomMiddleImage,bottomRightImage;

    private TextView bottomLeftTv,bottomMiddleTv,bottomRightTv;


    private void initButtom(){
        bottomLeft = findViewById(R.id.main_bottom_left);
        bottomLeft.setOnClickListener(this);
        bottomLeftImage = (TextView)findViewById(R.id.main_bottom_left_image);
        bottomLeftTv = (TextView)findViewById(R.id.main_bottom_left_text);

        bottomMiddle = findViewById(R.id.main_bottom_middle);
        bottomMiddle.setOnClickListener(this);
        bottomMiddleImage = (TextView) findViewById(R.id.main_bottom_middle_image);
        bottomMiddleTv = (TextView)findViewById(R.id.main_bottom_middle_text);
        bottomMiddleTv.setTextColor(Color.GREEN);

        bottomRight = findViewById(R.id.main_bottom_right);
        bottomRight.setOnClickListener(this);
        bottomRightImage = (TextView)findViewById(R.id.main_bottom_right_image);
        bottomRightTv = (TextView) findViewById(R.id.main_bottom_right_text);
    }

    private void clearBottom(){
        bottomLeftImage.setBackgroundResource(R.drawable.track_grey_1);
        bottomMiddleImage.setBackgroundResource(R.drawable.data_grey);
        bottomRightImage.setBackgroundResource(R.drawable.right_grey);
        bottomLeftTv.setTextColor(Color.BLACK);
        bottomMiddleTv.setTextColor(Color.BLACK);
        bottomRightTv.setTextColor(Color.BLACK);
    }




    /*********************底部切换标签***************************************/
    private TextView mobileGsmTv,mobileTdTv,mobileLteTv,unicomLteTv,telecomLteTv, cdmaTv,wifiTv,wcdma,unicomGsmTv;

    private void initDataSwitch(){
        mobileGsmTv = (TextView)findViewById(R.id.gsm_m_tv);
        mobileGsmTv.setOnClickListener(this);
        mobileTdTv = (TextView)findViewById(R.id.td_tv);
        mobileTdTv.setOnClickListener(this);
        mobileLteTv = (TextView)findViewById(R.id.ltem_tv);
        mobileLteTv.setOnClickListener(this);

        unicomGsmTv = (TextView)findViewById(R.id.gsm_u_tv);
        unicomGsmTv.setOnClickListener(this);
        wcdma = (TextView)findViewById(R.id.wcdma_tv);
        wcdma.setOnClickListener(this);
        unicomLteTv = (TextView)findViewById(R.id.lteu_tv);
        unicomLteTv.setOnClickListener(this);


        wifiTv = (TextView)findViewById(R.id.wifi_tv);
        wifiTv.setOnClickListener(this);
        cdmaTv = (TextView)findViewById(R.id.cdma_tv);
        cdmaTv.setOnClickListener(this);
        telecomLteTv = (TextView)findViewById(R.id.ltet_tv);
        telecomLteTv.setOnClickListener(this);
    }

    private void changeType(BtsType btsType,TextView selectedTv){
        processBtsData.setCurrentType(btsType);
        clearDataSwitchItem();
        if(btsType == BtsType.WIFI){
            mainListView.setAdapter(wifiInfoAdapter);
            wifiInfoAdapter.notifyDataSetChanged();
        }
        else{
            mainListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        selectedTv.setBackgroundResource(R.drawable.btn_green_style);

        if(btsType==BtsType.WIFI){
            wifiList.clear();
            wifiList.addAll(processBtsData.wifiList);
            wifiInfoAdapter.notifyDataSetChanged();
        }else {

            cellListData.clear();
            cellListData.addAll(processBtsData.getLuceInfoList());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wifi_tv:
                changeType(BtsType.WIFI,wifiTv);
                switchHeader(BtsType.WIFI);
                break;
            case R.id.wcdma_tv:
                changeType(BtsType.WCDMA,wcdma);
                switchHeader(BtsType.WCDMA);
                break;
            case R.id.cdma_tv:
                changeType(BtsType.CDMA,cdmaTv);
                switchHeader(BtsType.CDMA);
                break;
            case R.id.gsm_m_tv:
                changeType(BtsType.GSM_MOBILE,mobileGsmTv);
                switchHeader(BtsType.GSM_MOBILE);
                break;
            case R.id.gsm_u_tv:
                changeType(BtsType.GSM_UNICOM,unicomGsmTv);
                switchHeader(BtsType.GSM_UNICOM);
                break;
            case R.id.ltem_tv:
                changeType(BtsType.LTE_MOBILE,mobileLteTv);
                switchHeader(BtsType.LTE_MOBILE);
                break;
            case R.id.lteu_tv:
                changeType(BtsType.LTE_UNICOM,unicomLteTv);
                switchHeader(BtsType.LTE_UNICOM);
                break;
            case R.id.ltet_tv:
                changeType(BtsType.LTE_TELECOM,telecomLteTv);
                switchHeader(BtsType.LTE_TELECOM);
                break;
            case R.id.td_tv:
                changeType(BtsType.TDSCDMA,mobileTdTv);
                switchHeader(BtsType.TDSCDMA);
                break;
            case R.id.main_bottom_left:
                clearBottom();
                maplayoutView.setVisibility(View.VISIBLE);
                dataView.setVisibility(View.GONE);
                moreView.setVisibility(View.GONE);
                bottomLeftTv.setTextColor(Color.GREEN);
                bottomLeftImage.setBackgroundResource(R.drawable.track_green_1);
                break;
            case R.id.main_bottom_middle:
                clearBottom();
                maplayoutView.setVisibility(View.GONE);
                dataView.setVisibility(View.VISIBLE);
                moreView.setVisibility(View.GONE);
                bottomMiddleTv.setTextColor(Color.GREEN);
                bottomMiddleImage.setBackgroundResource(R.drawable.data_green);
                break;
            case R.id.main_bottom_right:
                clearBottom();
                dataView.setVisibility(View.GONE);
                maplayoutView.setVisibility(View.GONE);
                moreView.setVisibility(View.VISIBLE);
                bottomRightTv.setTextColor(Color.GREEN);
                bottomRightImage.setBackgroundResource(R.drawable.right_green);
                break;
            case R.id.save_data_btn:
                if(processBtsData.isSaveToDb()){
                    processBtsData.setSaveToDb(false);
                    saveDataBtn.setBackgroundResource(R.drawable.btn_green_style);
                }
                else{
                    processBtsData.setSaveToDb(true);
                    saveDataBtn.setBackgroundResource(R.drawable.btn_red_style);
                }
                break;
        }

    }

    private View maplayoutView;
    private View dataView;
    private GridView moreGridView;
    private TextView logoView,gpsView;
    private View moreView;
    private Button frequencyBtn;
    private void initView() {
        maplayoutView = findViewById(R.id.main_map_rela);
        dataView = findViewById(R.id.data_view);
        mMapView=(MapView) findViewById(R.id.bmapView);
        mapRelaView = findViewById(R.id.main_map_rela);
        mapRelaView.setVisibility(View.INVISIBLE);
        moreGridView = (GridView)findViewById(R.id.more_grid_view);
        moreView = findViewById(R.id.more_view);
        moreView.setVisibility(View.GONE);
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < gridItemText.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", mImages[i]);// 添加图像资源的ID
            map.put("ItemText", gridItemText[i]);// 按序号做ItemText
            lstImageItem.add(map);
        }
        //构建一个适配器
        SimpleAdapter simple = new SimpleAdapter(this, lstImageItem, R.layout.gridview_item,
                new String[] { "ItemImage", "ItemText" }, new int[] {R.id.ItemImage, R.id.ItemText });
        moreGridView.setAdapter(simple);
        //添加选择项监听事件
        moreGridView.setOnItemClickListener(new GridView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent0 = new Intent();
                        intent0.setClass(context,MapFindActivity.class);
                        startActivity(intent0);
                        break;
                    case 1:
                        Intent intent1 = new Intent();
                        intent1.putExtra("类型", "导出");
                        intent1.setClass(context,FileImportActivity.class);
                        startActivity(intent1);
                        break;
                    case 2:
                        Intent intent2 = new Intent();
                        intent2.setClass(context,SettingActivity.class);
                        startActivity(intent2);
                        break;
                    case 3:
                        final EditText ed = new EditText(context);

                        ed.setHint("请输入备注");
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("定点采集");
                        builder.setView(ed);
                        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String taskName = ed.getText().toString().replaceAll(" ","");
                                if(taskName==null||taskName.length()==0){
                                    Utils.alertDialog(context,"温馨提示","备注不能空","确定");
                                    return;
                                }

                                bluetoothConn.sendCmd("BC+SETDUR=00");
                                Utils.delay(300);
                                bluetoothConn.sendCmd("BC+SETINF="+taskName);
                                Utils.delay(300);
                                bluetoothConn.sendCmd("BC+TRIGES");
                            }
                        }).show();
                        break;
                    case 4:
                        Intent intent4 = new Intent();
                        intent4.setClass(context,OffLineActivity.class);
                        startActivity(intent4);
                        break;
                }
            }

        });

        logoView=(TextView)findViewById(R.id.main_top_left);
        gpsView = (TextView)findViewById(R.id.main_top_gps);
        frequencyBtn = (Button)findViewById(R.id.frequency_btn);
        frequencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListDialog();
            }
        });
    }

    private void showListDialog() {
        final String[] items = { "1秒","2秒","3秒","4秒","5秒","8秒","10秒","20秒","30秒","60秒" };
        final AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("设置采集周期");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                frequencyBtn.setText(items[which]);
                String value = items[which].replaceAll("秒","");
                if(value.length()<2)
                    value = 0+value;
                bluetoothConn.sendCmd("BC+SETDUR="+value);
            }
        });
        listDialog.show();
    }


    private void clearDataSwitchItem(){
        mobileGsmTv.setBackgroundResource(R.drawable.btn_style);
        mobileTdTv.setBackgroundResource(R.drawable.btn_style);
        mobileLteTv.setBackgroundResource(R.drawable.btn_style);
        unicomLteTv.setBackgroundResource(R.drawable.btn_style);
        telecomLteTv.setBackgroundResource(R.drawable.btn_style);
        cdmaTv.setBackgroundResource(R.drawable.btn_style);
        wifiTv.setBackgroundResource(R.drawable.btn_style);
        wcdma.setBackgroundResource(R.drawable.btn_style);
        unicomGsmTv.setBackgroundResource(R.drawable.btn_style);
    }

    private int[] mImages={
            R.drawable.bts_search,
            R.drawable.export,
            R.drawable.system_setting,
            R.drawable.dingdian,
            R.drawable.yidong,
    };

    private String[] gridItemText = {"基站查询","数据导出","系统设置","定点采集","离线地图"};


    private TextView listHeadLacTv,listHeadCiTv,listHeadArfcnTv,listHeadPciTv,listHeadRssiTv;

    private void initListViewHeadTv(){
        listHeadLacTv = (TextView)findViewById(R.id.head_tv_lac);
        listHeadCiTv = (TextView)findViewById(R.id.head_tv_ci);
        listHeadArfcnTv = (TextView)findViewById(R.id.head_tv_arfcn);
        listHeadPciTv = (TextView)findViewById(R.id.head_tv_pci);
        listHeadRssiTv = (TextView)findViewById(R.id.head_tv_rssi);
    }

    private void switchHeader(BtsType btsType){
        if(btsType == BtsType.WIFI){
            listHeadLacTv.setText("类型");
            listHeadCiTv.setText("物理地址");
            listHeadArfcnTv.setVisibility(View.GONE);
            listHeadPciTv.setText("信道");
            listHeadRssiTv.setText("强度");
        }
        else {
            listHeadLacTv.setText("大区");
            listHeadCiTv.setText("小区");
            listHeadArfcnTv.setVisibility(View.VISIBLE);
            listHeadArfcnTv.setText("频点");
            listHeadPciTv.setText("扰码");
            listHeadRssiTv.setText("强度");
        }

    }
}
