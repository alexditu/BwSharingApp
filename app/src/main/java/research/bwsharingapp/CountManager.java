package research.bwsharingapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by alex on 8/22/16.
 */
public class CountManager {
    private static String TAG = "CountManager";

    public void runIpTables() {
        try {
            Process process = Runtime.getRuntime().exec("su iptables -nvxL");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            process.getErrorStream();


            String line = null;
            do {
                line = br.readLine();
                Log.d(TAG, "line: " + line);
            } while (line != null);

            br.close();

            br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            do {
                line = br.readLine();
                Log.d(TAG, "error line: " + line);
            } while (line != null);

            br.close();

            Log.d(TAG, "Waiting for process to finnish...");
            process.waitFor();
            Log.d(TAG, "Process finished");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
