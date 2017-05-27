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

public class ClientAccountingService extends AccountingService {
    private final static String TAG = "ClientAccountingService";

    public static final String KB_INFO_TAG  = "kibbutz_info";

    private ServiceInfo kb;


    @Override
    protected void startAccounting(ServiceInfo kb) {
        Log.d(TAG, "startAccounting: " + kb);
        this.kb = kb;

        try {
            sendData();
        } catch (Exception e) {
            Log.d(TAG, "sendData exception: " + e);
        }
    }

    private void sendData() throws IOException, InterruptedException {
        byte buf[] = new byte[1024];
        byte data[] = new String("ana").getBytes();

        System.arraycopy(data, 0, buf, 0, 3);

        int port = Integer.parseInt(kb.getRouterPort());
        InetAddress serverAddr = InetAddress.getByName(kb.getRouterIp());
        DatagramSocket s = new DatagramSocket();

        DatagramPacket p = new DatagramPacket(buf, 1024, serverAddr, port);

        int count = 0;
        while (count < 10 && !stopService) {
            Log.d(TAG, "Sending packet: " + count);
            s.send(p);
            Log.d(TAG, "Done Sending packet: " + count);
            count++;
            Thread.sleep(1000);
        }

    }
}
