package research.bwsharingapp.p2p;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import static android.net.wifi.p2p.WifiP2pManager.P2P_UNSUPPORTED;

/**
 * Created by alex on 4/29/17.
 */

public class Utils {
    private final static String TAG = "Utils";

    public static String getPeerDiscoveryErrorString(int errorCode) {
        if (errorCode == WifiP2pManager.P2P_UNSUPPORTED)
            return "P2P_UNSUPPORTED";
        else if (errorCode == WifiP2pManager.ERROR)
            return "ERROR";
        else if (errorCode == WifiP2pManager.BUSY)
            return "BUSY";

        return "Unknow error code: " + errorCode;
    }

    public static String getIPAddress(String interfaceName) {
        NetworkInterface iface = null;
        String addr = "";

        try {
            iface = NetworkInterface.getByName(interfaceName);
            for (InetAddress i : Collections.list(iface.getInetAddresses())) {
                addr = i.getHostAddress();
                boolean isIPv4 = addr.indexOf(':') < 0;
                if (isIPv4) {
                    return addr;
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "SocketException while searching for interface: " + interfaceName);
        }

        Log.e(TAG, "No IPv4 address found for interface: " + interfaceName);
        return addr;

    }
}
