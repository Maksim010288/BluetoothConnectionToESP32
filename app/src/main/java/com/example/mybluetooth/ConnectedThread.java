package com.example.mybluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class ConnectedThread extends Thread {

    private String deviceName;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private BeginListenerForDataThread beginListenerForDataThread;
    private InputStream inputStream;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public ConnectedThread(String deviceName) {
        this.deviceName = deviceName;
        if (getBtMacAddress(deviceName) != null) {
            bluetoothDevice = getBtMacAddress(deviceName);
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public void run() {
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            if (bluetoothSocket.isConnected()) {
                Log.i("connect", "bt is connected - " + bluetoothSocket.toString());
                Log.e("macAddress", getBtMacAddress(deviceName).getAddress());
            }
            inputStream = bluetoothSocket.getInputStream();
            beginListenerForDataThread =
                    new BeginListenerForDataThread(bluetoothSocket, inputStream, MY_UUID);
            beginListenerForDataThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public BluetoothDevice getBtMacAddress(String deviceName) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;

        for (BluetoothDevice device : pairedDevice) {
            if (device.getName().equals(deviceName)) {
                targetDevice = device;
            }
        }
        return targetDevice;
    }

    public void sendData(String message) {
       beginListenerForDataThread.sendData(message);
    }

    public String getTemperature() {
        return beginListenerForDataThread.getTemperature();
    }

    public String getHumidity() {
        return beginListenerForDataThread.getHumidity();
    }
}
