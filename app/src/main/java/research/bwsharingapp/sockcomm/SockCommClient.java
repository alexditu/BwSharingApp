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

    public SockCommClient(int port, InetAddress ipAddr) {
        this.port = port;
        this.ipAddr = ipAddr;
        sock = null;
    }

    public boolean connect() {
        boolean connected = false;
        try {
            sock = new Socket(ipAddr, port);
            initStreams();
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connected;
    }

    private void initStreams() throws IOException {
        input = new ObjectInputStream(sock.getInputStream());
        output = new ObjectOutputStream(sock.getOutputStream());
    }

    public void sendInitMsg() {
        SockCommMsg<Void> msg = new SockCommMsg<>(0, null);
        try {
            Log.d(TAG, "Sending init msg: " + msg);
            output.writeObject(msg);
            output.flush();

            try {
                SockCommMsg<String> reply = (SockCommMsg<String>)input.readObject();
                Log.d(TAG, "Recv reply: " + reply);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {

    }
}
