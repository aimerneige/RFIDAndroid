package com.aimerneige.rfid_android;

public class BluetoothConstants {
    public static final String SERIAL_PATH = "";
    public static final int BAUD_RATE = 115200;

    // values have to be globally unique
    public static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    public static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    public static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    // values have to be unique within each app
    public static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    public static final String TEST_DEVICE_ADDRESS = "84:CC:A8:2C:2B:5E";
}
