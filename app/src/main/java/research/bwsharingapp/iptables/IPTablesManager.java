package research.bwsharingapp.iptables;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by alex on 5/20/17.
 */

public class IPTablesManager {
    public static final String TAG = "IPTablesManager";
    public static final String IPTABLES_BIN = "/system/bin/iptables";

    public static final String R_SET_FW_INBOUND_CMD     = "-t filter -I FORWARD 1 -d %s/32 -j LOG --log-prefix %s__INBOUND";
    public static final String R_SET_FW_OUTBOUND_CMD    = "-t filter -I FORWARD 1 -s %s/32 -j LOG --log-prefix %s_OUTBOUND";
    public static final String C_SET_INBOUND_CMD        = "-t filter -I INPUT 1 ! -s %s/24 -j LOG --log-prefix %s__INBOUND";
    public static final String C_SET_OUTBOUND_CMD       = "-t filter -I OUTPUT 1 ! -d %s/24 -j LOG --log-prefix %s_OUTBOUND";

    public static final String R_DEL_FW_INBOUND_CMD     = "-t filter -D FORWARD -d %s/32 -j LOG --log-prefix %s__INBOUND";
    public static final String R_DEL_FW_OUTBOUND_CMD    = "-t filter -D FORWARD -s %s/32 -j LOG --log-prefix %s_OUTBOUND";
    public static final String C_DEL_INBOUND_CMD        = "-t filter -D INPUT ! -s %s/24 -j LOG --log-prefix %s__INBOUND";
    public static final String C_DEL_OUTBOUND_CMD       = "-t filter -D OUTPUT ! -d %s/24 -j LOG --log-prefix %s_OUTBOUND";

    public static final String R_GET_FW_STATS           = "-t filter -L FORWARD -nvx --line-numbers";
    public static final String C_GET_INBOUND_STATS      = "-t filter -L INPUT --line-numbers -nvx";
    public static final String C_GET_OUTBOUND_STATS     = "-t filter -L OUTPUT --line-numbers -nvx";

    public static final String R_ZERO_FORWARD_STATS     = "-t filter -Z FORWARD";
    public static final String C_ZERO_INBOUND_STATS     = "-t filter -Z INPUT";
    public static final String C_ZERO_OUTBOUND_STATS    = "-t filter -Z OUTPUT";

    private static final String ENABLE_IP_FORWARDING        = "echo 1 > /proc/sys/net/ipv4/ip_forward";
    private static final String DISABLE_IP_FORWARDING       = "echo 0 > /proc/sys/net/ipv4/ip_forward";
    private static final String GET_IP_FORWARDING_STATUS    = "cat /proc/sys/net/ipv4/ip_forward";
    /*
     * TODO: use rmnet0 for real phones and eth0 for emulators
     * private static final String SET_MASQUERADE_CMD          = "-I POSTROUTING 1 -t nat -o rmnet0 -j MASQUERADE";
     */
    private static final String SET_MASQUERADE_CMD          = "-I POSTROUTING 1 -t nat -o eth1 -j MASQUERADE";
    private static final String SET_DNS_CMD                 = "setprop net.dns1 8.8.8.8";
    private static final String SET_DEFAULT_ROUTE_CMD       = "ip r a default via %s";

    public IPTablesManager() {
    }

    public static TrafficInfo getInputStats(String id) throws IOException, ExecFailedException, IPTablesParserException {
        String cmd = String.format(C_GET_INBOUND_STATS, id);
        ArrayList<String> output = execCmdAndReadOutput(cmd);

        TrafficInfo info = IPTablesParser.parseInbound(output, id);
        return info;
    }

    public static TrafficInfo getOutputStats(String id) throws IOException, ExecFailedException, IPTablesParserException {
        String cmd = String.format(C_GET_OUTBOUND_STATS, id);
        ArrayList<String> output = execCmdAndReadOutput(cmd);

        TrafficInfo info = IPTablesParser.parseOutbound(output, id);
        return info;
    }


    public static TrafficInfo[] getFwStats(String id) throws IOException, ExecFailedException, IPTablesParserException {
        String cmd = String.format(R_GET_FW_STATS, id);
        ArrayList<String> output = execCmdAndReadOutput(cmd);

        TrafficInfo inbound     = IPTablesParser.parseFwInbound(output, id);
        TrafficInfo outbound    = IPTablesParser.parseFwOutbound(output, id);
        TrafficInfo result[]    = {inbound, outbound};
        return result;
    }

