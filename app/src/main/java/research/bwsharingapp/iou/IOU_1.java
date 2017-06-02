package research.bwsharingapp.iou;

import java.io.Serializable;

import research.bwsharingapp.iptables.TrafficInfo;

/**
 * Created by alex on 6/1/17.
 */

public class IOU_1 implements Serializable {
    TrafficInfo input;
    TrafficInfo output;

    public IOU_1(TrafficInfo input, TrafficInfo output) {
        this.input = input;
        this.output = output;
    }

    public IOU_1() {}

    @Override
    public String toString() {
        return "[in: " + input + ", out: " + output + "]";
    }

    public TrafficInfo getInput() {
        return input;
    }

    public void setInput(TrafficInfo input) {
        this.input = input;
    }

    public TrafficInfo getOutput() {
        return output;
    }

    public void setOutput(TrafficInfo output) {
        this.output = output;
    }
}
