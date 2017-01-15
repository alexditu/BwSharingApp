package research.bwsharingapp;

import android.content.Context;
import android.net.wifi.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        CountManager mgr = new CountManager();
//        mgr.runIpTables();

        Context context = this.getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiAPController wifiAPController  = new WifiAPController();
        wifiAPController.wifiToggle("mHotspot", "12345678", wifiManager, context);
    }
}
