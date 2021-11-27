package com.aimerneige.rfid_android;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements ServiceConnection, SerialListener {

    private final String LOG_TAG = "MainActivity";
    private final String thServerUrl = "http://101.34.24.60:5000/getTH";
    private String deviceAddress; // MAC address
    private SerialService serialService;
    private Connected connected = Connected.False;
    private LinearLayout bluetoothNotConnectedWarning;
    private LinearLayout btnOpenDoor;
    private LinearLayout btnTemperature;
    private LinearLayout btnHumidity;
    private LinearLayout btnChangeWifiData;
    private TextView textTemperature;
    private TextView textHumidity;
    private OkHttpClient client;
    private Request thRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initServices();
        initView();
        updateView();
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

        // okhttp client
        client = new OkHttpClient();

        // okhttp request
        thRequest = new Request.Builder()
                .url(thServerUrl)
                .build();
    }

    private void initView() {
        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        bluetoothNotConnectedWarning = findViewById(R.id.main_not_connected);
        bluetoothNotConnectedWarning.setOnClickListener(v -> retryConnect());

        btnOpenDoor = findViewById(R.id.main_btn_open_door);
        btnOpenDoor.setOnClickListener(v -> openDoor());

        textTemperature = findViewById(R.id.text_temperature);
        btnTemperature = findViewById(R.id.main_btn_temperature);
        btnTemperature.setOnClickListener(v -> updateTemperatureAndHumidity());

        textHumidity = findViewById(R.id.text_humidity);
        btnHumidity = findViewById(R.id.main_btn_humidity);
        btnHumidity.setOnClickListener(v -> updateTemperatureAndHumidity());

        btnChangeWifiData = findViewById(R.id.main_btn_change_wifi);
        btnChangeWifiData.setOnClickListener(v -> changeWifiData());
    }

    private void updateView() {
        updateTemperatureAndHumidity();
    }

    private void retryConnect() {
        Toast.makeText(getApplicationContext(), "正在尝试重新连接", Toast.LENGTH_SHORT).show();
        connect();
    }

    private void openDoor() {
        if (send("FBTjRVZI", false)) {
            new AlertDialog.Builder(this)
                    .setTitle("指令发送成功")
                    .setMessage("已成功发送开门指令")
                    .setCancelable(false)
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    private void updateTemperatureAndHumidity() {
        new Thread(() -> {
            try {
                Response response = client.newCall(thRequest).execute();
                String responseData = response.body().string();
                Gson gson = new Gson();
                THJsonData thJsonData = gson.fromJson(responseData, THJsonData.class);
                if (thJsonData.data.size() > 0) {
                    double t = thJsonData.data.get(0).T;
                    double h = thJsonData.data.get(0).H;
                    updateTHView(t, h);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTHView(double t, double h) {
        runOnUiThread(() -> {
            textTemperature.setText(t + "℃");
            textHumidity.setText(h + "%");
        });
    }

    private void changeWifiData() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_change_wifi_info, null);
        final TextInputEditText etSsid = alertLayout.findViewById(R.id.tiet_ssid);
        final TextInputEditText etPass = alertLayout.findViewById(R.id.tiet_password);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("设置 WiFi 信息");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        alert.setPositiveButton("确定", (dialog, which) -> {
            String ssid = etSsid.getText().toString();
            String pass = etPass.getText().toString();
            String data = String.format("WIFI:{\"ssid\":\"%s\",\"psw\":\"%s\"}", ssid, pass);
            send(data, false);
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }


    // send data to bluetooth serial port
    private boolean send(String data, boolean isHexString) {
        if (connected != Connected.True) {
            new AlertDialog.Builder(this)
                    .setTitle("蓝牙未连接")
                    .setMessage("无法建立串口通信，请检查蓝牙设置")
                    .setCancelable(false)
                    .setPositiveButton("确定", null)
                    .show();
            return false;
        }
        byte[] sendData;
        if (isHexString) {
            sendData = TextUtil.hexStringToByteArray(data);
        } else {
            sendData = (data + "\r\n").getBytes();
        }
        try {
            serialService.write(sendData);
            return true;
        } catch (Exception e) {
            onSerialIoError(e);
            return false;
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
            deviceAddress = "84:CC:A8:2C:2B:5E"; // TODO
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

    // TODO access sp and get saved device mac address
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