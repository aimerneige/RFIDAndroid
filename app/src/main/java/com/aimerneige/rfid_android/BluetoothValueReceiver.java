package com.aimerneige.rfid_android;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothValueReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "BluetoothValueReceiver";
    public static int DEFAULT_VALUE_BLUETOOTH = 1000;

    private OnStateChangeListener onStateChangeListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, DEFAULT_VALUE_BLUETOOTH);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    onStateChangeListener.onBluetoothOff();
                    Log.d(LOG_TAG, "Bluetooth OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    onStateChangeListener.onBluetoothOn();
                    Log.d(LOG_TAG, "Bluetooth ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    onStateChangeListener.onBluetoothTurningOn();
                    Log.d(LOG_TAG, "Bluetooth Opening");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    onStateChangeListener.onBluetoothTurningOff();
                    Log.d(LOG_TAG, "Bluetooth Closing");
                    break;
                default:
                    Log.w(LOG_TAG, "Bluetooth State Unknown");
            }
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }
}
