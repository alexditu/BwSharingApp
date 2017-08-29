package research.bwsharingapp.sockcomm.msg;

/**
 * Created by alex on 5/28/17.
 */

public enum MsgType {
    HELLO(0),
    HELLO_REPLY(3),
    IOU_1(1),
    IOU_2(2),

    ERROR(-1);

    private final int id;
    MsgType(int id) { this.id = id; };


    MsgType() {
        id = -1;
    }

}
