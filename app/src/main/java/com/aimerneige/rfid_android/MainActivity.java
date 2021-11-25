package com.aimerneige.rfid_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends BaseActivity implements ServiceConnection, SerialListener {

    private final String LOG_TAG = "MainActivity";
    private String deviceAddress; // MAC address
    private SerialService serialService;
    private Connected connected = Connected.False;
    private LinearLayout btnOpenDoor;
    private LinearLayout bluetoothNotConnectedWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initServices();
        initView();
    }

    @Override
    protected void onDestroy() {
        if (connected != Connected.False) {
            disconnect();
        }
        stopService(new Intent(MainActivity.this, SerialService.class));
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (serialService != null) {
            serialService.attach(this);
        } else {
            // prevents service destroy on unbind from recreated activity caused by orientation change
            startService(new Intent(MainActivity.this, SerialService.class));
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        serialService = ((SerialService.SerialBinder) binder).getService();
        serialService.attach(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serialService = null;
    }

    @Override
    public void onSerialConnect() {
        String msg = "Serial Connected.";
        Log.d(LOG_TAG, msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        connected = Connected.True;
        bluetoothNotConnectedWarning.setVisibility(View.GONE);
    }

    @Override
    public void onSerialConnectError(Exception e) {
        String msg = "Serial Connect Error.\nError Message:\n" + e.getMessage();
        Log.e(LOG_TAG, msg);
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        String msg = "Serial IO Error.\nError Message:\n" + e.getMessage();
        Log.e(LOG_TAG, msg);
        disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings: {
                Toast.makeText(getApplicationContext(), "hhh", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.menu_item_about: {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initServices() {
        serialService = new SerialService();
        connect();
    }

    private void initView() {
        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        bluetoothNotConnectedWarning = findViewById(R.id.main_not_connected);

        btnOpenDoor = findViewById(R.id.main_btn_open_door);
        btnOpenDoor.setOnClickListener(v -> openDoor());
    }

    private void openDoor() {
        send("FBTjRVZI", false);
    }

    // send data to bluetooth serial port
    private void send(String data, boolean isHexString) {
        if (connected != Connected.True) {
            // NOT CONNECTED
            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] sendData;
        if (isHexString) {
            sendData = TextUtil.toByteArray(data);
        } else {
            sendData = (data + "\r\n").getBytes();
        }
        try {
            serialService.write(sendData);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    // receive data from serial
    private void receive(byte[] data) {
        Log.d(LOG_TAG, TextUtil.toHexString(data));
    }

    // connect to bluetooth serial
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            deviceAddress = "84:CC:A8:2C:2B:5E";
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getApplicationContext(), device);
            serialService.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    // disconnect to bluetooth serial
    private void disconnect() {
        connected = Connected.False;
        serialService.disconnect();
    }

    // access sp and get saved device mac address
    private String getConnectedDeviceAddress() {
        // todo from sp
        return "84:CC:A8:2C:2B:5E";
    }

    private enum Connected {
        False,
        Pending,
        True
    }
}