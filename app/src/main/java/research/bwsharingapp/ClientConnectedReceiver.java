package research.bwsharingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by alex on 1/17/17.
 */

public class ClientConnectedReceiver extends BroadcastReceiver {
    private static String TAG = "ClientConnectedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Client connected");

        Toast.makeText(context, "Client connected", Toast.LENGTH_LONG).show();
    }
}
