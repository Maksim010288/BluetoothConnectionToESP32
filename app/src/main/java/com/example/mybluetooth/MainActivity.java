package com.example.mybluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String tempDate = null;
    private String humDate = null;
    private String selected = null;
    private TextView temperature, humidity, movement;
    private AutoCompleteTextView deviceName;
    private Spinner spinner;
    private ArrayAdapter<String> arrayAdapter;
    private Button button;
    private MenuItem soundOn, soundOff;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectedThread connectedThread;
    private List<String> deviceList = new ArrayList<>();

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

        spinner = findViewById(R.id.my_spinner);
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);
        movement = findViewById(R.id.movement);
        button = findViewById(R.id.button_on);

        findAllDeviceName();

        getDeviceLists();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onClick(View v) {
                getBtManager();
                connectedThread = new ConnectedThread(selected);
                connectedThread.start();
                updateTempHumidity();
                updateMovement();
            }
        });

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

    private void updateTempHumidity() {
        try {
            new CountDownTimer(3000, 5000) {
                @Override
                public void onFinish() {
                    try {
                        tempDate = "t: " + connectedThread.getTemperature() + "°" + " C";
                        humDate = "v: " + connectedThread.getHumidity() + " %";
                        temperature.setText(tempDate);
                        humidity.setText(humDate);
                        start();
                    } catch (Exception e) {
                        toastMessage("Немає підключення до обладнання");
                    }
                }

                @Override
                public void onTick(long millisUntilFinished) {
                }
            }.start();
        } catch (Exception e) {
            Log.e("not connection", e.toString());
        }
    }

    private void updateMovement() {
        new CountDownTimer(2000, 1000) {
            @Override
            public void onFinish() {
                try {
                    movement.setText(connectedThread.getMovement());
                    if (connectedThread.getMovement().equals("РУХ")) {
                        movement.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                        button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                    } else {
                        movement.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                        button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue));
                    }
                    start();
                } catch (Exception e) {
                    Log.e("movement", e.toString());
                }
            }

            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void getDeviceLists() {
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, findAllDeviceName());
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter1);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private List<String> findAllDeviceName() {
        List<String> list = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevice) {
            list.add(device.getName());
        }
        Log.e("list", list.toString());
        return list;
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}