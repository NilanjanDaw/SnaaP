package com.example.nilanjandaw.snaap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothComm {

    //Required bluetooth objects
    public BluetoothDevice device = null;
    public BluetoothSocket socket = null;
    public BluetoothAdapter mBluetoothAdapter = null;
    public BufferedReader receiveReader;
    static final String TAG = "Snaap Bluetooth";

    public BluetoothComm() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //when called from the main activity, it sets the connection with the remote device
    public BluetoothSocket connect(String bluetoothAddress) {
        Set<BluetoothDevice> setpairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice[] pairedDevices = setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);

        boolean foundDevice = false;
        for (BluetoothDevice pairedDevice : pairedDevices) {
            if (pairedDevice.getName().contains(bluetoothAddress.trim())) {
                device = pairedDevice;
                try {
                    //the String "00001101-0000-1000-8000-00805F9B34FB" is standard for Serial connections
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                foundDevice = true;
                break;
            }
        }
        if(!foundDevice){
            Log.v(TAG, "Bluetooth Band not Paired");
        }
        try {
            mBluetoothAdapter.cancelDiscovery();
            socket.connect();
            if(socket==null)
                Log.d("BTStream", "null socket");
            else
                Log.d(TAG, socket.toString());
            return socket;
        }
        catch (IOException e) {
            Log.d("Connection Status", "Failed : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //properly closing the socket and updating the status
    public void close() {
        try {
            if(socket!=null) {
                socket.close();
                Log.d("BTStream", "close BT");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] msg,OutputStream outputStream){
        try {
            if(outputStream != null) {
                outputStream.write(msg);
                Log.d("BluetoothSend", "true");
                outputStream.flush();
            }
            else
                Log.d("BTStream", "sendStream NULL");
        } catch (Exception e) {
            if(outputStream==null)
                Log.d("BTStream", "sendStream NULL");
            e.printStackTrace();
        }
    }


    public String receiveData(BluetoothSocket socket) {
        String receivedData = "";
            try {
                receiveReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if(socket.getInputStream().available() > 0) {
                    receivedData = receiveReader.readLine();
                    if (receivedData != null && !receivedData.equalsIgnoreCase("")) {
                        Log.v(TAG, receivedData);
                        receivedData = "";

                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        return receivedData;
    }

}
