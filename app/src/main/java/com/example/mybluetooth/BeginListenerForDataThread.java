package com.example.mybluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BeginListenerForDataThread extends Thread {
    private BluetoothSocket bluetoothSocket;
     private InputStream inputStream;
    private OutputStream outputStream;
    private String receiverMessage, temperature, humidity;
    private byte[] buffer;
    private UUID MY_UUID;

    public BeginListenerForDataThread(BluetoothSocket bluetoothSocket,
                                      InputStream inputStream,
                                      UUID MY_UUID) {
        this.bluetoothSocket = bluetoothSocket;
        this.inputStream = inputStream;
        this.MY_UUID = MY_UUID;

        try {
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        buffer = new byte[1024];
        while (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                while (inputStream.available() > 0) {
                    int bytes = inputStream.read(buffer);
                    receiverMessage = new String(buffer, 0, bytes);
                    temperature = receiverMessage;
                    humidity = receiverMessage;
                }
            } catch (IOException e) {
                Log.i("ligament", "the ligament is torn");
            }
        }
    }

    public void sendData(String message) {
        try {
            outputStream.write(message.getBytes());
            Log.i("sendData", "дані Передано");
        } catch (IOException ioException) {
            Log.e("sendDAta", "передача не вдалась");
        }

    }

    public String getTemperature() throws Exception {
        return temperature.substring(0,4);
    }

    public String getHumidity() throws Exception {
        return humidity.substring(8,13);
    }
}
