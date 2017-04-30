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

    public DeviceInfo() {
        device = null;
        isConnected = false;
        ipAddr = null;
        clients = null;
    }

    public DeviceInfo(WifiP2pDevice device, boolean isConnected, InetAddress ipAddr, List<WifiP2pDevice> clients, boolean isGroupOwner) {
        this.device = device;
        this.isConnected = isConnected;
        this.ipAddr = ipAddr;
        this.clients = clients;
        this.isGroupOwner = isGroupOwner;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public InetAddress getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(InetAddress ipAddr) {
        this.ipAddr = ipAddr;
    }

    public List<WifiP2pDevice> getClients() {
        return clients;
    }

    public void setClients(List<WifiP2pDevice> clients) {
        this.clients = clients;
    }

    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }
}
