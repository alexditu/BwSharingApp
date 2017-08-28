package research.bwsharingapp.sockcomm;

import java.io.Serializable;

/**
 * Created by alex on 8/28/17.
 */

public class ClientInfo implements Serializable {
    private String username;
    private byte[] pubKeyEnc;

    public ClientInfo(){}

    public ClientInfo(String username, byte[] pubKeyEnc) {
        this.username = username;
        this.pubKeyEnc = pubKeyEnc;
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
}
