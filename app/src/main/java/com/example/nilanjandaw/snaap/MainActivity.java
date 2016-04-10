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

public class MainActivity extends Activity {

    private EditText addressBar;
    private TextView showList;
    private BluetoothComm communicator;
    private BluetoothSocket socket = null;
    public static final int REQUEST_ENABLE_BT = 4;
    private ReceiverTask receiverTask;
    private SenderTask senderTask;
    public String timestamp;
    public String mac_id;
    Fields_Details fields_details;
    JsonUploader jsonUploader;
    GPSTracker gps;
//    TimeStamp timestamp;
    //@TargetApi(Build.VERSION_CODES.MNC)
    //@Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button connect;
        Button lost_id;
        //final String[] LOCATION_PERMS={
          //      Manifest.permission.ACCESS_FINE_LOCATION
        //};
        //requestPermissions(LOCATION_PERMS, 10);


                addressBar = (EditText) findViewById(R.id.address_bar);
        showList = (TextView) findViewById(R.id.show_list);
        showList.setText("");
        connect = (Button) findViewById(R.id.connect);
        lost_id = (Button) findViewById(R.id.lost_id);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = addressBar.getText().toString();
                String deviceList = showList.getText().toString();
                showList.setText(deviceList + "\n" + addressBar.getText().toString());
                new Connect().execute(address);
            }
        });

        lost_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Lost_Tag.class);
                startActivity(i);
            }
        });

        fields_details = new Fields_Details();
//        timestamp = new TimeStamp(MainActivity.this);
        fields_details.setTimestamp(getTimestamp());
        showToast(getTimestamp());
        fields_details.setMacId(getMacID());
        showToast(getMacID());
        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            fields_details.setLatitude(String.valueOf(latitude));
            fields_details.setLongitude(String.valueOf(longitude));

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        jsonUploader = new JsonUploader();
        if(!jsonUploader.validate()) {
            Toast.makeText(getBaseContext(), "No Data Found!!", Toast.LENGTH_LONG).show();
        }else {
            Log.d("POST",fields_details.getMacId());
            jsonUploader.new HttpAsyncTask().execute("http://192.168.1.4:3000/api/Bands", fields_details.getLongitude().toString(), fields_details.getLatitude(), fields_details.getMacId(), fields_details.getTimestamp());    // call AsynTask to perform network operation on separate thread

        }

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
        senderTask.cancel(true);
        receiverTask.cancel(true);
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
            if (socket != null)
                showToast("Connection Successful");
            else
                showToast("Connection Failed");
            startCommunication();
        }
    }

    public class ReceiverTask extends AsyncTask<BluetoothSocket, Void, String> {

        @Override
        protected String doInBackground(BluetoothSocket... params) {
            while (socket != null) {
                String stringReceived = communicator.receiveData(params[0]);
//                Integer stringInteger = Integer.parseInt(stringReceived);
//                stringInteger = stringInteger&00000001;
//                if (stringInteger>0){
//
//
//                }
                Log.d("receiverTask", stringReceived);
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

    public class SenderTask extends AsyncTask<BluetoothSocket, Void, String> {

        @Override
        protected String doInBackground(BluetoothSocket... params) {
            try {
                String message = "Test String";// message to be sent
                while (params[0] != null && !message.equalsIgnoreCase("")) {
                    byte msg[] = message.getBytes();
                    communicator.sendData(msg, params[0].getOutputStream());
                    //message = "";
                    Log.d("message", message);
                    Thread.sleep(50);
                    if (senderTask.isCancelled())
                        break;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return "Execution Done";
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
}
