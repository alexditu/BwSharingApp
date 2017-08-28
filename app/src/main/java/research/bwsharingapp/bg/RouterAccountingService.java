package research.bwsharingapp.bg;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import research.bwsharingapp.bg.pojo.ServiceInfo;
import research.bwsharingapp.sockcomm.CommConstants;
import research.bwsharingapp.sockcomm.SockCommServer;

/**
 * Created by alex on 5/27/17.
 */

public class RouterAccountingService extends AccountingService {
    private final static String TAG = "RouterAccountingService";

    private ServiceInfo kb;
    private SockCommServer server;

    @Override
    protected void startAccounting(ServiceInfo kb, byte[] pubKeyEnc, byte[] privKeyEnc, String username) {
        this.kb = kb;
        Log.d(TAG, "startAccounting: " + kb);
        try {
            startCommServer(pubKeyEnc, privKeyEnc, username);
        } catch (Exception e) {
            Log.e(TAG, "UDP server exception: " + e);
        }
    }

    private void startCommServer(byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws UnknownHostException {
        InetAddress ip  = InetAddress.getByName(kb.getRouterIp());
        int port        = Integer.parseInt(kb.getRouterPort());

        server = new SockCommServer(ip, port, pubKeyEnc, privKeyEnc, username);
        Log.d(TAG, "Starting SockCommServer: " + server);

        if (server.start()) {
            Log.d(TAG, "Starting SockCommServer succeeded!");
        } else {
            Log.d(TAG, "Starting SockCommServer failed!");
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy()", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Stopping SockCommServer: " + server);
        boolean ret = server.stop();
        if (ret == true) {
            Log.d(TAG, "Stopping SockCommServer succeeded!");
        } else {
            Log.d(TAG, "Stopping SockCommServer failed!");
        }
    }


//    // NOT USED!
//    private void startUdpServer() throws IOException {
//        int port = Integer.parseInt(kb.getRouterPort());
//        InetAddress serverAddr = InetAddress.getByName(kb.getRouterIp());
//
//
//        DatagramSocket serverSock = new DatagramSocket(port, serverAddr);
//
//        byte[] buf = new byte[1024];
//        DatagramPacket p = new DatagramPacket(buf, 1024);
//        while (true) {
//            Log.d(TAG, "Waiting for data...");
//            serverSock.receive(p);
//
//            String s = new String(p.getData());
//            Log.d(TAG, "Recv: " + s);
//        }
//
//    }
}
