package research.bwsharingapp.sockcomm;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import research.bwsharingapp.iou.IOU_1;
import research.bwsharingapp.iptables.ExecFailedException;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.iptables.IPTablesParserException;
import research.bwsharingapp.iptables.TrafficInfo;

import static research.bwsharingapp.MainActivity.CLIENT_ID;

/**
 * Created by alex on 4/30/17.
 */

public class SockCommClient {
    private final static String TAG = "SockCommServer";

    private int port;
    private InetAddress ipAddr;
    private Socket sock;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;

    private boolean stop = false;

    public SockCommClient(InetAddress ipAddr, int port) {
        Log.d(TAG, "SockCommClient: " + ipAddr.getHostAddress() + ":" + port);
        this.port = port;
        this.ipAddr = ipAddr;
        sock = null;
    }

    public boolean connect() {
        boolean connected = false;
        try {
            sock = new Socket(ipAddr, port);
            connected = true;
            Log.d(TAG, "Connection success!");
        } catch (IOException e) {
            Log.e(TAG, "Connection failed: " + e);
            e.printStackTrace();
        }
        return connected;
    }

    public void sendInitMsg() {
        SockCommMsg<Void> msg = new SockCommMsg<>(MsgType.HELLO, null);
        try {
            Log.d(TAG, "Sending init msg: " + msg);
            output = new ObjectOutputStream(sock.getOutputStream());
            output.writeObject(msg);
            output.flush();

            Log.d(TAG, "Msg sent. Reading reply");
            input = new ObjectInputStream(sock.getInputStream());
            SockCommMsg<String> reply = (SockCommMsg<String>)input.readObject();
            Log.d(TAG, "Recv reply: " + reply);

        } catch (IOException e) {
            Log.e(TAG, "sendInitMsg failed: " + e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "sendInitMsg failed: " + e);
            e.printStackTrace();
        }
    }

    public void sendIou() {
        try {

            while (!stop) {
                TrafficInfo in = IPTablesManager.getInputStats(CLIENT_ID);
                TrafficInfo out = IPTablesManager.getOutputStats(CLIENT_ID);
                IOU_1 iou = new IOU_1(in, out);
                SockCommMsg<IOU_1> msg = new SockCommMsg<>(MsgType.IOU_1, iou);

                Log.d(TAG, "Sending IOU_1 msg: " + msg);
                output = new ObjectOutputStream(sock.getOutputStream());
                output.writeObject(msg);
                output.flush();
                Log.d(TAG, "Msg sent.");

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception while sending IOU: " + e);
        }
    }

    public void disconnect() {
        try {
            stop = true;
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
