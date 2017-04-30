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

/**
 * Created by alex on 4/30/17.
 */

public class SockCommServer {
    private final static String TAG = "SockCommServer";
    private final static int MAX_CLIENT_CONN = 10;

    private int port;
    private InetAddress ipAddr;
    private ServerSocket serverSocket;

    public SockCommServer(InetAddress ipAddr, int port) {
        Log.d(TAG, "SockCommServer: " + ipAddr.getHostAddress() + ":" + port);
        this.port           = port;
        this.ipAddr         = ipAddr;
        this.serverSocket   = null;
    }

    public void start() {
        if(createServerSocket() == null) {
            Log.e(TAG, "Socket creation failed for server: " + this.toString());
            return;
        }

        run();
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

    private void run() {
        while(true) {
            Log.d(TAG, "Waiting for clients...");
            Socket clientSock = null;
            try {
                clientSock = serverSocket.accept();
                Log.d(TAG, "Client connected: " + clientSock.getInetAddress().getHostAddress());

                ServerRequestProcessorThread th = new ServerRequestProcessorThread(clientSock);
                th.start();

                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            } catch (IOException e) {
                Log.e(TAG, "Exception while accepting socket: " + e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return ipAddr.getHostAddress() + ":" + port;
    }

class ServerRequestProcessorThread extends Thread {
    private final String TAG = "ServerTh-" + Thread.currentThread().getId();
    private Socket clientSocket;

    public ServerRequestProcessorThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        try {
            Log.d(TAG, "Reading message");
            input = new ObjectInputStream(clientSocket.getInputStream());
            SockCommMsg<Void> request = (SockCommMsg<Void>) input.readObject();
            Log.d(TAG, "Message read: " + request);

            Log.d(TAG, "Sending reply");
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            if (request.getType() == 0) {
                SockCommMsg<String> reply = new SockCommMsg<String>(1, "Ok");
                output.writeObject(reply);
             } else {
                SockCommMsg<String> reply = new SockCommMsg<String>(-1, "Error: invalid type for first msg");
                output.writeObject(reply);
                output.flush();
            }

            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}


}
