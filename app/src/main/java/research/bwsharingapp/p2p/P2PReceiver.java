package research.bwsharingapp.p2p;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import research.bwsharingapp.R;

/**
 * Created by alex on 1/17/17.
 */

public class P2PReceiver extends BroadcastReceiver {
    private final String TAG = "P2PReceiver";
    private P2PMainActivity activity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener peerListListener;

    public P2PReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, P2PMainActivity activity, WifiP2pManager.PeerListListener peerListListener) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.activity = activity;
        this.peerListListener = peerListListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                activity.setIsWifiP2pEnabled(true);
                Log.d(TAG, "activity.setIsWifiP2pEnabled(true)");
            } else {
//                activity.setIsWifiP2pEnabled(false);
                Log.d(TAG, "activity.setIsWifiP2pEnabled(false)");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
            // The peer list has changed!  We should probably do something about
            // that.
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
                activity.clearDevicesList();
            }


        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Log.d(TAG, "WiFi Direct CONNECTED: " + networkInfo);
                activity.setConnectionStatus(1, null);

                // We are connected with the other device, request connection
                // info to find group owner IP

//                mManager.requestConnectionInfo(mChannel, connectionListener);
            } else {
                Log.d(TAG, "WiFi Direct DISCONNECTED: " + networkInfo);
                activity.setConnectionStatus(0, null);
            }

            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

            WifiP2pDevice device =
                    (WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(TAG, "Device status: " + device);

            WifiP2pDevice crtDevice = activity.getCrtDevice();
            if (crtDevice == null) {
                activity.setCrtDevice(device);
                Log.d(TAG, "Current device info set. Name: " + device.deviceName);
            } else {
                if (crtDevice.equals(device)) {
                    Log.d(TAG, "Current device has not changed");
                } else {
                    Log.d(TAG, "Updating current device");
                    activity.setCrtDevice(device);
                }
            }

        }
    }
}
