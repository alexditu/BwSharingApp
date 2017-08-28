package research.bwsharingapp.account;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import research.bwsharingapp.R;

import static research.bwsharingapp.account.AccountManagementActivity.USER_REGISTERED_KEY;

/**
 * Created by alex on 8/28/17.
 */

public class RegisterUserTask extends AsyncTask<Activity, Void, Integer> {
    private Activity activity;
    public RegisterUserTask(Activity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result >= 0) {
            TextView userStatus = (TextView) activity.findViewById(R.id.user_status_tv);
            userStatus.setText("User status: REGISTERED");

            Button btn = (Button) activity.findViewById(R.id.register_user_btn);
            btn.setEnabled(false);

            SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(USER_REGISTERED_KEY, true);
            editor.commit();

            Toast.makeText(activity, "User registered successfully!", Toast.LENGTH_LONG);
        } else {
            Toast.makeText(activity, "User registration failed!", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected Integer doInBackground(Activity... params) {
        RegisterUserAL appLogic = new RegisterUserAL(params[0]);
        int ret = appLogic.registerUser();
        return ret;
    }
}
