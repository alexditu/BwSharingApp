package research.bwsharingapp.account;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import static research.bwsharingapp.account.AccountManagementActivity.USERNAME_KEY;


/**
 * Created by alex on 8/28/17.
 */

public class RegisterUserAL {
    private final static String TAG = "RegisterUserThread";

    private ManagedChannel mChannel;
    private KibbutzGrpc.KibbutzBlockingStub stub;

    private Context ctx;
    private Activity activity;

    public RegisterUserAL(Activity activity) {
        this.activity = activity;
        this.ctx = activity.getApplicationContext();
    }

    public int registerUser() {
        PublicKey pubKey = null;
        try {
            pubKey = PKIManager.getPublicKey(ctx);
        } catch (Exception e) {
            Log.d(TAG, "KeyPair could not be retrieved: " + e + ". Retry!");
            e.printStackTrace();
            return -100;
        }


        try {
            connectToKBServer();
            RegisterUserReply reply = registerUser(pubKey);

            int statusCode = reply.getStatusCode();
            if (statusCode == 0) {
                Log.d(TAG, "User registered successfuly");
            } else {
                Log.d(TAG, "User registration failed with code: " + statusCode);
            }
            return statusCode;
        } catch(Exception e) {
            Log.e(TAG, "grpc call registerUser failed: " + e);
            return -100;
        } finally {
            disconnectFromKBServer();
        }
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
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String username = sharedPref.getString(USERNAME_KEY, null);
        return username;
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
