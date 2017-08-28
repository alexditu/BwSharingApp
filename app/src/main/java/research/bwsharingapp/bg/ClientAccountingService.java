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
import research.bwsharingapp.sockcomm.SockCommClient;

/**
 * Created by alex on 5/27/17.
 */

public class ClientAccountingService extends AccountingService {
    private final static String TAG = "ClientAccountingService";



    private ServiceInfo kb;
    private SockCommClient client;

    @Override
    protected void startAccounting(ServiceInfo kb, byte[] pubKeyEnc, byte[] privKeyEnc, String username) {
        Log.d(TAG, "startAccounting: " + kb);
        this.kb = kb;

        try {
            startCommClient(pubKeyEnc, privKeyEnc, username);
        } catch (Exception e) {
            Log.d(TAG, "sendData exception: " + e);
        }
    }



    private void startCommClient(byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws UnknownHostException {
        InetAddress ip  = InetAddress.getByName(kb.getRouterIp());
        int port        = Integer.parseInt(kb.getRouterPort());

        client = new SockCommClient(ip, port, pubKeyEnc, privKeyEnc, username);
        boolean connected = client.connect();

        if (connected) {
            client.sendIou();
        } else {
            Log.e(TAG, "Client cannot connect to server!");
            Toast.makeText(this, "Client cannot connect to server!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy()", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Stopping sockCommClient: " + client);
        client.disconnect();
    }


//    private void sendData() throws IOException, InterruptedException {
//        byte buf[] = new byte[1024];
//        byte data[] = new String("ana").getBytes();
//
//        System.arraycopy(data, 0, buf, 0, 3);
//
//        int port = Integer.parseInt(kb.getRouterPort());
//        InetAddress serverAddr = InetAddress.getByName(kb.getRouterIp());
//        DatagramSocket s = new DatagramSocket();
//
//        DatagramPacket p = new DatagramPacket(buf, 1024, serverAddr, port);
//
//        int count = 0;
//        while (count < 10 && !stopService) {
//            Log.d(TAG, "Sending packet: " + count);
//            s.send(p);
//            Log.d(TAG, "Done Sending packet: " + count);
//            count++;
//            Thread.sleep(1000);
//        }
//
//    }
}
