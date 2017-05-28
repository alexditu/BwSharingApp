package research.bwsharingapp.sockcomm;

/**
 * Created by alex on 5/28/17.
 */

public enum MsgType {
    HELLO(0),
    IOU_1(1),
    IOU_2(2),

    ERROR(-1);

    private final int id;
    MsgType(int id) { this.id = id; };


    MsgType() {
        id = -1;
    }

}
