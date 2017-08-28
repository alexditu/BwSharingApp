package research.bwsharingapp.sockcomm;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import research.bwsharingapp.iou.IOU_1;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.proto.kb.TrafficInfo;

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

    byte[] pubKeyEnc;
    byte[] privKeyEnc;
    String username;

    public SockCommClient(InetAddress ipAddr, int port, byte[] pubKeyEnc, byte[] privKeyEnc, String username) {
        Log.d(TAG, "SockCommClient: " + ipAddr.getHostAddress() + ":" + port);
        this.port = port;
        this.ipAddr = ipAddr;
        sock = null;

        this.pubKeyEnc  = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.privKeyEnc = Arrays.copyOf(privKeyEnc, privKeyEnc.length);
        this.username = username;
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

    public Integer sendInitMsg() {
        ClientInfo clientInfo = new ClientInfo(username, pubKeyEnc);
        SockCommMsg<ClientInfo> msg = new SockCommMsg<>(MsgType.HELLO, clientInfo);

        try {
            Log.d(TAG, "Sending init msg: " + msg);
            output = new ObjectOutputStream(sock.getOutputStream());
            output.writeObject(msg);
            output.flush();

            Log.d(TAG, "Msg sent. Reading reply");
            input = new ObjectInputStream(sock.getInputStream());
            SockCommMsg<Integer> reply = (SockCommMsg<Integer>)input.readObject();
            Log.d(TAG, "Recv reply: " + reply);

            return reply.getData();

        } catch (IOException e) {
            Log.e(TAG, "sendInitMsg failed: " + e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "sendInitMsg failed: " + e);
            e.printStackTrace();
        }
        return -1;
    }

    public void sendIou() {
        int exceptionCount = 0;

        /* handshake */
        int status = sendInitMsg();
        if (status != 0) {
            Log.e(TAG, "Handshake error, exiting...");
            disconnect();
        }

        Log.d(TAG, "Handshake succeeded!");

        while (!stop) {
            try {
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
            } catch (Exception e) {
                Log.d(TAG, "Exception while sending IOU: " + e);
                exceptionCount++;
                if (exceptionCount == 20) {
                    Log.d(TAG, "Too many exceptions, giving up...");
                    disconnect();
                }
            }
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
