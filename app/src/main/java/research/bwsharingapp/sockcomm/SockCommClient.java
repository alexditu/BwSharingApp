package research.bwsharingapp.sockcomm;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

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
        SockCommMsg<Void> msg = new SockCommMsg<>(0, null);
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

    public void disconnect() {

    }
}
