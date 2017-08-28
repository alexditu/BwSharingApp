package research.bwsharingapp.account;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import research.bwsharingapp.R;

/**
 * Created by alex on 8/28/17.
 */

public class RegisterUserTask extends AsyncTask<Context, Void, Integer> {
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
            Toast.makeText(activity, "User registered successfully!", Toast.LENGTH_LONG);
        } else {
            Toast.makeText(activity, "User registration failed!", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected Integer doInBackground(Context... params) {
        RegisterUserAL appLogic = new RegisterUserAL(params[0]);
        int ret = appLogic.registerUser();
        return ret;
    }
}
