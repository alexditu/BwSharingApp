package research.bwsharingapp.account;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by alex on 8/28/17.
 */

public class PKIManager {
    private final static String TAG             = "PKIManager";
    private final static String PUB_FILENAME    = "key.pub";
    private final static String PRIV_FILENAME   = "key.priv";


    private final static String PKI_ALG_NAME    = "RSA";
    private final static String RAND_ALG        = "SHA1PRNG";
    private final static String SIGN_ALG_NAME   = "SHA1withRSA";
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

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(PKI_ALG_NAME);
        SecureRandom random = SecureRandom.getInstance(RAND_ALG);

        byte[] seed = random.generateSeed(2048);
        random.setSeed(seed);

        keyGen.initialize(INIT_LEN, random);
        KeyPair pair = keyGen.generateKeyPair();

        saveKeysToFile(pair, ctx);

        Log.d(TAG, "Key pairs generated successfuly");
        return pair;
    }

    public static KeyPair getKeys(Context ctx) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        KeyPair keyPair = null;
        PrivateKey privKey = getPrivateKey(ctx);
        PublicKey pubKey = getPublicKey(ctx);
        keyPair = new KeyPair(pubKey, privKey);
        return keyPair;
    }

    public static byte[] generateRandomNonce(final int length) throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance(RAND_ALG);
        byte[] seed = random.generateSeed(length);
        byte[] nonce = new byte[length];

        random.setSeed(seed);
        random.nextBytes(nonce);

        return nonce;
    }

    public static byte[] signData(byte[] data, PrivateKey privKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance(SIGN_ALG_NAME);
        sig.initSign(privKey);
        sig.update(data);

        return sig.sign();
    }

    public static boolean verifySignature(byte[] data, byte[] sigToVerify, PublicKey pubKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance(SIGN_ALG_NAME);
        sig.initVerify(pubKey);
        sig.update(data);

        return sig.verify(sigToVerify);
    }


    /******************** Internal private methods ************************************************/

    public static PublicKey convertPublicKeyBytes(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance(PKI_ALG_NAME);
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        return pubKey;
    }

    public static PrivateKey convertPrivateKeyBytes(byte[] encKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
        KeyFactory keyFactory = KeyFactory.getInstance(PKI_ALG_NAME);
        PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
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
