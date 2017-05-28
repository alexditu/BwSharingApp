package research.bwsharingapp.sockcomm;

import java.io.Serializable;

/**
 * Created by alex on 4/30/17.
 */

public class SockCommMsg<T> implements Serializable {
    public MsgType type;
    public T data;

    public SockCommMsg() {
        type = MsgType.HELLO;
        data = null;
    }

    public SockCommMsg(MsgType type, T data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" + type + ", " + data + "}";
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
