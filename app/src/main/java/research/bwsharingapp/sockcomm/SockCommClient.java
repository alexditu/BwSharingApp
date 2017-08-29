package research.bwsharingapp.sockcomm;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import research.bwsharingapp.account.PKIManager;
import research.bwsharingapp.sockcomm.msg.ClientIOUSigned;
import research.bwsharingapp.sockcomm.msg.IOU_1;
import research.bwsharingapp.iptables.ExecFailedException;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.iptables.IPTablesParserException;
import research.bwsharingapp.proto.kb.TrafficInfo;
import research.bwsharingapp.sockcomm.msg.ClientInfo;
import research.bwsharingapp.sockcomm.msg.HelloReply;
import research.bwsharingapp.sockcomm.msg.MsgType;
import research.bwsharingapp.sockcomm.msg.SockCommMsg;

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

    private byte[] pubKeyEnc;
//    private byte[] privKeyEnc;
    private PrivateKey privKey;
    private String username;

    private byte[] nonce;

    public SockCommClient(InetAddress ipAddr, int port, byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Log.d(TAG, "SockCommClient: " + ipAddr.getHostAddress() + ":" + port);
        this.port = port;
        this.ipAddr = ipAddr;
        sock = null;

        this.pubKeyEnc  = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.privKey = PKIManager.convertPrivateKeyBytes(privKeyEnc);
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

    private void sendMsg(SockCommMsg msg) throws IOException {
        output = new ObjectOutputStream(sock.getOutputStream());
        output.writeObject(msg);
        output.flush();
    }

    private Object readMsg() throws IOException, ClassNotFoundException {
        input = new ObjectInputStream(sock.getInputStream());
        return input.readObject();
    }

    public int sendInitMsg() {
        ClientInfo clientInfo = new ClientInfo(username, pubKeyEnc);
        SockCommMsg<ClientInfo> msg = new SockCommMsg<>(MsgType.HELLO, clientInfo);

        try {
            Log.d(TAG, "Sending init msg: " + msg);
            sendMsg(msg);

            Log.d(TAG, "Msg sent. Reading reply");
            SockCommMsg<HelloReply> reply = (SockCommMsg<HelloReply>) readMsg();
            Log.d(TAG, "Recv reply: " + reply);

            if (reply.getData().getStatusCode() == 0) {
                // connection succeeded, save nonce
                nonce = Arrays.copyOf(reply.getData().getNonce(), reply.getData().getNonce().length);
            }

            return reply.getData().getStatusCode();

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

                ClientIOUSigned iou = generateIOU();
                SockCommMsg<ClientIOUSigned> msg = new SockCommMsg<>(MsgType.IOU_1, iou);

                Log.d(TAG, "Sending IOU_1 msg: " + msg);
                sendMsg(msg);
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

    private ClientIOUSigned generateIOU() throws ExecFailedException, IOException, IPTablesParserException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        TrafficInfo in = IPTablesManager.getInputStats(CLIENT_ID);
        TrafficInfo out = IPTablesManager.getOutputStats(CLIENT_ID);
        IOU_1 iou = new IOU_1(in, out, nonce);
        byte[] sign = PKIManager.signData(iou.toBytes(), privKey);

        ClientIOUSigned signedIOU = new ClientIOUSigned(iou, sign);

        return signedIOU;
    }
}
