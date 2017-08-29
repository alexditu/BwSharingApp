package research.bwsharingapp.sockcomm.msg;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by alex on 8/29/17.
 */

public class HelloReply implements Serializable {
    private boolean connectionAccepted;
    private int statusCode;
    byte[] nonce;

    public HelloReply(){}

    public HelloReply(boolean connectionAccepted, int statusCode, byte[] nonce) {
        this.connectionAccepted     = connectionAccepted;
        this.statusCode             = statusCode;
        this.nonce                  = Arrays.copyOf(nonce, nonce.length);
    }

    public boolean isConnectionAccepted() {
        return connectionAccepted;
    }

    public void setConnectionAccepted(boolean connectionAccepted) {
        this.connectionAccepted = connectionAccepted;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = Arrays.copyOf(nonce, nonce.length);
    }
}
