package research.bwsharingapp.sockcomm.server;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.DecimalFormat;
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
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.proto.kb.RouterIOU;
import research.bwsharingapp.proto.kb.RouterIOUReply;
import research.bwsharingapp.proto.kb.RouterIOUSigned;
import research.bwsharingapp.proto.kb.TrafficInfo;
import research.bwsharingapp.sockcomm.CommConstants;
import research.bwsharingapp.sockcomm.msg.ClientIOUSigned;
import research.bwsharingapp.sockcomm.msg.ClientInfo;
import research.bwsharingapp.sockcomm.msg.HelloReply;
import research.bwsharingapp.sockcomm.msg.IOU_1;
import research.bwsharingapp.sockcomm.msg.MsgType;
import research.bwsharingapp.sockcomm.msg.SockCommMsg;

import static research.bwsharingapp.MainActivity.CLIENT_ID;
import static research.bwsharingapp.sockcomm.SockCommServer.removeWorker;

/**
 * Created by alex on 8/29/17.
 */

public class ServerWorkerThread extends Thread {
    private final String TAG = "SockCommServer_worker-" + Thread.currentThread().getId();
    private Socket clientSocket;

    private ManagedChannel mChannel;
    private KibbutzGrpc.KibbutzBlockingStub stub;

//    private byte[] pubKeyEnc;
    private PublicKey pubKey;
    private PrivateKey privKey;
    private String username;

    private ClientInfo clientInfo;
    private PublicKey clientPubKey;
    private byte[] nonce;
    private static final int SESSION_NONCE_LENGTH = 64;

