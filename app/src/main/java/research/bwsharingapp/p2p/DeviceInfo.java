package research.bwsharingapp.p2p;

import android.net.wifi.p2p.WifiP2pDevice;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by alex on 4/30/17.
 */

public class DeviceInfo {
    private WifiP2pDevice device;
    private boolean isConnected;
    private InetAddress ipAddr;
    private List<WifiP2pDevice> clients;

    private boolean isGroupOwner;

    
}
