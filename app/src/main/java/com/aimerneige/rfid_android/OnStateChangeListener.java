package com.aimerneige.rfid_android;

public interface OnStateChangeListener {
    void onBluetoothOff();

    void onBluetoothOn();

    void onBluetoothTurningOn();

    void onBluetoothTurningOff();
}
