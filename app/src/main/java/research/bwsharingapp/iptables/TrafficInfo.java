package research.bwsharingapp.iptables;

/**
 * Created by alex on 5/21/17.
 */

public class TrafficInfo {
    public String pkts;
    public String bytes;
    public String src;
    public String dst;

    public TrafficInfo() {
    }

    public TrafficInfo(String pkts, String bytes, String src, String dst) {
        this.pkts = pkts;
        this.bytes = bytes;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return "{" + pkts + ",\t" + bytes + ",\t" + src + ",\t" + dst + "}";
    }
}
