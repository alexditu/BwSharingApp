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
import java.util.Collection;
import java.util.List;

import research.bwsharingapp.R;

/**
 * Created by alex on 1/17/17.
 */

public class DevicesAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<WifiP2pDevice> mDataSource;

    public DevicesAdapter(Context context) {
        mContext = context;
        mDataSource = new ArrayList<>();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateDataSource(Collection<WifiP2pDevice> newDataSource) {
        mDataSource.clear();
        for (WifiP2pDevice d : newDataSource) {
            mDataSource.add(d);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        mDataSource.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mDataSource.get(i).deviceAddress.hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = mInflater.inflate(R.layout.peer_item, viewGroup, false);
        TextView tv = (TextView) rowView.findViewById(R.id.peer_item_tv);
        tv.setText(mDataSource.get(i).deviceName);
        return rowView;
    }




}
