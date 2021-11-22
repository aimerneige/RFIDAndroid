package com.aimerneige.rfid_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends BaseActivity implements ServiceConnection, SerialListener {

    private enum Connected {
        False,
        Pending,
        True
    }

    private final String LOG_TAG = "MainActivity";

    private String deviceAddress; // MAC address
    private SerialService serialService;

    private Connected connected = Connected.False;

//    private MaterialAlertDialogBuilder noBluetoothSupportDialog;
//    private MaterialAlertDialogBuilder bluetoothOffDialog;
//    private MaterialAlertDialogBuilder noBluetoothExitDialog;

    ActivityResultLauncher<Intent> openBluetoothActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_CANCELED) {
                    requestOpenBluetooth();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDialogs();
        initServices();
        initView();
        applyClickAction();
        requestOpenBluetooth();
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

    private void initDialogs() {
//        noBluetoothSupportDialog = new MaterialAlertDialogBuilder(mContext)
//                .setCancelable(false)
//                .setTitle(R.string.no_bluetooth_support_dialog_title)
//                .setMessage(R.string.no_bluetooth_support_dialog_message)
//                .setPositiveButton(R.string.no_bluetooth_support_dialog_ok, (dialog, which) -> finish());
//        bluetoothOffDialog = new MaterialAlertDialogBuilder(mContext)
//                .setCancelable(false)
//                .setTitle(R.string.bluetooth_off_dialog_title)
//                .setMessage(R.string.bluetooth_off_dialog_message)
//                .setPositiveButton(R.string.bluetooth_off_dialog_ok, (dialog, which) -> {
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    openBluetoothActivityResult.launch(enableBtIntent);
//                })
//                .setNegativeButton(R.string.bluetooth_off_dialog_cancel, (dialog, which) -> noBluetoothExitDialog.show());
//        noBluetoothExitDialog = new MaterialAlertDialogBuilder(mContext)
//                .setCancelable(false)
//                .setTitle(R.string.no_bluetooth_exit_dialog_title)
//                .setMessage(R.string.no_bluetooth_exit_dialog_message)
//                .setPositiveButton(R.string.no_bluetooth_exit_dialog_ok, (dialog, which) -> requestOpenBluetooth())
//                .setNegativeButton(R.string.no_bluetooth_exit_dialog_cancel, (dialog, which) -> finish());
    }

    private void initServices() {
        // register bluetooth receiver
        BluetoothValueReceiver bluetoothValueReceiver = new BluetoothValueReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothValueReceiver, filter);
        bluetoothValueReceiver.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onBluetoothOff() {
                requestOpenBluetooth();
            }

            @Override
            public void onBluetoothOn() {
            }

            @Override
            public void onBluetoothTurningOn() {
            }

            @Override
            public void onBluetoothTurningOff() {
            }
        });
        serialService = new SerialService();
        connect();
    }

    private void initView() {
        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    private void applyClickAction() {

    }

    // send data to bluetooth serial port
    private void send(String data) {
        if (connected != Connected.True) {
            // NOT CONNECTED
            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            serialService.write(TextUtil.toByteArray(data));
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
            deviceAddress = "00:02:0A:01:A5:47";
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

    // request to open bluetooth
    private void requestOpenBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
//            noBluetoothSupportDialog.show();
        } else if (!bluetoothAdapter.isEnabled()) {
            // User didn't open Bluetooth
//            bluetoothOffDialog.show();
        }
    }
}