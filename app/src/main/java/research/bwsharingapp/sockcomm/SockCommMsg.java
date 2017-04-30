package research.bwsharingapp.sockcomm;

import java.io.Serializable;

/**
 * Created by alex on 4/30/17.
 */

public class SockCommMsg<T> implements Serializable {
    public int type;
    public T data;

    public SockCommMsg() {
        type = 0;
        data = null;
    }

    public SockCommMsg(int type, T data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" + type + ", " + data + "}";
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
