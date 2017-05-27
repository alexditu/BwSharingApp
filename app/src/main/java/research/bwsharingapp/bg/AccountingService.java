package research.bwsharingapp.bg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import research.bwsharingapp.bg.pojo.ServiceInfo;

import static research.bwsharingapp.bg.ClientAccountingService.KB_INFO_TAG;

/**
 * Created by alex on 5/27/17.
 */

public class AccountingService extends Service {
    private final static String TAG = "AccountingService";

    protected boolean stopService = false;

    @Override
    public void onCreate() {
        Toast.makeText(this, TAG + " onCreate()", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);

        final ServiceInfo kb = (ServiceInfo) intent.getSerializableExtra(KB_INFO_TAG);
        Thread t = new Thread() {
            @Override
            public void run() {
                startAccounting(kb);
            }
        };
        t.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy()", Toast.LENGTH_SHORT).show();
        stopService = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void startAccounting(ServiceInfo kb) {
        Log.d(TAG, "startAccounting");
    }

}
