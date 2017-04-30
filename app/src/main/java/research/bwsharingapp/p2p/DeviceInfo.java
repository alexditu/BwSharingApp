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
    private boolean isGroupOwner;
    private InetAddress ipAddr;
    private InetAddress groupOwnerAddress;

    private List<WifiP2pDevice> clients;



    public DeviceInfo() {
        device = null;
        isConnected = false;
        ipAddr = null;
        clients = null;
    }

    public DeviceInfo(WifiP2pDevice device, boolean isConnected, InetAddress ipAddr, InetAddress groupOwnerAddress, boolean isGroupOwner, List<WifiP2pDevice> clients) {
        this.device = device;
        this.isConnected = isConnected;
        this.ipAddr = ipAddr;
        this.groupOwnerAddress = groupOwnerAddress;
        this.isGroupOwner = isGroupOwner;
        this.clients = clients;
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

    public InetAddress getGroupOwnerAddress() {
        return groupOwnerAddress;
    }

    public void setGroupOwnerAddress(InetAddress groupOwnerAddress) {
        this.groupOwnerAddress = groupOwnerAddress;
    }
}
