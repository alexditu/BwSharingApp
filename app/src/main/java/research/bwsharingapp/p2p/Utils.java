package research.bwsharingapp.p2p;

import android.net.wifi.p2p.WifiP2pManager;

import static android.net.wifi.p2p.WifiP2pManager.P2P_UNSUPPORTED;

/**
 * Created by alex on 4/29/17.
 */

public class Utils {
    public static String getPeerDiscoveryErrorString(int errorCode) {
        if (errorCode == WifiP2pManager.P2P_UNSUPPORTED)
            return "P2P_UNSUPPORTED";
        else if (errorCode == WifiP2pManager.ERROR)
            return "ERROR";
        else if (errorCode == WifiP2pManager.BUSY)
            return "BUSY";

        return "Unknow error code: " + errorCode;
    }
}
