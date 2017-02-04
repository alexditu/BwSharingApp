package research.bwsharingapp.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import research.bwsharingapp.R;

/**
 * Created by alex on 1/17/17.
 */

public class DevicesAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private WifiP2pDevice mDataSource[];

    public DevicesAdapter(Context context, WifiP2pDevice mDataSource[]) {
        mContext = context;
        this.mDataSource = mDataSource;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateDataSource(WifiP2pDevice mDataSource[]) {
        this.mDataSource = mDataSource;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataSource.length;
    }

    @Override
    public Object getItem(int i) {
        return mDataSource[i];
    }

    @Override
    public long getItemId(int i) {
        return mDataSource[i].deviceAddress.hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.peer_item, viewGroup, false);
        TextView tv = (TextView) rowView.findViewById(R.id.peer_item_tv);
        tv.setText(mDataSource[i].deviceName);
        return null;
    }
}
