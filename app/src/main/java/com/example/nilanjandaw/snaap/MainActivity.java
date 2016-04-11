package com.example.nilanjandaw.snaap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @Bind(R.id.band_address) EditText bandAddress;
    @Bind(R.id.tag_address) EditText tagAddress;
    @Bind(R.id.show_list) TextView showList;
    @Bind(R.id.connect) Button connect;
    @Bind(R.id.lost_id) Button lost_id;
    @Bind(R.id.tag_button) Button tagButton;
    private BluetoothComm communicator;
    private BluetoothSocket socket = null;
    public static final int REQUEST_ENABLE_BT = 4;
    private ReceiverTask receiverTask;
    private SenderTask senderTask;
    public String timestamp;
    public String mac_id;
    JsonUploader jsonUploader;
    public static String addressString = "";
    public static int connectionStatus = -1, semaphore = 0;
    public static boolean lostRaider = false;
//    TimeStamp timestamp;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        final String[] LOCATION_PERMS={
                Manifest.permission_group.PHONE,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission_group.STORAGE,
                Manifest.permission_group.LOCATION,
                Manifest.permission.WRITE_GSERVICES
        };
        requestLocationPermission();
        showList.setText("");
        tagButton = (Button) findViewById(R.id.tag_button);
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressString += tagAddress.getText().toString() + "#";
                Log.d("Address String", addressString);
                String deviceList = showList.getText().toString();
                showList.setText(deviceList + "\n" + tagAddress.getText().toString());
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = bandAddress.getText().toString();
                if (connectionStatus == -1)
                    new Connect().execute(address);
                else
                    showToast("Your device is already connected to a device");
            }
        });

        lost_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Helper help = new Helper(getBaseContext());
                double latitude = 0, longitude = 0;
                showToast(getTimestamp());
                showToast(getMacID());
                // check if GPS enabled
                if(help.canGetLocation()) {

                    latitude = help.getLatitude();
                    longitude = help.getLongitude();
                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    help.showSettingsAlert();
                }
                jsonUploader = new JsonUploader();
                if(!jsonUploader.validate()) {
                    Toast.makeText(getBaseContext(), "No Data Found!!", Toast.LENGTH_LONG).show();
                }else {
                    // call AsynTask to perform network operation on separate thread
                    jsonUploader.new HttpAsyncTask().execute("http://192.168.0.104:3000/api/Bands",
                            "Band0HC-05",
                            help.getMacID(),
                            "Tag0NRF-01",
                            help.getTimeStamp(),
                            latitude + "",
                            longitude + ""
                    );
                }
                Intent i = new Intent(getApplicationContext(), Lost_Tag.class);
                startActivity(i);
            }
        });



    }

    @Override
    public void onResume(){
        super.onResume();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d("MainActivity", "Device does not support Bluetooth");
        }
        else{
            //Device supports BT
            if (!mBluetoothAdapter.isEnabled()){
                //if Bluetooth not activated, then request it
                showToast("Please turn your bluetooth ON");
            }
            else{
                //BT activated, then initiate the BtInterface object to handle all BT communication
                communicator = new BluetoothComm();
            }
        }
    }

    private void startCommunication() {
        receiverTask = new ReceiverTask();
        receiverTask.execute(socket);
        senderTask = new SenderTask();
        senderTask.execute(socket);
        connectionStatus = 1;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (senderTask != null)
            senderTask.cancel(true);
        if (receiverTask != null)
            receiverTask.cancel(true);
        if (communicator != null)
            communicator.close();
    }

    public class Connect extends AsyncTask<String, Void, BluetoothSocket> {

        @Override
        protected BluetoothSocket doInBackground(String... params) {

            String address = params[0];
            BluetoothSocket socket;
            socket = communicator.connect(address);
            return socket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket bluetoothSocket) {
            super.onPostExecute(bluetoothSocket);
            socket = bluetoothSocket;
            if (socket != null) {
                showToast("Connection Successful");
                startCommunication();
                connectionStatus = 0;
            }
            else
                showToast("Connection Failed");
        }
    }

    public class ReceiverTask extends AsyncTask<BluetoothSocket, Void, String> {

        @Override
        protected String doInBackground(BluetoothSocket... params) {
            while (socket != null) {
                String stringReceived = communicator.receiveData(params[0]);
                parseData(stringReceived);
                Log.d("receiverTask", stringReceived);
                if (!Objects.equals(stringReceived, ""))
                    semaphore = 0;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (receiverTask.isCancelled())
                    break;
            }
            return null;
        }
    }

    private void parseData(String stringReceived) {

        int tagStatusReply = Integer.parseInt(stringReceived);
        for (int i = 0; i < stringReceived.length(); i++) {
            int tagStat = tagStatusReply & (0b1 << i);
            if (tagStat > 0)
                Log.d("Tag id", (stringReceived.length() - i) + "Found");
            else {
                Log.e("WARNING", "Tag " + (stringReceived.length() - i) + "lost");
                showToast("WARNING " + "Tag " + (stringReceived.length() - i) + "lost");
            }

        }

        if (lostRaider) {
            int lostTagStatus = tagStatusReply & (0b1);
            if (lostTagStatus > 0)
                Log.d("Lost Tag", "Karan Arjun Mil Gaya");
        }
    }

    public class SenderTask extends AsyncTask<BluetoothSocket, Void, String> {

        @Override
        protected String doInBackground(BluetoothSocket... params) {
            try {
                while (params[0] != null && !addressString.equalsIgnoreCase("")) {
                    if (semaphore == 0) {
                        addressString = "\\SOM" + addressString + "\\EOM";
                        byte msg[] = addressString.getBytes();
                        communicator.sendData(msg, params[0].getOutputStream());
                        //message = "";
                        Log.d("message", addressString);
                        semaphore = 1;
                    }
                    Thread.sleep(1000);
                    if (senderTask.isCancelled())
                        break;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return addressString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("Data Sent", s);
        }
    }

    private void showToast(String s) {
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }

    public String getTimestamp() {
        Calendar c1 = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("d/M/yy h:m:s a");
        timestamp = simpledateformat.format(c1.getTime());
        Log.d("Time Stamp..........", timestamp);
        return timestamp;
    }
    public String getMacID(){
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        mac_id = wInfo.getMacAddress();
        return mac_id;
    }

    private void requestLocationPermission() {

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(findViewById(R.id.coordinator_layout), "We need location services", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Allow", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission_group.LOCATION},
                                    10);
                        }
                    })
                    .show();
        } else {

            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission_group.LOCATION},
                    10);
        }
        // END_INCLUDE(Location_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && grantResults[0] == 10) {
            Log.d("Permission", "Granted");
        }
    }
}
