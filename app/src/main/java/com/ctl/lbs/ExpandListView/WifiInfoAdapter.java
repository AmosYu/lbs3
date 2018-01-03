package com.ctl.lbs.ExpandListView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctl.lbs.R;
import com.ctl.lbs.cell.WifiInfo;

import java.util.List;


public class WifiInfoAdapter extends CommonAdapter<WifiInfo> {


    public WifiInfoAdapter(Context context, List<WifiInfo> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.wifi_item, position);
        WifiInfo wifiInfo = mDatas.get(position);
        ((TextView)holder.getView(R.id.wifi_type)).setText(wifiInfo.getType());
        ((TextView)holder.getView(R.id.wifi_bssid)).setText(wifiInfo.getMac());

        ((TextView)holder.getView(R.id.wifi_chanel)).setText(wifiInfo.getChanel());
        ((TextView)holder.getView(R.id.wifi_rssi)).setText(wifiInfo.getRssi());


        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, WifiInfo wifiInfo) {

        holder.setText(R.id.wifi_type, wifiInfo.getType())
                .setText(R.id.wifi_bssid, wifiInfo.getMac())
                .setText(R.id.wifi_chanel,wifiInfo.getChanel())
                .setText(R.id.wifi_rssi, wifiInfo.getRssi());
    }
}
