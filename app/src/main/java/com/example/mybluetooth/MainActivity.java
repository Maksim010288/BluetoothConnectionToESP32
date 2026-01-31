package com.example.mybluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String tempDate = null;
    private String humDate = null;
    private TextView temperature, humidity;
    private Button button;
    private MenuItem soundOn, soundOff;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectedThread connectedThread;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        button = findViewById(R.id.button_on);

        getBtManager();
        connectedThread = new ConnectedThread("ESP32test");
        connectedThread.start();
        updateTextView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        soundOn = menu.findItem(R.id.on_sound);
        soundOff = menu.findItem(R.id.off_sound);
        return true;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.on_sound) {
            sendData("on");
        }
        if (item.getItemId() == R.id.off_sound) {
            sendData("off");
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void sendData(String message) {
        connectedThread.sendData(message);
    }

    private void getBtManager() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }
    }

    private void updateTextView() {
        new CountDownTimer(3000, 5000) {

            @Override
            public void onFinish() {
                tempDate = "t: " + connectedThread.getTemperature() + "°" + " C";
                humDate = "v: " + connectedThread.getHumidity() + " %";
                temperature.setText(tempDate);
                humidity.setText(humDate);
                start();
            }

            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }
}