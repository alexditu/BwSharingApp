package research.bwsharingapp.account;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import research.bwsharingapp.R;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.sockcomm.CommConstants;

import static research.bwsharingapp.MainActivity.CLIENT_ID;

public class AccountManagementActivity extends AppCompatActivity {
    private final static String TAG = "AccountManagementActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        final Activity act = this;
        Button btn = (Button) findViewById(R.id.register_user_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserTask task = new RegisterUserTask(act);
                task.execute(getApplicationContext());
            }
        });
    }


}
