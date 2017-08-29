package research.bwsharingapp.bg.router;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import io.grpc.stub.StreamObserver;
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
import research.bwsharingapp.proto.kb.RouterIOU;
import research.bwsharingapp.proto.kb.RouterIOUReply;
import research.bwsharingapp.proto.kb.RouterIOUSigned;
import research.bwsharingapp.proto.kb.TrafficInfo;

import static research.bwsharingapp.MainActivity.CLIENT_ID;
import static research.bwsharingapp.bg.router.KibbutzRouterService.SESSION_NONCE_LENGTH;

/**
 * Created by alex on 8/29/17.
 */

public class KibbutzRouterService extends KibbutzRouterGrpc.KibbutzRouterImplBase {
    private final static String TAG = "KibbutzRouterService";

    public static final int SESSION_NONCE_LENGTH = 64;

    private byte[] pubKeyEnc;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private String username;
    private KibbutzGrpc.KibbutzBlockingStub stub;

    private HashMap<String, Client> clients;

    private synchronized Client getClient(String clientUsername) {
        return clients.get(clientUsername);
    }

    private synchronized void putClient(Client client) {
        clients.put(client.getUsername(), client);
    }

    public KibbutzRouterService(KibbutzGrpc.KibbutzBlockingStub stub, byte[] pubKeyEnc, byte[] privKeyEnc, String username) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.stub       = stub;
        this.pubKeyEnc  = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.privKey    = PKIManager.convertPrivateKeyBytes(privKeyEnc);
        this.pubKey     = PKIManager.convertPublicKeyBytes(pubKeyEnc);
        this.username   = username;

