package research.bwsharingapp.iou;

import java.io.Serializable;

import research.bwsharingapp.iptables.TrafficInfo2;
import research.bwsharingapp.proto.kb.TrafficInfo;


/**
 * Created by alex on 6/1/17.
 */

public class IOU_1 implements Serializable {
    TrafficInfo2 input;
    TrafficInfo2 output;

    public IOU_1(TrafficInfo input, TrafficInfo output) {
        this.input = new TrafficInfo2();
        this.input.bytes = input.getBytes();
        this.input.pkts = input.getPkts();
        this.input.src = input.getSrc();
        this.input.dst = input.getDst();

        this.output = new TrafficInfo2();
        this.output.bytes = output.getBytes();
        this.output.pkts = output.getPkts();
        this.output.src = output.getSrc();
        this.output.dst = output.getDst();
    }

    public IOU_1() {}

    @Override
    public String toString() {
        return "[in: " + input + ", out: " + output + "]";
    }

    public TrafficInfo2 getInput() {
        return input;
    }

    public void setInput(TrafficInfo2 input) {
        this.input = input;
    }

    public TrafficInfo2 getOutput() {
        return output;
    }

    public void setOutput(TrafficInfo2 output) {
        this.output = output;
    }
}
