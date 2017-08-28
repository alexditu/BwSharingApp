package research.bwsharingapp.account;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by alex on 8/28/17.
 */

public class PKIManager {
    private final static String TAG             = "PKIManager";
    private final static String PUB_FILENAME    = "key.pub";
    private final static String PRIV_FILENAME   = "key.priv";


    private final static String ALG_NAME        = "RSA";
    private final static String RAND_ALG        = "SHA1PRNG";
    private final static int INIT_LEN           = 2048;

    public static PublicKey getPublicKey(Context ctx) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Log.d(TAG, "Retrieving public key");

        byte[] encKey = getKeyBytesFromFile(PUB_FILENAME, ctx);
        PublicKey pubKey = convertPublicKeyBytes(encKey);

        Log.d(TAG, "Public key retrieved successfuly");
        return pubKey;
    }

    public static PrivateKey getPrivateKey(Context ctx) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Log.d(TAG, "Retrieving private key");

        byte[] encKey = getKeyBytesFromFile(PRIV_FILENAME, ctx);
        PrivateKey privKey = convertPrivateKeyBytes(encKey);

        Log.d(TAG, "Private key retrieved successfuly");
        return privKey;
    }

    public static KeyPair generateKeys(Context ctx) throws NoSuchAlgorithmException, IOException {
        Log.d(TAG, "Generating key pairs");

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALG_NAME);
        SecureRandom random = SecureRandom.getInstance(RAND_ALG);
        keyGen.initialize(INIT_LEN, random);
        KeyPair pair = keyGen.generateKeyPair();

        saveKeysToFile(pair, ctx);

        Log.d(TAG, "Key pairs generated successfuly");
        return pair;
    }

    //TODO: remove this
//    public static KeyPair getKeys(Context ctx) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
//        KeyPair keyPair = null;
//        File f = new File(PRIV_FILENAME);
//        if (f.exists()) {
//            /* key pair already exists no need to generate them */
//            PrivateKey privKey = getPrivateKey(ctx);
//            PublicKey pubKey = getPublicKey(ctx);
//            keyPair = new KeyPair(pubKey, privKey);
//        } else {
//            /* need to generate keys */
//            keyPair = generateKeys(ctx);
//        }
//        return keyPair;
//    }


    /******************** Internal private methods ************************************************/

    private static PublicKey convertPublicKeyBytes(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALG_NAME);
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        return pubKey;
    }

    private static PrivateKey convertPrivateKeyBytes(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALG_NAME);
        PrivateKey privKey = keyFactory.generatePrivate(pubKeySpec);
        return privKey;
    }

    private static byte[] getKeyBytesFromFile(String filename, Context ctx) throws IOException {
        FileInputStream fis = null;
        try {
            fis = ctx.openFileInput(filename);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "KeyPair does not exist! Call generateKeys first");
            throw e;
        }

        byte[] encKey = new byte[fis.available()];
        fis.read(encKey);
        fis.close();
        return encKey;
    }



    private static void saveKeysToFile(KeyPair pair, Context ctx) throws IOException {
        saveKeyToFile(pair.getPrivate(), ctx);
        saveKeyToFile(pair.getPublic(), ctx);
    }

    private static void saveKeyToFile(PrivateKey priv, Context ctx) throws IOException {
        FileOutputStream fos = ctx.openFileOutput(PRIV_FILENAME, Context.MODE_PRIVATE);
        byte[] key = priv.getEncoded();
        fos.write(key);
        fos.close();
    }

    private static void saveKeyToFile(PublicKey pub, Context ctx) throws IOException {
        FileOutputStream fos = ctx.openFileOutput(PUB_FILENAME, Context.MODE_PRIVATE);
        byte[] key = pub.getEncoded();
        fos.write(key);
        fos.close();
    }
}
