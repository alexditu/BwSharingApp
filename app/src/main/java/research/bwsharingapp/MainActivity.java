package research.bwsharingapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import research.bwsharingapp.iptables.IPTablesManagerActivity;
import research.bwsharingapp.p2p.P2PMainActivity;
import research.bwsharingapp.p2p.P2PReceiver;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    public final static String CLIENT_ID = "C";
    public final static String ROUTER_ID = "R";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        CountManager mgr = new CountManager();
//        mgr.runIpTables();

        addOnClickListeners();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        this.registerReceiver(new ClientConnectedReceiver(), filter);
//
//        enableHotspot();
    }

    public void enableHotspot() {
        Context context = this.getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiAPController wifiAPController  = new WifiAPController();
        wifiAPController.wifiToggle("mHotspot", "12345678", wifiManager, context);
    }

    public void addOnClickListeners() {
        Button configWifiP2P = (Button) findViewById(R.id.wifi_p2p_btn);
        final Intent intent = new Intent(this, P2PMainActivity.class);
        configWifiP2P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });

        Button startIPTables = (Button) findViewById(R.id.iptables_act_btn);
        final Intent iptables = new Intent(this, IPTablesManagerActivity.class);
        startIPTables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(iptables);
            }
        });
    }



}
