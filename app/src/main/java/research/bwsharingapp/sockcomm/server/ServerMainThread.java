package research.bwsharingapp.sockcomm.server;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import static research.bwsharingapp.sockcomm.SockCommServer.addWorker;


/**
 * Created by alex on 8/29/17.
 */

public class ServerMainThread extends Thread {
    private final String TAG = "SockCommServer_main";

    private byte[] pubKeyEnc;
    private byte[] privKeyEnc;
    private String username;
    private ServerSocket serverSocket;

    public ServerMainThread(ServerSocket serverSocket, byte[] pubKeyEnc, byte[] privKeyEnc, String username) {
        this.serverSocket = serverSocket;
        this.pubKeyEnc  = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.privKeyEnc = Arrays.copyOf(privKeyEnc, privKeyEnc.length);
        this.username   = username;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Log.d(TAG, "Waiting for clients...");
                Socket clientSock = null;

                clientSock = serverSocket.accept();
                Log.d(TAG, "Client connected: " + clientSock.getInetAddress().getHostAddress());

                ServerWorkerThread th =
                        new ServerWorkerThread(clientSock, pubKeyEnc, privKeyEnc, username);
                th.start();
                addWorker(th);
            }
        } catch (SocketException e) {
            Log.d(TAG, "Server stopped: signaled to stop: " + e);
        } catch (IOException e) {
            Log.e(TAG, "Server stopped: Exception while accepting socket: " + e);
        } catch (Exception e) {
            Log.e(TAG, "Unknown exception while starting server worker thread: " + e);
        }
    }
}
