package research.bwsharingapp.bg;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import research.bwsharingapp.bg.pojo.ServiceInfo;

/**
 * Created by alex on 5/27/17.
 */

public class RouterAccountingService extends AccountingService {
    private final static String TAG = "RouterAccountingService";

    public static final String KB_INFO_TAG  = "kibbutz_info";
    private ServiceInfo kb;

    @Override
    protected void startAccounting(ServiceInfo kb) {
        this.kb = kb;
        Log.d(TAG, "startAccounting: " + kb);
        try {
            startUdpServer();
        } catch (Exception e) {
            Log.e(TAG, "UDP server exception: " + e);
        }
    }

    private void startUdpServer() throws IOException {
        int port = Integer.parseInt(kb.getRouterPort());
        InetAddress serverAddr = InetAddress.getByName(kb.getRouterIp());


        DatagramSocket serverSock = new DatagramSocket(port, serverAddr);

        byte[] buf = new byte[1024];
        DatagramPacket p = new DatagramPacket(buf, 1024);
        while (true) {
            Log.d(TAG, "Waiting for data...");
            serverSock.receive(p);

            String s = new String(p.getData());
            Log.d(TAG, "Recv: " + s);
        }

    }
}
