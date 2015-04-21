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
import android.os.AsyncTask;
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

    final static String DISCONNECT = "DISCONNECT";

    private final BroadcastReceiver mDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(BTSERVICE, "Cancelled");
                try {
                    Intent disconnectIntent = new Intent();
                    disconnectIntent.setAction("DISCONNECT");
                    sendBroadcast(disconnectIntent);
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    Toast.makeText(getApplicationContext(), "Disconnected from " + DeviceName, Toast.LENGTH_LONG).show();
                    v.vibrate(500);
                    r.play();
                    new sendEmail().execute();
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

    private class sendEmail extends AsyncTask<String, Void, Void> {

        protected void onPreExecute(){
            return;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                GMailSender sender = new GMailSender("securealert@gmail.com", "Alongpassword101");
                sender.sendMail("Secure Alert Disconnected",
                        "This email is being sent to alert you that your bluetooth connection has disconnected.",
                        "securealert@gmail.com",
                        "chu.300@osu.edu");
            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
            return null;
        }



        protected Void onProgressUpdate(){
            return null;
        }

        protected Void onPostExecute(){
            return null;
        }

    }
}