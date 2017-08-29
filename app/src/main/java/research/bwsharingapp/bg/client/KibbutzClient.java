package research.bwsharingapp.bg.client;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import research.bwsharingapp.account.PKIManager;
import research.bwsharingapp.iptables.ExecFailedException;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.iptables.IPTablesParserException;
import research.bwsharingapp.proto.kb.ClientConnectReply;
import research.bwsharingapp.proto.kb.ClientIOU;
import research.bwsharingapp.proto.kb.ClientIOUReply;
import research.bwsharingapp.proto.kb.ClientIOUSigned;
import research.bwsharingapp.proto.kb.ClientInfo;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.proto.kb.KibbutzRouterGrpc;
import research.bwsharingapp.proto.kb.TrafficInfo;
import research.bwsharingapp.sockcomm.CommConstants;
import research.bwsharingapp.sockcomm.msg.HelloReply;
import research.bwsharingapp.sockcomm.msg.MsgType;
import research.bwsharingapp.sockcomm.msg.SockCommMsg;

import static research.bwsharingapp.MainActivity.CLIENT_ID;
import static research.bwsharingapp.bg.router.Utils.toBytes;

/**
 * Created by alex on 8/30/17.
 */

public class KibbutzClient {
    private final static String TAG = "SockCommServer";

    //TODO: send values via constructor
    public final static String KIBBUTZ_ROUTER_IP = "192.168.56.101";
    public final static int KIBBUTZ_ROUTER_PORT = 50051;

    private int port;
    private InetAddress ipAddr;


    private ManagedChannel mChannel = null;
    private KibbutzRouterGrpc.KibbutzRouterBlockingStub stub = null;

    private boolean stop = false;

    private byte[] pubKeyEnc;
    private PrivateKey privKey;
    private String username;

    private byte[] nonce;
    private ByteString nonceByteStr;

    public KibbutzClient(InetAddress ipAddr, int port, byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Log.d(TAG, "SockCommClient: " + ipAddr.getHostAddress() + ":" + port);
        this.port = port;
        this.ipAddr = ipAddr;

        this.pubKeyEnc  = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.privKey = PKIManager.convertPrivateKeyBytes(privKeyEnc);
        this.username = username;
    }

    private void connectToKibbutzRouter() {
        Log.d(TAG, "Connecting to KibbutzServer @ " + KIBBUTZ_ROUTER_IP + ":" + KIBBUTZ_ROUTER_PORT);
        mChannel = ManagedChannelBuilder
                .forAddress(KIBBUTZ_ROUTER_IP, KIBBUTZ_ROUTER_PORT)
                .usePlaintext(true)
                .build();
        stub = KibbutzRouterGrpc.newBlockingStub(mChannel);
        Log.d(TAG, "Connected successfully");
    }

    private void closeConnectionToKBServer() {
        try {
            if (mChannel != null) {
                Log.d(TAG, "Closing connection to KB Server");
                mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } else {
                Log.w(TAG, "Connection to KB server not started, nothing to close");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Exception while stopping connection to KBServer: " + e);
        }
    }

    public boolean connect() {
        boolean connected = false;
        try {
            connectToKibbutzRouter();
            connected = true;
            Log.d(TAG, "Connection success!");
        } catch (Exception e) {
            Log.e(TAG, "Connection failed: " + e);
            e.printStackTrace();
        }
        return connected;
    }

    public void disconnect() {
        try {
            stop = true;
            closeConnectionToKBServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int sendInitMsg() {
        Log.d(TAG, "Sending init msg ");
        ClientInfo clientInfo = ClientInfo.newBuilder()
                .setClientUsername(username)
                .setClientPubKey(ByteString.copyFrom(pubKeyEnc))
                .build();

        ClientConnectReply reply = grpcClientConnect(clientInfo);

        Log.d(TAG, "InitMsg reply status code: " + reply.getStatusCode());
        if (reply.getStatusCode() == 0) {
            // connection succeeded, save nonce
            nonce = reply.getNonce().toByteArray();
            nonceByteStr = ByteString.copyFrom(nonce);
        }
        return reply.getStatusCode();
    }

    private ClientConnectReply grpcClientConnect(ClientInfo clientInfo) {
        try {
            return stub.clientConnect(clientInfo);
        } catch (Exception e) {
            Log.d(TAG, "grpc ClientConnect exception: " + e);
            return ClientConnectReply.newBuilder().setStatusCode(-100).build();
        }
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

        int failedIOUs = 0;
        while (!stop) {
            try {

                ClientIOUSigned iou     = generateAndSignIOU();
                ClientIOUReply reply    = grpcSendClientIOU(iou);

                if (reply.getStatusCode() != 0) {
                    Log.d(TAG, "IOU rejected with code: " + reply.getStatusCode());
                    failedIOUs++;
                }

                if (failedIOUs >= 20) {
                    Log.d(TAG, "Too many failed IOUs, giving up...");
                    disconnect();
                }

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

    private ClientIOUReply grpcSendClientIOU(ClientIOUSigned signedIou) {
        try {
            return stub.sendClientIOU(signedIou);
        } catch (Exception e) {
            Log.d(TAG, "grpcSendClientIOU exception: " + e);
            return ClientIOUReply.newBuilder().setStatusCode(-100).build();
        }
    }



    private ClientIOUSigned generateAndSignIOU() throws ExecFailedException, IOException, IPTablesParserException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        TrafficInfo in = IPTablesManager.getInputStats(CLIENT_ID);
        TrafficInfo out = IPTablesManager.getOutputStats(CLIENT_ID);
        ClientIOU clientIou = ClientIOU.newBuilder()
                .setIn(in)
                .setOut(out)
                .setClientUsername(username)
                .setNonce(nonceByteStr)
                .build();

        byte[] sign = PKIManager.signData(toBytes(clientIou), privKey);

        ClientIOUSigned signedIOU = ClientIOUSigned.newBuilder()
                .setClientIOU(clientIou)
                .setSign(ByteString.copyFrom(sign))
                .build();

        return signedIOU;
    }
}
