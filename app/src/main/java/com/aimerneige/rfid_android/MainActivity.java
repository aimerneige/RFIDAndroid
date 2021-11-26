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
import androidx.biometric.BiometricPrompt;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.Executor;

public class MainActivity extends BaseActivity implements ServiceConnection, SerialListener {

    private final String LOG_TAG = "MainActivity";
    private String deviceAddress; // MAC address
    private SerialService serialService;
    private Connected connected = Connected.False;
    private LinearLayout btnOpenDoor;
    private LinearLayout bluetoothNotConnectedWarning;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


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
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
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
        // bluetooth serial
        serialService = new SerialService();
        connect();

        // fingerprint
//        executor = ContextCompat.getMainExecutor(this);
//        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
//
//            @Override
//            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//                Toast.makeText(getApplicationContext(),
//                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
//                        .show();
//            }
//
//            @Override
//            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//                Toast.makeText(getApplicationContext(),
//                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
//                openDoor();
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//                Toast.makeText(getApplicationContext(), "Authentication failed",
//                        Toast.LENGTH_SHORT)
//                        .show();
//            }
//        });
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("指纹认证")
//                .setSubtitle("使用指纹认证确认开门")
//                .setNegativeButtonText("使用密码")
//                .build();
    }

    private void initView() {
        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        bluetoothNotConnectedWarning = findViewById(R.id.main_not_connected);

        btnOpenDoor = findViewById(R.id.main_btn_open_door);
        btnOpenDoor.setOnClickListener(v -> openDoor());
//        btnOpenDoor.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
    }

    private void openDoor() {
        send("FBTjRVZI", false);
    }

    // send data to bluetooth serial port
    private void send(String data, boolean isHexString) {
        if (connected != Connected.True) {
            // NOT CONNECTED
            Toast.makeText(getApplicationContext(), "蓝牙未连接", Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] sendData;
        if (isHexString) {
            sendData = TextUtil.hexStringToByteArray(data);
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
        Log.d(LOG_TAG, TextUtil.byteArrayToHexString(data));
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