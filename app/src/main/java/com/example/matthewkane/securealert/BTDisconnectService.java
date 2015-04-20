package com.example.matthewkane.securealert;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ryanchu on 4/20/15.
 */
public class BTDisconnectService extends Service {

    private final String BTSERVICE = "BTSERVICE";

    private String DeviceName;

    private final BroadcastReceiver mDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(BTSERVICE, "Cancelled");
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    Toast.makeText(getApplicationContext(), "Disconnected from " + DeviceName, Toast.LENGTH_LONG).show();
                    v.vibrate(500);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter disconnectFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mDisconnectReceiver, disconnectFilter);
        Log.d(BTSERVICE, "Service: onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        DeviceName = intent.getStringExtra("DeviceName");
        Log.d(BTSERVICE, "Service: onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(BTSERVICE, "Service: onDestroy");
        unregisterReceiver(mDisconnectReceiver);
        super.onDestroy();
    }
}