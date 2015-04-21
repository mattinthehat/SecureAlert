package com.example.matthewkane.securealert;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SplashActivity extends ActionBarActivity {

    //local variables
    ProgressDialog dialog;
    boolean recRegTypeDevice;
    BluetoothAdapter bluetoothAdapter;
    boolean connected = false;
    UUID myUUID;
    ArrayList<UUID> targetUUIDs;
    Boolean discovering;
    BluetoothDevice selectedDevice;
    Button connectButton;
    int REQUEST_ENABLE_BT;
    ListView lv;
    ArrayList<String> items = new ArrayList<String>();
    ArrayAdapter<String> mBTList;
    ArrayList<BluetoothDevice> devices;

    //called when 'find devices' button is clicked
    public void findDevices(View v){
        //notify user to wait for discovery to finish
        //reregister receiver if necessary
        if(!recRegTypeDevice){
            unregisterReceiver(receiver);
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter); // Don't forget to unregister during onDestroy
            recRegTypeDevice = true;
        }
        //clear the arrayadapter and device arraylist to refresh with new devices
        lv.clearChoices();
        connectButton.setEnabled(false);
        mBTList.clear();
        devices.clear();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //start finding new devices
        bluetoothAdapter.startDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND and ACTION_UUID
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BTCONNECTA", "DEVICE FOUND: " + device.getName());

                // Add the name and address to an array adapter to show in a ListView
                if(!devices.contains(device)) {
                    devices.add(device);
                    mBTList.add(device.getName() + "\n" + device.getAddress());
                }
            }else if (BluetoothDevice.ACTION_UUID.equals(action)){
                //only take non-null uuid sets
                Parcelable[] uuidExtra = null;

                if(intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID) != null) {
                    uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    Log.d("BTCONNECTA", "UUID found for device " + selectedDevice.getName());
                }else{
                    Log.d("BTCONNECTA", "Null UUID found for device " + selectedDevice.getName());
                }
                if(uuidExtra != null) {
                    for (int i = 0; i < uuidExtra.length; i++) {
                        if (!targetUUIDs.contains(UUID.fromString(uuidExtra[i].toString()))) {
                            targetUUIDs.add(UUID.fromString(uuidExtra[i].toString()));
                        }
                    }
                }

                //try to connect with all the found uuids for the given device
                if(!targetUUIDs.isEmpty() && !connected) {
                    String alertMessage;
                    for (int i = 0; i < targetUUIDs.size(); i++) {
                        Log.d("BTCONNECTA", "Trying to connect with UUID: " + targetUUIDs.get(i).toString());
                        myUUID = targetUUIDs.get(i);
                        ConnectThread tryThread = new ConnectThread(selectedDevice);
                        tryThread.start();
                        //wait for connect thread to complete so we know if we are connected
                        try {
                            tryThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (connected) {
                            Intent disconnectIntent = new Intent(getApplicationContext(), BTDisconnectService.class);
                            disconnectIntent.putExtra("DeviceName", selectedDevice.getName());
                            getApplicationContext().startService(disconnectIntent);
                            break;
                        }
                    }
                    dialog.dismiss();
                    if(connected) {
                        alertMessage = "Connection Successful!";
                    }else{
                        alertMessage = "Connection NOT established.\nTry again.";
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(alertMessage)
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else if (action.equals(BTDisconnectService.DISCONNECT)){
                connected = false;
                connectButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //initialize arrayadapter and listview
        mBTList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, items);
        lv = (ListView)findViewById(R.id.BTListView);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        //initialize connectbutton
        connectButton = (Button) findViewById(R.id.connectButton);

        //connect arrayadapter
        lv.setAdapter(mBTList);

        //initialize devices array list
        devices = new ArrayList<BluetoothDevice>();

        //initialize arraylist for uuids
        targetUUIDs = new ArrayList<UUID>();

        //implement itemclicklistener for listview
        //position -> the selected item's position in the arrayadapter
        //view -> view that was selected from the list
        //id -> row number of item that was selected
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                //enable connectButton
                selectedDevice = devices.get(position);
                connectButton.setEnabled(true);
                //select device; be prepared for connect
                //Toast.makeText(getApplicationContext(), devices.get(position).getName(), Toast.LENGTH_SHORT).show();


            }
        });

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(receiver, filter); // Don't forget to unregister during onDestroy
        recRegTypeDevice = true;

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);

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


    public void connectDevice(View v){
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
        IntentFilter uuidFilter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        uuidFilter.addAction(BTDisconnectService.DISCONNECT);
        registerReceiver(receiver, uuidFilter);
        recRegTypeDevice = false;
        Log.d("BTCONNECTA", "Attempting to fetch UUIDS...");
        if(selectedDevice.fetchUuidsWithSdp() == true){
            Log.d("BTCONNECTA", "Connection initiation started...");
        }
        connectButton.setEnabled(false);
        dialog = ProgressDialog.show(this, "Loading", "Connecting to "+ selectedDevice.getName() +", please wait...", true);

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) { }
            mmSocket = tmp;
            Log.d("BTCONNECTA", "SOCKET CREATED");
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.e("CONNERROR1", connectException.toString());
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("CONNERROR2", closeException.toString());
                }
                return;
            }
            Log.d("BTCONNECTA", "CONNECTION ESTABLISHED");
            connected = true;
            // Do work to manage the connection (in a separate thread)

        }
        // Will cancel an in-progress connection, and close the socket
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