    public static void setClientIptablesRules(String routerIp, String id) throws ExecFailedException {
        try {
            execIptablesCmd(String.format(C_DEL_INBOUND_CMD, routerIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + C_SET_INBOUND_CMD + "' not set");
        }
        try {
            execIptablesCmd(String.format(C_DEL_OUTBOUND_CMD, routerIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + C_SET_OUTBOUND_CMD + "' not set");
        }
        execIptablesCmd(String.format(C_SET_INBOUND_CMD, routerIp, id));
        execIptablesCmd(String.format(C_SET_OUTBOUND_CMD, routerIp, id));
    }

    public static void setRouterIptablesRules(String clientIp, String id) throws ExecFailedException {
        try {
            execIptablesCmd(String.format(R_DEL_FW_INBOUND_CMD, clientIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + R_SET_FW_INBOUND_CMD + "' not set");
        }
        try {
            execIptablesCmd(String.format(R_DEL_FW_OUTBOUND_CMD, clientIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + R_SET_FW_OUTBOUND_CMD + "' not set");
        }
        execIptablesCmd(String.format(R_SET_FW_INBOUND_CMD, clientIp, id));
        execIptablesCmd(String.format(R_SET_FW_OUTBOUND_CMD, clientIp, id));
    }

    public static void clearForwardStats() throws ExecFailedException {
        execIptablesCmd(R_ZERO_FORWARD_STATS);
    }

    public static void clearInputStats() throws ExecFailedException {
        execIptablesCmd(C_ZERO_INBOUND_STATS);
    }

    public static void clearOutputStats() throws ExecFailedException {
        execIptablesCmd(C_ZERO_OUTBOUND_STATS);
    }

    public static void deleteRouterIptablesRules(String clientIp, String id) throws ExecFailedException {
        try {
            execIptablesCmd(String.format(R_DEL_FW_INBOUND_CMD, clientIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + R_SET_FW_INBOUND_CMD + "' not set");
        }
        try {
            execIptablesCmd(String.format(R_DEL_FW_OUTBOUND_CMD, clientIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + R_SET_FW_OUTBOUND_CMD + "' not set");
        }
    }

    public static void deleteClientIptablesRules(String routerIp, String id) throws ExecFailedException {
        try {
            execIptablesCmd(String.format(C_DEL_INBOUND_CMD, routerIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + C_SET_INBOUND_CMD + "' not set");
        }
        try {
            execIptablesCmd(String.format(C_DEL_OUTBOUND_CMD, routerIp, id));
        } catch (ExecFailedException e) {
            Log.d(TAG, "Rule '" + C_SET_OUTBOUND_CMD + "' not set");
        }
    }





    private static String getStringCmd(String cmd[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cmd.length; i++) {
            sb.append(cmd[i]);
            if (i != cmd.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private static Process exec(final String cmd[]) throws ExecFailedException {
        Process p = null;
        int ret = -1001;

        Runtime runtime = Runtime.getRuntime();
        try {
            p = runtime.exec(cmd);
            ret = p.waitFor();
            Log.d(TAG, "p.waitFor() ret: " + ret);
        } catch (IOException e) {
            Log.e(TAG, "Exception while running exec with cmd: '" + getStringCmd(cmd) + "' : " + e);
            ret = -100;
        } catch (InterruptedException e) {
            Log.e(TAG, "Exception while waiting for process to finnish: " + e);
            ret = -101;
        }

        if (ret != 0) {
            Log.e(TAG, "Failed to exec cmd: '" + getStringCmd(cmd) + "', exit code: " + ret);
            throw new ExecFailedException(ret);
        } else {
            return p;
        }
    }

    /*
     * It turns out that exec(cmdArray[]) does not work properly for iptables command
     */
    private static Process exec(final String cmd) throws ExecFailedException {
        Process p = null;
        int ret = -1001;

        Runtime runtime = Runtime.getRuntime();
        try {
            p = runtime.exec("su", null, null);
            OutputStream stdout = p.getOutputStream();
            stdout.write(cmd.getBytes());
            stdout.flush();
            stdout.close();
            ret = p.waitFor();
            Log.d(TAG, "p.waitFor() ret: " + ret);
        } catch (IOException e) {
            Log.e(TAG, "Exception while running exec with cmd: '" + cmd + "' : " + e);
            ret = -100;
        } catch (InterruptedException e) {
            Log.e(TAG, "Exception while waiting for process to finnish: " + e);
            ret = -101;
        }

        if (ret != 0) {
            Log.e(TAG, "Failed to exec cmd: '" + cmd + "', exit code: " + ret);
            throw new ExecFailedException(ret);
        } else {
            return p;
        }
    }

    private static ArrayList<String> readProcessOutput(Process proc) throws IOException {
        ArrayList<String> output = new ArrayList<>();

        InputStream in = proc.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        while (line != null) {
            output.add(line);
            line = br.readLine();
        }
        return output;
    }

    private static void printOutput(ArrayList<String> output) {
        for (String i : output) {
            Log.d(TAG, i);
        }
    }

    private static Process execCmd(String bin, String args) throws ExecFailedException {
        String cmd = bin + " " + args;
        Process proc = exec(cmd);
        return proc;
    }

    private static ArrayList<String> execCmdAndReadOutput(String args) throws IOException, ExecFailedException {
        Process proc = execCmd(IPTABLES_BIN, args);
        ArrayList<String> output = readProcessOutput(proc);

        if (output == null) {
            // TODO: print error output stream for more info
            Log.e(TAG, "getInputStats: failed to exec cmd: '" + IPTABLES_BIN + " " + args + "' output stream was empty!");
            throw new ExecFailedException(-105);
        }
        return output;
    }

    public static void printInputStats(String id) throws IOException, ExecFailedException {
        String cmd = String.format(C_GET_INBOUND_STATS, id);
        ArrayList<String> output = execCmdAndReadOutput(cmd);
        printOutput(output);
    }
    public static void printOutputStats(String id) throws IOException, ExecFailedException {
        String cmd = String.format(C_GET_OUTBOUND_STATS, id);
        ArrayList<String> output = execCmdAndReadOutput(cmd);
        printOutput(output);
    }
    public static void printForwardStats(String id) throws IOException, ExecFailedException {
        String cmd = String.format(R_GET_FW_STATS, id);
        ArrayList<String> output = execCmdAndReadOutput(cmd);
        printOutput(output);
    }

    private static void enableIPForward() throws ExecFailedException {
        String cmdArray[] = new String[] {"su", "-c", ENABLE_IP_FORWARDING};
        Process proc = exec(cmdArray);
    }
    private static void disableIPForward() throws ExecFailedException {
        String cmdArray[] = new String[] {"su", "-c", DISABLE_IP_FORWARDING};
        Process proc = exec(cmdArray);
    }
    private static String getIPForwardStatus() throws ExecFailedException, IOException {
        String cmdArray[] = new String[] {"su", "-c", GET_IP_FORWARDING_STATUS};
        Process proc = exec(cmdArray);
        ArrayList<String> output = readProcessOutput(proc);
        if (output != null && output.size() > 0)
            return output.get(0);
        else
            return "-1";
    }

    private static void execIptablesCmd(final String args) throws ExecFailedException {
        execCmd(IPTABLES_BIN, args);
    }

    private static void setMasquerade() throws ExecFailedException {
        execIptablesCmd(SET_MASQUERADE_CMD);
    }

    private static void setDNS() throws ExecFailedException {
        String cmdArray[] = new String[] {"su", "-c", SET_DNS_CMD};
//        Process proc = exec(cmdArray);
        Process proc = execCmd(SET_DNS_CMD, "");
    }

    private static void setDefaultRoute(String gatewayIp) throws ExecFailedException {
        String cmd = String.format(SET_DEFAULT_ROUTE_CMD, gatewayIp);
        String cmdArray[] = new String[] {"su", "-c", cmd};
        Process proc = execCmd(cmd, "");
    }

    public static void clientEnableNetworking(String routerIp) throws ExecFailedException {
        setDefaultRoute(routerIp);
        setDNS();
    }

    public static void routerEnableNetworking() throws ExecFailedException {
        enableIPForward();
        setMasquerade();
    }


}
