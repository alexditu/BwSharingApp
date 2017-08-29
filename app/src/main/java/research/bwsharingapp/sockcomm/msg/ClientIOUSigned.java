package research.bwsharingapp.sockcomm.msg;

import java.io.Serializable;

import research.bwsharingapp.sockcomm.msg.IOU_1;

/**
 * Created by alex on 8/29/17.
 */

public class ClientIOUSigned implements Serializable {
    private IOU_1 clientIOU;
    private byte[] sign;

    public ClientIOUSigned(IOU_1 clientIOU, byte[] sign) {
        this.clientIOU = clientIOU;
        this.sign = sign;
    }

    public IOU_1 getClientIOU() {
        return clientIOU;
    }

    public void setClientIOU(IOU_1 clientIOU) {
        this.clientIOU = clientIOU;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }
}
