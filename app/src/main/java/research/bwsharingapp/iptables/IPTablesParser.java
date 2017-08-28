package research.bwsharingapp.iptables;

import android.util.Log;

import java.util.ArrayList;

import research.bwsharingapp.proto.kb.TrafficInfo;

/**
 * Created by alex on 5/21/17.
 */

public class IPTablesParser {
    private static final String TAG = "IPTablesParser";

    public IPTablesParser() {

    }

    public static TrafficInfo parseFwInbound(ArrayList<String> outputLines, String id) throws IPTablesParserException {
        String identifier = id + "__INBOUND";
        return parse(outputLines, identifier);
    }

    public static TrafficInfo parseFwOutbound(ArrayList<String> outputLines, String id) throws IPTablesParserException {
        String identifier = id + "_OUTBOUND";
        return parse(outputLines, identifier);
    }

    public static TrafficInfo parseInbound(ArrayList<String> outputLines, String id) throws IPTablesParserException {
        String identifier = id + "__INBOUND";
        return parse(outputLines, identifier);
    }

    public static TrafficInfo parseOutbound(ArrayList<String> outputLines, String id) throws IPTablesParserException {
        String identifier = id + "_OUTBOUND";
        return parse(outputLines, identifier);
    }

    /**
     * Parses output and returns a TrafficInfo object if identifier is found throughout the lines.
     * If the identifier is not found or an error occurs it returns null
     * @param identifier
     * @return              success: a TrafficInfo object
     *                      failure: null
     */
    private static TrafficInfo parse(ArrayList<String> outputLines, String identifier) throws IPTablesParserException {
        TrafficInfo info = null;

        String line = null;
        for (String i : outputLines) {
            if (i.contains(identifier)) {
                line = i;
            }
        }
        if (line == null) {
            String err = "parse: outputLines does not contain identifier: " + identifier;
            Log.e(TAG, err);
            throw new IPTablesParserException(err, -1);
        }

        line = line.trim().replaceAll(" +", " ");
        String parts[] = line.split(" ");

        if (parts.length < 10) {
            String err = "invalid line format: " + line;
            Log.e(TAG, err);
            throw new IPTablesParserException(err, -2);
        }

        TrafficInfo.Builder builder = TrafficInfo.newBuilder();
        builder.setPkts(Long.parseLong(parts[1]));
        builder.setBytes(Long.parseLong(parts[2]));
        builder.setSrc(parts[8]);
        builder.setDst(parts[9]);
        info = builder.build();

        return info;
    }
}
