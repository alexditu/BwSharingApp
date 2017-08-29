package research.bwsharingapp.sockcomm;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import research.bwsharingapp.account.PKIManager;
import research.bwsharingapp.sockcomm.msg.ClientIOUSigned;
import research.bwsharingapp.sockcomm.msg.IOU_1;
import research.bwsharingapp.iptables.ExecFailedException;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.iptables.IPTablesParserException;
import research.bwsharingapp.proto.kb.ClientConnectReply;
import research.bwsharingapp.proto.kb.ClientIOU;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.proto.kb.RouterIOU;
import research.bwsharingapp.proto.kb.RouterIOUReply;
import research.bwsharingapp.proto.kb.TrafficInfo;
import research.bwsharingapp.sockcomm.msg.ClientInfo;
import research.bwsharingapp.sockcomm.msg.HelloReply;
import research.bwsharingapp.sockcomm.msg.MsgType;
import research.bwsharingapp.sockcomm.msg.SockCommMsg;
import research.bwsharingapp.sockcomm.server.ServerMainThread;
import research.bwsharingapp.sockcomm.server.ServerWorkerThread;

import static research.bwsharingapp.MainActivity.CLIENT_ID;


/**
 * Created by alex on 4/30/17.
 */

public class SockCommServer {
    private final static String TAG = "SockCommServer";
    private final static int MAX_CLIENT_CONN = 10;

    private int port;
    private InetAddress ipAddr;
    private ServerSocket serverSocket;

    private ServerMainThread serverMainThread;
    private static List<ServerWorkerThread> workerThreadPool = new ArrayList<>(MAX_CLIENT_CONN);

    private byte[] pubKeyEnc;
    private byte[] privKeyEnc;
    private String username;

    public SockCommServer(InetAddress ipAddr, int port, byte[] pubKeyEnc, byte[] privKeyEnc, String username) {
        this.port               = port;
        this.ipAddr             = ipAddr;
        this.serverSocket       = null;
        this.serverMainThread   = null;
        this.pubKeyEnc          = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.privKeyEnc         = Arrays.copyOf(privKeyEnc, privKeyEnc.length);
        this.username           = username;
    }

    public static synchronized void addWorker(ServerWorkerThread worker) {
        workerThreadPool.add(worker);
    }

    public static synchronized void removeWorker(ServerWorkerThread worker) {
        workerThreadPool.remove(worker);
    }

    public void waitWorkers() {
        int size = -1;

        while (size != 0) {
            synchronized (this) {
                size = workerThreadPool.size();
            }
            Log.d(TAG, "Waiting for " + size + " workers to finnish");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d(TAG, "Exception while waiting workers to finnsih: " + e);
                return;
            }
        }
    }


    public boolean start() {
        if(createServerSocket() == null) {
            Log.e(TAG, "Socket creation failed for server: " + this.toString());
            return false;
        }

        serverMainThread = new ServerMainThread(serverSocket, pubKeyEnc, privKeyEnc, username);
        serverMainThread.start();

        return true;
    }

    public boolean stop() {
        try {
            serverSocket.close();

            synchronized (this) {
                for (ServerWorkerThread worker : workerThreadPool) {
                    if (worker.isAlive()) {
                        Log.d(TAG, "Interrupting thread: " + worker.getState());
                        worker.interrupt();
                    } else {
                        Log.d(TAG, "Worker already dead: " + worker.getState());
                    }

                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception while closing serverSocket: " + e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private ServerSocket createServerSocket() {
        try {
            serverSocket = new ServerSocket(port, MAX_CLIENT_CONN, ipAddr);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create server: " + e);
            e.printStackTrace();
        }
        return serverSocket;
    }


    @Override
    public String toString() {
        return ipAddr.getHostAddress() + ":" + port;
    }




}
