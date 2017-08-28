package research.bwsharingapp.iptables;

import java.io.Serializable;

/**
 * Created by alex on 5/21/17.
 */

public class TrafficInfo2 implements Serializable {
    public long pkts;
    public long bytes;
    public String src;
    public String dst;

    public TrafficInfo2() {
    }

    public TrafficInfo2(long pkts, long bytes, String src, String dst) {
        this.pkts = pkts;
        this.bytes = bytes;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return "{" + pkts + ",\t" + bytes + ",\t" + src + ",\t" + dst + "}";
    }

    public long getPkts() {
        return pkts;
    }

    public void setPkts(long pkts) {
        this.pkts = pkts;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }
}
