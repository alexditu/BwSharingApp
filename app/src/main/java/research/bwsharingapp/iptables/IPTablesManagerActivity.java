package research.bwsharingapp.iptables;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import research.bwsharingapp.R;

import static research.bwsharingapp.MainActivity.CLIENT_ID;
import static research.bwsharingapp.p2p.P2PMainActivity.CLIENT_IP;
import static research.bwsharingapp.p2p.P2PMainActivity.ROUTER_IP;

public class IPTablesManagerActivity extends AppCompatActivity {
    private final static String TAG = "IPTablesManagerActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iptables_manager);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        Button btn = (Button) findViewById(R.id.print_in_out_stats_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Input stats:");
                    IPTablesManager.printInputStats(CLIENT_ID);
                    Log.d(TAG, "Output stats:");
                    IPTablesManager.printOutputStats(CLIENT_ID);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.print_fw_stats_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Forward stats:");
                    IPTablesManager.printForwardStats(CLIENT_ID);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.set_in_out_rules_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Setting client rules");
                    IPTablesManager.setClientIptablesRules(ROUTER_IP, CLIENT_ID);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.set_fw_rules_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Setting router rules");
                    IPTablesManager.setRouterIptablesRules(CLIENT_IP, CLIENT_ID);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.zero_in_stats_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Clear input stats");
                    IPTablesManager.clearInputStats();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.zero_out_stats_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Clear output stats");
                    IPTablesManager.clearOutputStats();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.zero_fw_stats_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Clear fw stats");
                    IPTablesManager.clearForwardStats();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.del_in_out_rules_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Delete IN/OUT rules");
                    IPTablesManager.deleteClientIptablesRules(ROUTER_IP, CLIENT_ID);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

        btn = (Button) findViewById(R.id.del_fw_rules_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Delete FW rules");
                    IPTablesManager.deleteRouterIptablesRules(CLIENT_IP, CLIENT_ID);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });



        btn = (Button) findViewById(R.id.client_enable_net_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Enabling Client Network");
                    IPTablesManager.clientEnableNetworking(ROUTER_IP);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });
        btn = (Button) findViewById(R.id.router_enable_net_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Enabling Router Network");
                    IPTablesManager.routerEnableNetworking();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to print status: " + e);
                }
            }
        });

    }
}
