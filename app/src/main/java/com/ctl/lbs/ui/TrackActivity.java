package com.ctl.lbs.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctl.lbs.InfoWindowHolder;
import com.ctl.lbs.R;
import com.ctl.lbs.cell.BtsType;
import com.ctl.lbs.cell.LuceCellInfo;
import com.ctl.lbs.cell.ProcessBtsData;
import com.ctl.lbs.database.DbAcessImpl;

import java.util.ArrayList;
import java.util.LinkedList;

public class TrackActivity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        context = this;
        initMap();
        initMapTopView();
    }

    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private InfoWindow mInfoWindow;
    private LinearLayout baidumap_infowindow;
    private MarkerOnInfoWindowClickListener markerListener;
    private void initMap() {
        mMapView = (MapView)findViewById(R.id.track_mapView);
        baidumap_infowindow = (LinearLayout) LayoutInflater.from (context).inflate (R.layout.baidu_map_infowindow, null);
        mBaiduMap = mMapView.getMap();
        markerListener = new MarkerOnInfoWindowClickListener();
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
    private final class  MarkerOnInfoWindowClickListener implements InfoWindow.OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(){
            mBaiduMap.hideInfoWindow();
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
    private LinkedList<LatLng>  trackList = new LinkedList<>();
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
}
