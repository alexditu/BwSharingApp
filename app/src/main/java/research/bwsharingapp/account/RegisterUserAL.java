package research.bwsharingapp.account;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.proto.kb.RegisterUserReply;
import research.bwsharingapp.proto.kb.UserData;
import research.bwsharingapp.sockcomm.CommConstants;

import static java.security.AccessController.getContext;

/**
 * Created by alex on 8/28/17.
 */

public class RegisterUserAL {
    private final static String TAG = "RegisterUserThread";

    private ManagedChannel mChannel;
    private KibbutzGrpc.KibbutzBlockingStub stub;

    private Context ctx;

    public RegisterUserAL(Context ctx) {
        this.ctx = ctx;
    }

    public int registerUser() {
        KeyPair keyPair = null;
        try {
            keyPair = PKIManager.generateKeys(ctx);
        } catch (Exception e) {
            Log.d(TAG, "KeyPair generation failed: " + e + ". Retry!");
            e.printStackTrace();
//            Toast.makeText(ctx, "KeyPair generation failed: " + e, Toast.LENGTH_LONG);
            return -100;
        }

        connectToKBServer();
        RegisterUserReply reply = registerUser(keyPair.getPublic());

        int statusCode = reply.getStatusCode();
        if (statusCode == 0) {
//
            Log.d(TAG, "User registered successfuly");
        } else {
//            Toast.makeText(ctx, "User registration failed with code: " + statusCode, Toast.LENGTH_LONG);
            Log.d(TAG, "User registration failed with code: " + statusCode);
        }

        disconnectFromKBServer();
        return statusCode;
    }

    private RegisterUserReply registerUser(PublicKey keyPub) {
        UserData userData = createUserData(keyPub);
        RegisterUserReply reply = stub.registerUser(userData);
        return reply;
    }

    private UserData createUserData(PublicKey keyPub) {
        String username         = getUsername();
        String deviceId         = getDeviceId();
        ByteString keyPubStr    = convertKeyPubToByteString(keyPub);

        UserData userData = UserData.newBuilder()
                .setUsername(username)
                .setDeviceId(deviceId)
                .setPubKey(keyPubStr)
                .build();

        return userData;
    }

    private ByteString convertKeyPubToByteString(PublicKey keyPub) {
        byte[] encKey = keyPub.getEncoded();
        return ByteString.copyFrom(encKey);
    }

    private String getUsername() {
        //TODO: should read this info from somewhere
        return "some user name";
    }

    private String getDeviceId() {
        String id = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }

    private void connectToKBServer() {
        mChannel = ManagedChannelBuilder
                .forAddress(CommConstants.KB_IP, CommConstants.KB_PORT)
                .usePlaintext(true)
                .build();
        stub = KibbutzGrpc.newBlockingStub(mChannel);
    }

    private void disconnectFromKBServer() {
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
