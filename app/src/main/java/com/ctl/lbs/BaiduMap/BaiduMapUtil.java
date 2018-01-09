package com.ctl.lbs.BaiduMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.OpenClientUtil;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.ctl.lbs.R;
import com.ctl.lbs.ui.MapFindActivity;

import java.util.List;



/**
 * Created by CTL on 2017/10/24.
 */

/**
 * 百度地图功能类
 */
public class BaiduMapUtil {
    private BaiduMap mBaiduMap;
    private Context context;
    GeoCoder geoCoder = null;
    public BaiduMapUtil(Context context, BaiduMap map) {
        this.context=context;
        this.mBaiduMap=map;
        geoCoder = GeoCoder.newInstance();
    }

    /**
     * 往地图上添加marker
     * @param lat
     * @param lon
     */
    public void addMarker(double lat, double lon, String info, float progressR, float progressG, float progressB, float progressA){
        LatLng point = new LatLng(lat, lon);
        float[] src = new float[]{
                progressR, 0, 0, 0, 0,
                0, progressG, 0, 0, 0,
                0, 0, progressB, 0, 0,
                0, 0, 0, progressA, 0};
        Bitmap baseBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.iconmarka);
        Bitmap baseBitmap_small = BitmapFactory.decodeResource(context.getResources(), R.drawable.iconn);
        Bitmap afterBitmap = Bitmap.createBitmap(baseBitmap.getWidth(),baseBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(afterBitmap);
        Paint paint = new Paint();
        // 定义ColorMatrix，并指定RGBA矩阵
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(src);
        // 设置Paint的颜色
        paint.setColorFilter(new ColorMatrixColorFilter(src));
        // 通过指定了RGBA矩阵的Paint把原图画到空白图片上
        canvas.drawBitmap(baseBitmap, 0,0, new Paint());
        canvas.drawBitmap(baseBitmap_small, new Matrix(), paint);

        // 构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(afterBitmap);
//                .fromResource(icon);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(18)
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        //在地图上添加Marker，并显示
        Marker marker= (Marker) mBaiduMap.addOverlay(option);
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", info);
        marker.setExtraInfo(bundle);
    }
    private Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 往地图上添加一个覆盖物
     * @param latLngs
     */
    public void draw_find(List<LatLng> latLngs){
        OverlayOptions polygonOption = new PolygonOptions()
                .points(latLngs)
                .stroke(new Stroke(5, 0xff00ffff))
                .fillColor(0x80ffffff);//0xffffff00
        mBaiduMap.addOverlay(polygonOption);
        MapFindActivity.showList.add(polygonOption);
    }

    /**
     * 添加多个覆盖物到地图
     * @param showList 添加地图的云层
     * 多个覆盖物颜色不同   可以设置每个OverlayOptions的颜色不同
     */
    public void add_more_overlay(final List<OverlayOptions> showList){
        OverlayManager manager = new OverlayManager(mBaiduMap) {
            @Override
            public List<OverlayOptions> getOverlayOptions() {
                return showList;
            }

            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }

            @Override
            public boolean onPolylineClick(Polyline polyline) {
                return true;
            }
        };
        manager.addToMap();//添加到地图
//        manager.zoomToSpan();//自动缩放到合适比例
    }
    /**
     *启动百度地图导航
     * @param star_address 起点地址
     * @param star_latLng  起点经纬度
     * @param end_latlng   终点经纬度
     */
    public void launchNavigator(String star_address, LatLng star_latLng, LatLng end_latlng){
        //起点
        String star_add=star_address;
        //终点
        final String[] end_add = {"",""};
        GeoCoder geoCoder = GeoCoder.newInstance();
        // 设置反地理经纬度坐标,请求位置时,需要一个经纬度
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(end_latlng));
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                end_add[0] =geoCodeResult.getAddress();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                end_add[1] =reverseGeoCodeResult.getAddress();
            }
        });
        RouteParaOption routeParaOption=new RouteParaOption();
        routeParaOption.startPoint(star_latLng);
        routeParaOption.startName(star_add);
        routeParaOption.endPoint(end_latlng);
        routeParaOption.endName(end_add[0]);
        try{
            BaiduMapRoutePlan.openBaiduMapWalkingRoute(routeParaOption,context);
        }catch (Exception e){
            e.printStackTrace();
            showDialog();
        }
    }

    /**
     * 根据地址得到经纬度
     * @param city
     * @param address
     * @return
     */
    public LatLng AddressToLatLng(String city, String address){
        final LatLng[] latLng = {null};
        geoCoder.geocode(new GeoCodeOption().city(city).address(address));
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
//                latLng[0] =geoCodeResult.getLocation();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                latLng[0] =reverseGeoCodeResult.getLocation();
            }
        });
        return latLng[0];
    }

    /**
     * 根据经纬度得到地址
     * @param latLng
     * @return
     */
    public String LatLngToAddress(LatLng latLng){
        final String[] address = {""};
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                address[0] =geoCodeResult.getAddress();
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

            }
        });
        return address[0];
    }
    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("您尚未安装百度地图app或app版本过低，点击确认安装？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                OpenClientUtil.getLatestBaiduMapApp(context);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }
}