        clients = new HashMap<>();
    }

    @Override
    public void clientConnect(ClientInfo request, StreamObserver<ClientConnectReply> responseObserver) {
        ClientConnectReply reply = null;
        try {
            reply = clientConnect(request);
        } catch (Exception e) {
            Log.e(TAG, "clientConnect exception" + e);
            ClientConnectReply.Builder replyB = ClientConnectReply.newBuilder();
            replyB.setStatusCode(-101);
            replyB.setStatusMsg("clientConnect exception" + e);

            reply = replyB.build();
        }
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void sendClientIOU(ClientIOUSigned request, StreamObserver<ClientIOUReply> responseObserver) {
        ClientIOUReply reply = null;
        try {
            reply = sendClientIOU(request);
        } catch (Exception e) {
            Log.e(TAG, "sendClientIOU exception" + e);
            ClientIOUReply.Builder replyB = ClientIOUReply.newBuilder();
            replyB.setStatusCode(-101);

            reply = replyB.build();
        }
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private ClientConnectReply clientConnect(ClientInfo request) throws InvalidKeySpecException, NoSuchAlgorithmException {
        ClientConnectReply.Builder replyB = ClientConnectReply.newBuilder();

        Log.d(TAG, "Recv connect from client: " + request.getClientUsername());
        Client client = new Client(request.getClientUsername(), request.getClientPubKey().toByteArray());

        ClientConnectReply kbReply = grpcClientConnect(client);
        if (kbReply.getStatusCode() == -100) {
            Log.e(TAG, "grpc call failed, rejecting connection");
            replyB.setStatusCode(-100);
            replyB.setStatusMsg("grpc call to Kibbutz Server failed, connection rejected");
        } else if (kbReply.getStatusCode() != 0) {
            Log.e(TAG, "client is not registered, rejecting connection");
            replyB.setStatusCode(-1);
            replyB.setStatusMsg("client is not registered, rejecting connection");
        } else {
            Log.d(TAG, "client connected successfully");
            replyB.setStatusCode(0);
            replyB.setStatusMsg("client connected successfully");
            replyB.setNonce(ByteString.copyFrom(client.getNonce()));
            putClient(client);
        }

        return replyB.build();
    }

    private ClientConnectReply grpcClientConnect(Client client) {
        ClientConnectReply.Builder kbReplyB = ClientConnectReply.newBuilder();
        research.bwsharingapp.proto.kb.ClientInfo.Builder kbClientInfoB =
                research.bwsharingapp.proto.kb.ClientInfo.newBuilder();

        kbClientInfoB.setClientUsername(client.getUsername());
        kbClientInfoB.setClientPubKey(ByteString.copyFrom(client.getPubKeyEnc()));
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


    // sendClientIOU -------------------------------------------------------------------------------
    private ClientIOUReply sendClientIOU(ClientIOUSigned request) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException, IPTablesParserException, ExecFailedException {
        ClientIOUReply.Builder replyB = ClientIOUReply.newBuilder();

        // step 1: verify client IOU signature
        int verificationStatus = verifyClientIOU(request);
        if (verificationStatus != 0) {
            Log.e(TAG, "IOU signature verification failed");
            return replyB.setStatusCode(-1).build();
        }

        // step 2: gather router statistics
        TrafficInfo fw[] = IPTablesManager.getFwStats(CLIENT_ID);
        printIOUs(request, fw);

        // step 3: generate router IOU by signing clientIouSigned
        RouterIOUSigned routerIou = createRouterIOUSigned(request, fw);

        // step 4: send routerIOU to Kibbutz Server for validation
        RouterIOUReply reply = grpcSendRouterIOU(routerIou);

        if (reply.getStatusCode() != 0) {
            Log.d(TAG, "grpcSendRouterIOU failed with status code: " + reply.getStatusCode());
            return replyB.setStatusCode(-2).build();
        }

        Log.d(TAG, "IOU processed successfully");

        return replyB.setStatusCode(0).build();
    }

    private void printIOUs(ClientIOUSigned request, TrafficInfo fw[]) {
        Log.d(TAG, "client stats bytes: " +
                fmt(request.getClientIOU().getIn().getBytes() + "") + "\t\t" +
                fmt(request.getClientIOU().getOut().getBytes() + ""));
        Log.d(TAG, "router stats bytes: " +
                fmt(fw[0].getBytes() + "") + "\t\t" +
                fmt(fw[1].getBytes() + ""));
    }

    private RouterIOUSigned createRouterIOUSigned(ClientIOUSigned clientIouSigned, TrafficInfo fw[]) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        RouterIOUSigned.Builder routerIouSignedB = RouterIOUSigned.newBuilder();

        RouterIOU routerIou = createRouterIOU(clientIouSigned, fw);
        byte[] sign = PKIManager.signData(toBytes(routerIou), privKey);

        routerIouSignedB.setRouterIou(routerIou);
        routerIouSignedB.setSign(ByteString.copyFrom(sign));

        return routerIouSignedB.build();
    }

    private RouterIOU createRouterIOU(ClientIOUSigned clientIouSigned, TrafficInfo fw[]) {
        RouterIOU.Builder routerIouB = RouterIOU.newBuilder();
        routerIouB.setIn(fw[0]);
        routerIouB.setOut(fw[1]);
        routerIouB.setClientIouSigned(clientIouSigned);
        return routerIouB.build();
    }

    private RouterIOUReply grpcSendRouterIOU(RouterIOUSigned routerIou) {
        try {
            return stub.sendRouterIOU(routerIou);
        } catch (Exception e) {
            Log.d(TAG, "grpcSendRouterIOU exception: " + e);
            return RouterIOUReply.newBuilder().setStatusCode(-100).build();
        }
    }

    /**
     *
     * @return
     *           0 IOU is verified successfully
     *          -1 client not connected
     *          -2 Invalid signature
     *          -3 Nonces differ
     */
    private int verifyClientIOU(ClientIOUSigned request) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Client client = getClient(request.getClientIOU().getClientUsername());

        if (client == null) {
            Log.d(TAG, "Client not connected");
            return -1;
        }

        boolean isIOUValid = PKIManager.verifySignature(
                toBytes(request.getClientIOU()),
                request.getSign().toByteArray(),
                client.getPubKey());
        if (isIOUValid == false) {
            Log.d(TAG, "Invalid signature");
            return -2;
        }

        if (Arrays.equals(request.getClientIOU().getNonce().toByteArray(), client.getNonce()) == false) {
            Log.d(TAG, "Nonces differ");
            return -3;
        }

        return 0;
    }



    //----------------------------------------------------------------------------------------------

    private byte[] toBytes(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.flush();
            byte[] bytes = bos.toByteArray();
            return bytes;
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    private String fmt(String value) {
        DecimalFormat myFormatter = new DecimalFormat("###,###.###");
        String output = myFormatter.format(Double.parseDouble(value));
        return output;
    }


}


class Client {
    private String username;
    private byte[] pubKeyEnc;
    private PublicKey pubKey;
    private byte[] nonce;

    public Client(String username, byte[] pubKeyEnc) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.username = username;
        this.pubKeyEnc = Arrays.copyOf(pubKeyEnc, pubKeyEnc.length);
        this.pubKey = PKIManager.convertPublicKeyBytes(pubKeyEnc);
        this.nonce = PKIManager.generateRandomNonce(SESSION_NONCE_LENGTH);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPubKeyEnc() {
        return pubKeyEnc;
    }

    public void setPubKeyEnc(byte[] pubKeyEnc) {
        this.pubKeyEnc = pubKeyEnc;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public void setPubKey(PublicKey pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}