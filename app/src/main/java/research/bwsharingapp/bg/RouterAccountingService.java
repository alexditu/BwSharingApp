package research.bwsharingapp.bg;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import research.bwsharingapp.bg.pojo.ServiceInfo;
import research.bwsharingapp.bg.router.KibbutzRouterService;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.sockcomm.CommConstants;
import research.bwsharingapp.sockcomm.SockCommServer;

/**
 * Created by alex on 5/27/17.
 */

public class RouterAccountingService extends AccountingService {
    private final static String TAG = "RouterAccountingService";

    private ServiceInfo kb;
    private SockCommServer server;

    private ManagedChannel mChannel = null;
    private KibbutzGrpc.KibbutzBlockingStub stub = null;

    private Server server2;

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

        //TODO: remove rest
        closeConnectionToKBServer();
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




    private void startCommServer2(byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        InetAddress ip  = InetAddress.getByName(kb.getRouterIp());
        int port        = Integer.parseInt(kb.getRouterPort());

        connectToKBServer();
        startKibbutzRouterServer(port, pubKeyEnc, privKeyEnc, username);
    }

    private void startKibbutzRouterServer(int port, byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        Log.d(TAG, "Starting KibbutzRouterService on port: " + port);
        server2 = ServerBuilder.forPort(port)
                .addService(new KibbutzRouterService(stub, pubKeyEnc, privKeyEnc, username))
                .build()
                .start();
        Log.d(TAG, "KibbutzRouterService started successfuly");
    }

    private void connectToKBServer() {
        Log.d(TAG, "Connecting to KibbutzServer @ " + CommConstants.KB_IP + ":" + CommConstants.KB_PORT);
        mChannel = ManagedChannelBuilder
                .forAddress(CommConstants.KB_IP, CommConstants.KB_PORT)
                .usePlaintext(true)
                .build();
        stub = KibbutzGrpc.newBlockingStub(mChannel);
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
}
