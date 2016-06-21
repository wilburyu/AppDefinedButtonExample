package appdefinedbuttonexample.odg.com.appdefinedbuttonexample.appbuttons;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.R;

/**
 * Created by wilbur.yu on 6/21/16.
 */
public class ButtonDefinedActivity extends Activity {
    private static final String TAG = ButtonDefinedActivity.class.getName();

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra("EXTRA");
            Toast.makeText(ButtonDefinedActivity.this, "Command received == " + command, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_defined_layout);

        //Register receiver to receive action, "com.dg.sub_intent"
        //  This action needs to be defined in the button xml file as well.
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.dg.sub_intent");
        filter.addAction("com.odg.intent");
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
