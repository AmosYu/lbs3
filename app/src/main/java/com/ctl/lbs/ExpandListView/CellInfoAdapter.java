package com.ctl.lbs.ExpandListView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctl.lbs.R;
import com.ctl.lbs.cell.BtsType;
import com.ctl.lbs.cell.LuceCellInfo;

import java.util.List;



/**
 * Created by yuxingyu on 16/5/15.
 */
public class CellInfoAdapter extends CommonAdapter<LuceCellInfo> {

    public static int flag_six;
    public static int flag_ci;
    public static int mode;
    public CellInfoAdapter(Context context, List<LuceCellInfo> datas, int layoutId)
    {
        super(context, datas, layoutId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.extend_listview_item, position);

        LuceCellInfo cellInfo = mDatas.get(position);

        TextView lacTv = (TextView)holder.getView(R.id.tv_lac);
        lacTv.setText(cellInfo.getLacStr());

        TextView ciTv = (TextView)holder.getView(R.id.tv_ci);
        ciTv.setText(cellInfo.getCiStr());



        ((TextView)holder.getView(R.id.tv_arfcn)).setText(String.valueOf(cellInfo.getArfcnA()));
        ((TextView)holder.getView(R.id.tv_pci)).setText(String.valueOf(cellInfo.getArfcnB()));
        ((TextView)holder.getView(R.id.tv_rssi)).setText(String.valueOf(cellInfo.getRssi()));
        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, LuceCellInfo cellInfo)
    {
        holder
                .setText(R.id.tv_arfcn, String.valueOf(cellInfo.getArfcnA()))
                .setText(R.id.tv_pci, String.valueOf(cellInfo.getArfcnB()))
                .setText(R.id.tv_rssi, String.valueOf(cellInfo.getRssi()))
                .setText(R.id.tv_lac, String.valueOf(cellInfo.getLac()))
                .setText(R.id.tv_ci, String.valueOf(cellInfo.getCellId()));
    }
}
