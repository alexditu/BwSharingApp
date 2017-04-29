package research.bwsharingapp.p2p;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import research.bwsharingapp.R;

/**
 * Created by alex on 1/17/17.
 */

public class P2PMainActivity extends AppCompatActivity {
    private final String TAG = "P2PMainActivity";

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private P2PReceiver receiver;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private DevicesAdapter devicesAdapter;
    private ListView peerListView;

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);

                // If an AdapterView is backed by this data, notify it
                // of the change.  For instance, if you have a ListView of
                // available peers, trigger an update.
//                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
                Log.d(TAG, "Peer list updated");
                // Perform any other updates needed based on the new list of
                // peers connected to the Wi-Fi P2P network.
                //devicesAdapter.updateDataSource(peers.toArray(new WifiP2pDevice[]{}));
            }

            if (peers.size() == 0) {
                Log.d(TAG, "No devices found");
                return;
            }

            Log.d(TAG, "Discovered devices: " + refreshedPeers.size());
            for (WifiP2pDevice d : refreshedPeers) {
                Log.d(TAG, d.toString());
            }

            devicesAdapter.updateDataSource(refreshedPeers);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p2p_activity_main);

        init();
        addIntentFilters();
        setOnClickListeners();
        setPeersListView();


    }

    public void init() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        receiver = new P2PReceiver(mManager, mChannel, this, peerListListener);
    }

    public void addIntentFilters() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void peerDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
                Log.d(TAG, "peerDiscovery success");
            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                Log.d(TAG, "peerDiscovery failure: " + reasonCode);
            }
        });
    }

    public void connectToPeer(WifiP2pDevice device) {

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                Log.d(TAG, "connectToPeer success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "connectToPeer failure");
                Toast.makeText(null, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setOnClickListeners() {
        Button peerDiscoveryBtn = (Button) findViewById(R.id.peer_discovery_btn);
        peerDiscoveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                peerDiscovery();
            }
        });

        Button peerConnectBtn = (Button) findViewById(R.id.peer_connect_btn);
        peerConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object device = devicesAdapter.getItem(0);

                if (device != null) {
                    connectToPeer((WifiP2pDevice) device);
                } else {
                    Log.d(TAG, "No devices discovered!");
                    Toast.makeText(null, "No devices discovered! Retry.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void setPeersListView() {
        peerListView = (ListView) findViewById(R.id.peer_list_lv);
        devicesAdapter = new DevicesAdapter(this);
        peerListView.setAdapter(devicesAdapter);

        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device = (WifiP2pDevice) devicesAdapter.getItem(position);
                Log.d(TAG, "Clicked on device: " + device.deviceName);

            }
        });
    }
}
