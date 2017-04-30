package research.bwsharingapp.p2p;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

    private DevicesAdapter devicesAdapter;
    private ListView peerListView;


    private List<WifiP2pDevice> crtGroup;
    private WifiP2pDevice crtDevice;

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();

            if (refreshedPeers.size() == 0) {
                Log.d(TAG, "No devices found");
            } else {
                Log.d(TAG, "Discovered devices: " + refreshedPeers.size());
                for (WifiP2pDevice d : refreshedPeers) {
                    Log.d(TAG, d.toString());
                }
            }
            clearDevicesList();
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
        setConnectionStatus(0, null);


    }

    public void init() {
        crtDevice = null;
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
                Log.d(TAG, "peerDiscovery failure: " + Utils.getPeerDiscoveryErrorString(reasonCode));
            }
        });
        clearDevicesList();
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

        Button connInfoBtn = (Button) findViewById(R.id.conn_info_btn);
        connInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status();
            }
        });

        Button disconnectBtn = (Button) findViewById(R.id.disconnect_btn);
        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        Button createGroupBtn = (Button) findViewById(R.id.create_group_btn);
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Creating Group...");
                mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Device is ready to accept incoming connections from peers.
                        Log.d(TAG, "Group created successfully!");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "P2P group creation failed. Reason: " + reason);
                    }
                });
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
                Log.d(TAG, "Connecting to device: " + device.deviceName);
                connectToPeer(device);
            }
        });
    }

    private void status() {
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info == null) {
                    Log.d(TAG, "Connection info not available yet");
                    return;
                }

                Log.d(TAG, "Connection Info:");
                Log.d(TAG, "groupFormed: " + info.groupFormed);
                Log.d(TAG, "isGroupOwner: " + info.isGroupOwner);
                Log.d(TAG, "groupOwnerAddress: " + info.groupOwnerAddress);
                Log.d(TAG, "");
            }
        });

        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group == null) {
                    Log.d(TAG, "Group info not available yet");
                    return;
                }

                Log.d(TAG, "Group info: " + group);
                String devices = "";
                Collection<WifiP2pDevice> clientList = group.getClientList();
                for (WifiP2pDevice i : clientList) {
                    devices += i.deviceName + ", ";
                }
                Log.d(TAG, "Devices in group ( " + clientList.size() + " ): " + devices);
                Log.d(TAG, "Interface: " + group.getInterface());
                Log.d(TAG, "NetworkName: " + group.getNetworkName());
                Log.d(TAG, "Group owner: " + group.getOwner().deviceName);
                Log.d(TAG, "Passphrase: " + group.getPassphrase());
                Log.d(TAG, "");
            }
        });
    }

    private void disconnect() {
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Group removed successfully");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Group remove failed with reason: " + reason);
            }
        });
    }

    public void setConnectionStatus(int status, WifiP2pDevice device) {
        TextView t = (TextView) findViewById(R.id.conn_status_tv);
        String baseText = "<bold>Status:</bold> ";
        switch(status) {
            case 0:
                // Disconnected
                t.setText(Html.fromHtml(baseText + "DISCONNECTED"));
                clearGroupInfoStatus();
                break;
            case 1:
                // Connected
                t.setText(Html.fromHtml(baseText + "CONNECTED"));
                setGroupInfoStatus();
                break;
            default:
                Log.d(TAG, "Unknown state!");
                break;
        }
    }

    private void setGroupInfoStatus() {
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group == null) {
                    Log.d(TAG, "Group info not available yet");
                    return;
                }

                TextView ipInfo = (TextView) findViewById(R.id.crt_ip_addr_tv);
                String ipAddr = Utils.getIPAddress(group.getInterface());
                ipInfo.setText("IP: " + ipAddr);

                TextView groupDevices = (TextView) findViewById(R.id.conn_dev_list_tv);
                String devices = "Connected devices:\n";
                for (WifiP2pDevice i : group.getClientList()) {
                    devices += "\t\t" + i.deviceName + "\n";
                }
                groupDevices.setText(devices);
            }
        });
    }

    private void clearGroupInfoStatus() {
        TextView groupDevices = (TextView) findViewById(R.id.conn_dev_list_tv);
        groupDevices.setText("");

        TextView ipInfo = (TextView) findViewById(R.id.crt_ip_addr_tv);
        ipInfo.setText("");
    }

    public void clearDevicesList() {
        devicesAdapter.clearData();
    }

    public WifiP2pDevice getCrtDevice() {
        return crtDevice;
    }

    public void setCrtDevice(WifiP2pDevice crtDevice) {
        this.crtDevice = crtDevice;
        if (crtDevice != null) {
            TextView tv = (TextView) findViewById(R.id.crt_dev_info_tv);
            tv.setText("Device info: " + crtDevice.deviceName + "  [" + crtDevice.deviceAddress + "]");
        }
    }
}
