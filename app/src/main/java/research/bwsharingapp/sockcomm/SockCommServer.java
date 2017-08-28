package research.bwsharingapp.sockcomm;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import research.bwsharingapp.iou.IOU_1;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.proto.kb.ClientIOU;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.proto.kb.RouterIOU;
import research.bwsharingapp.proto.kb.RouterIOUReply;
import research.bwsharingapp.proto.kb.TrafficInfo;

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
    private List<ServerWorkerThread> workerThreadPool;

    public SockCommServer(InetAddress ipAddr, int port) {
        this.port           = port;
        this.ipAddr         = ipAddr;
        this.serverSocket   = null;
        this.serverMainThread = null;

        workerThreadPool = new ArrayList<>(MAX_CLIENT_CONN);
    }

    public synchronized void addWorker(ServerWorkerThread worker) {
        workerThreadPool.add(worker);
    }

    public synchronized void removeWorker(ServerWorkerThread worker) {
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

        serverMainThread = new ServerMainThread();
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

class ServerWorkerThread extends Thread {
    private final String TAG = "SockCommServer_worker-" + Thread.currentThread().getId();
    private Socket clientSocket;

    private ManagedChannel mChannel;
    private KibbutzGrpc.KibbutzBlockingStub stub;

    public ServerWorkerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void connectToKBServer() {
        mChannel = ManagedChannelBuilder
                .forAddress(CommConstants.KB_IP, CommConstants.KB_PORT)
                .usePlaintext(true)
                .build();
        stub = KibbutzGrpc.newBlockingStub(mChannel);
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

    void sendRouterIou(ClientIOU clientIou, TrafficInfo fw[]) {
        try {
            RouterIOU.Builder routerIouBuilder = RouterIOU.newBuilder();
            routerIouBuilder.setClientIou(clientIou);
            routerIouBuilder.setIn(fw[0]);
            routerIouBuilder.setOut(fw[1]);

            RouterIOU routerIou = routerIouBuilder.build();
            RouterIOUReply reply = stub.sendRouterIOU(routerIou);
            Log.d(TAG, "recv message: " + reply);
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending routerIOU: " + e);
        }
    }

    private String fmt(String value) {
        DecimalFormat myFormatter = new DecimalFormat("###,###.###");
        String output = myFormatter.format(Double.parseDouble(value));
        return output;
    }

    // TODO: should be replaced when communication between client and router is done via grpc
    private ClientIOU convert(IOU_1 iou) {
        ClientIOU.Builder builder = ClientIOU.newBuilder();

        TrafficInfo.Builder bIn = TrafficInfo.newBuilder();
        bIn.setBytes(iou.getInput().getBytes());
        bIn.setPkts(iou.getInput().getPkts());
        bIn.setSrc(iou.getInput().getSrc());
        bIn.setDst(iou.getInput().getDst());

        TrafficInfo.Builder bOut = TrafficInfo.newBuilder();
        bOut.setBytes(iou.getOutput().getBytes());
        bOut.setPkts(iou.getOutput().getPkts());
        bOut.setSrc(iou.getOutput().getSrc());
        bOut.setDst(iou.getOutput().getDst());

        builder.setIn(bIn.build());
        builder.setOut(bOut.build());
        return builder.build();
    }

    @Override
    public void run() {
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        try {

            connectToKBServer();

            while (true) {
                Log.d(TAG, "Reading message");
                input = new ObjectInputStream(clientSocket.getInputStream());
                Object ob = input.readObject();


                SockCommMsg<Object> test = (SockCommMsg<Object>) ob;
                Log.d(TAG, "Recv msg type: " + test.getType());


                SockCommMsg<IOU_1> request = (SockCommMsg<IOU_1>) ob;
//                Log.d(TAG, "Message read: " + request);

                TrafficInfo fw[] = IPTablesManager.getFwStats(CLIENT_ID);
                Log.d(TAG, "client stats bytes: " +
                        fmt(request.getData().getInput().getBytes() + "") + "\t\t" +
                        fmt(request.getData().getOutput().getBytes() + ""));
                Log.d(TAG, "router stats bytes: " +
                        fmt(fw[0].getBytes() + "") + "\t\t" +
                        fmt(fw[1].getBytes() + ""));


                ClientIOU clientIou = convert(request.getData());
                sendRouterIou(clientIou, fw);
            }


//            Log.d(TAG, "Sending reply");
//            output = new ObjectOutputStream(clientSocket.getOutputStream());
//            if (request.getType() == MsgType.HELLO) {
//                SockCommMsg<String> reply = new SockCommMsg<String>(MsgType.HELLO, "Ok");
//                output.writeObject(reply);
//             } else {
//                SockCommMsg<String> reply = new SockCommMsg<String>(MsgType.ERROR, "Error: invalid type for first msg");
//                output.writeObject(reply);
//                output.flush();
//            }
//
//            input.close();
//            output.close();
        } catch (Exception e) {
            Log.d(TAG, "Exception in server worker thread: " + e);
            closeConnectionToKBServer();
        }

        removeWorker(this);
    }
}

    class ServerMainThread extends Thread {
        private final String TAG = "SockCommServer_main";

        public ServerMainThread() {

        }

        @Override
        public void run() {
            try {
                while(true) {
                    Log.d(TAG, "Waiting for clients...");
                    Socket clientSock = null;

                    clientSock = serverSocket.accept();
                    Log.d(TAG, "Client connected: " + clientSock.getInetAddress().getHostAddress());

                    ServerWorkerThread th = new ServerWorkerThread(clientSock);
                    th.start();
                    addWorker(th);
                }
            } catch (SocketException e) {
                Log.d(TAG, "Server stopped: signaled to stop: " + e);
            } catch (IOException e) {
                Log.e(TAG, "Server stopped: Exception while accepting socket: " + e);
            }
        }
    }
}
