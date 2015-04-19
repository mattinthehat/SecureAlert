package com.example.matthewkane.securealert;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SplashActivity extends ActionBarActivity{

    //Ryan test comment

    //local variables
    int REQUEST_ENABLE_BT;
    ArrayList<String> items = new ArrayList<String>();
    ArrayAdapter<String> mBTList;

    //called when 'find devices' button is clicked
    public void findDevices(View v){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //clear the arrayadapter to refresh with new devices
        mBTList.clear();
        //start finding new devices
        bluetoothAdapter.startDiscovery();

    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("BTFOUND", "DEVICE FOUND");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mBTList.add(device.getName() + "\n" + device.getAddress());

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //initialize arrayadapter and listview
        mBTList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        ListView lv = (ListView)findViewById(R.id.BTListView);

        //connect arrayadapter and listview
        lv.setAdapter(mBTList);

        //implement itemclicklistener for listview
        //position -> the selected item's position in the arrayadapter
        //view -> view that was selected from the list
        //id -> row number of item that was selected
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                TextView selectedView = (TextView) view;
                Toast.makeText(getApplicationContext(), mBTList.getItem(position), Toast.LENGTH_LONG).show();
            }
        });

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter); // Don't forget to unregister during onDestroy
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
