package research.bwsharingapp.account;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import research.bwsharingapp.R;
import research.bwsharingapp.iptables.IPTablesManager;
import research.bwsharingapp.proto.kb.KibbutzGrpc;
import research.bwsharingapp.sockcomm.CommConstants;

import static research.bwsharingapp.MainActivity.CLIENT_ID;
import static research.bwsharingapp.account.PKIManager.generateKeys;

public class AccountManagementActivity extends AppCompatActivity {
    private final static String TAG = "AccountManagementAct";

    public final static String USERNAME_KEY         = "USERNAME_KEY";
    public final static String USER_REGISTERED_KEY  = "USER_REGISTERED_KEY";
    public final static String ACCOUNT_PREF_NAME    = "AccountManagementActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        setOnClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreUserState();
    }

    private void setOnClickListeners() {
        final Activity act = this;
        Button btn = (Button) findViewById(R.id.register_user_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserTask task = new RegisterUserTask(act);
                task.execute(act);
            }
        });

        Button createUserBtn = (Button) findViewById(R.id.create_user_btn);
        createUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
    }

    private void createUser() {
        try {
            generateKeys(getApplicationContext());
            saveUsername();
            disableUsernameInputText();
            disableCreateUserButton();
            enableRegisterUserButton();
            setUserStatus("CREATED");
            Toast.makeText(this, "User creation success", Toast.LENGTH_LONG);
        } catch (Exception e) {
            Log.e(TAG, "Key generation failed: " + e);
            Toast.makeText(this, "User creation failed", Toast.LENGTH_LONG);
        }
    }

    private void setUserStatus(String status) {
        TextView tv = (TextView)findViewById(R.id.user_status_tv);
        tv.setText("User status: " + status);
    }

    private void saveUsername() {
        EditText usernameEt = (EditText) findViewById(R.id.username_et);

        SharedPreferences sharedPref = getSharedPreferences(ACCOUNT_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USERNAME_KEY, usernameEt.getText().toString());
        editor.commit();
    }

    private void restoreUserState() {
        SharedPreferences sharedPref = getSharedPreferences(ACCOUNT_PREF_NAME, Context.MODE_PRIVATE);
        String username = sharedPref.getString(USERNAME_KEY, null);

        if (username == null) {
            Log.d(TAG, "User not created");
            setUserStatus("NOT CREATED");
            disableRegisterUserButton();
        } else {
            EditText usernameEt = (EditText) findViewById(R.id.username_et);
            usernameEt.setText(username);

            disableUsernameInputText();
            disableCreateUserButton();

            boolean isUserRegistered = sharedPref.getBoolean(USER_REGISTERED_KEY, false);
            if (isUserRegistered) {
                setUserStatus("REGISTERED");
                disableRegisterUserButton();
            } else {
                setUserStatus("CREATED");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
    }

    private void disableCreateUserButton() {
        Button btn = (Button) findViewById(R.id.create_user_btn);
        btn.setEnabled(false);
    }

    private void enableCreateUserButton() {
        Button btn = (Button) findViewById(R.id.create_user_btn);
        btn.setEnabled(true);
    }

    private void disableRegisterUserButton() {
        Button btn = (Button) findViewById(R.id.register_user_btn);
        btn.setEnabled(false);
    }

    private void enableRegisterUserButton() {
        Button btn = (Button) findViewById(R.id.register_user_btn);
        btn.setEnabled(true);
    }

    private void disableUsernameInputText() {
        EditText usernameEt = (EditText) findViewById(R.id.username_et);
        usernameEt.setFocusable(false);
        usernameEt.setFocusableInTouchMode(false);
    }

    private void enableUsernameInputText() {
        EditText usernameEt = (EditText) findViewById(R.id.username_et);
        usernameEt.setFocusable(true);
        usernameEt.setFocusableInTouchMode(true);
    }


}