    public ServerWorkerThread(Socket clientSocket, byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.clientSocket   = clientSocket;
        this.pubKey         = PKIManager.convertPublicKeyBytes(pubKeyEnc);
        this.privKey        = PKIManager.convertPrivateKeyBytes(privKeyEnc);
        this.username       = username;
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
        //TODO: fix me
        try {
            RouterIOU.Builder routerIouBuilder = RouterIOU.newBuilder();
            research.bwsharingapp.proto.kb.ClientIOUSigned clientIOUSigned =
                    research.bwsharingapp.proto.kb.ClientIOUSigned.newBuilder()
                    .setClientIOU(clientIou).build();
            routerIouBuilder.setClientIouSigned(clientIOUSigned);
            routerIouBuilder.setIn(fw[0]);
            routerIouBuilder.setOut(fw[1]);

            RouterIOU routerIou = routerIouBuilder.build();

            RouterIOUSigned routerIouSigned = RouterIOUSigned.newBuilder()
                    .setRouterIou(routerIou).build();

            RouterIOUReply reply = stub.sendRouterIOU(routerIouSigned);
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
        try {
            connectToKBServer();

            boolean closeConnection = false;
            while (true) {
                Log.d(TAG, "Reading message");
                input = new ObjectInputStream(clientSocket.getInputStream());
                Object ob = input.readObject();


                SockCommMsg<Object> genericMsg = (SockCommMsg<Object>) ob;
                Log.d(TAG, "Recv msg type: " + genericMsg.getType());
                if (genericMsg.getType() == MsgType.HELLO) {
                    closeConnection = processHelloMsg((SockCommMsg<ClientInfo>) ob);
                } else if (genericMsg.getType() == MsgType.IOU_1) {
                    int ret = processClientIOU((SockCommMsg<ClientIOUSigned>) ob);
                    if (ret != 0) {
                        //do stuff
                    }
                } else {
                    Log.d(TAG, "Unknown message type: " + genericMsg.getType());
                }

                if (closeConnection) {
                    clientSocket.close();
                    break;
                }
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
        } finally {
            closeConnectionToKBServer();
            removeWorker(this);
        }
    }

    private boolean processHelloMsg(SockCommMsg<ClientInfo> request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        boolean closeConnection = false;

        Log.d(TAG, "Recv HELLO from client: " + request.getData().getUsername());
        this.clientInfo = request.getData();

        ClientConnectReply kbReply = grpcClientConnect();

        boolean clientIsValid = false;
        if (kbReply.getStatusCode() == -100) {
            Log.e(TAG, "grpc call failed, rejecting connection");
            closeConnection = true;
        } else if (kbReply.getStatusCode() != 0) {
            Log.e(TAG, "client is not registered, rejecting connection");
            closeConnection = true;
        } else {
            clientIsValid = true;
            this.clientPubKey = PKIManager.convertPublicKeyBytes(this.clientInfo.getPubKeyEnc());
            Log.d(TAG, "Saved client public key");
        }

        Log.d(TAG, "Generating reply");
        SockCommMsg<HelloReply> reply = createHelloReply(clientIsValid, kbReply.getStatusCode());

        Log.d(TAG, "Sending reply");
        sendReply(reply);

        return closeConnection;
    }

    private SockCommMsg<HelloReply> createHelloReply(boolean clientIsValid, int statusCode) throws NoSuchAlgorithmException {
        this.nonce = PKIManager.generateRandomNonce(SESSION_NONCE_LENGTH);
        HelloReply helloReply = new HelloReply(clientIsValid, statusCode, this.nonce);
        SockCommMsg<HelloReply> reply = new SockCommMsg<>(MsgType.HELLO_REPLY, helloReply);
        return reply;
    }

    private void sendReply(SockCommMsg reply) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
        output.writeObject(reply);
        output.flush();
    }

    private ClientConnectReply grpcClientConnect() {
        ClientConnectReply.Builder kbReplyB = ClientConnectReply.newBuilder();
        research.bwsharingapp.proto.kb.ClientInfo.Builder kbClientInfoB =
                research.bwsharingapp.proto.kb.ClientInfo.newBuilder();

        kbClientInfoB.setClientUsername(clientInfo.getUsername());
        kbClientInfoB.setClientPubKey(ByteString.copyFrom(clientInfo.getPubKeyEnc()));
        kbClientInfoB.setRouterUsername(username);

        Log.d(TAG, "grpc call 'clientConnect'");
        ClientConnectReply kbReply;
        try {
            kbReply = stub.clientConnect(kbClientInfoB.build());
        } catch (Exception e) {
            Log.e(TAG, "grpc request 'clientConnect' failed: " + e);
            return kbReplyB.setClientExists(false).setStatusCode(-100).build();
        }
        Log.d(TAG, "grpc call 'clientConnect' succeeded");

        return kbReply;
    }

    /**
     * @return:
     *           0 IOU processed successfully
     *          -1 invalid IOU client signature
     *
     */
    private int processClientIOU(SockCommMsg<ClientIOUSigned> request) throws ExecFailedException, IOException, IPTablesParserException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        int ret = 0;

        ClientIOUSigned signedIOU = request.getData();
        boolean isIOUValid = PKIManager.verifySignature(
                signedIOU.getClientIOU().toBytes(),
                signedIOU.getSign(),
                this.clientPubKey);

        if (isIOUValid == false) {
            Log.e(TAG, "IOU signature verification failed");
            ret = -1;
        }

        TrafficInfo fw[] = IPTablesManager.getFwStats(CLIENT_ID);
        Log.d(TAG, "client stats bytes: " +
                fmt(request.getData().getClientIOU().getInput().getBytes() + "") + "\t\t" +
                fmt(request.getData().getClientIOU().getOutput().getBytes() + ""));
        Log.d(TAG, "router stats bytes: " +
                fmt(fw[0].getBytes() + "") + "\t\t" +
                fmt(fw[1].getBytes() + ""));


        ClientIOU clientIou = convert(signedIOU.getClientIOU());
        sendRouterIou(clientIou, fw);

        return ret;
    }

    private boolean checkIOUSignature(ClientIOUSigned signedIOU) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        boolean isValid = false;
        isValid = PKIManager.verifySignature(
                signedIOU.getClientIOU().toBytes(),
                signedIOU.getSign(),
                this.clientPubKey);
        return isValid;
    }
}
