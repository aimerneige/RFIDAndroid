package com.aimerneige.rfid_android;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {

    private static final String DEFAULT_MAC = "84:CC:A8:2C:2B:5E";
    private static final String DEFAULT_PSW = "FBTjRVZI";

    private static final String DEVICE_CONNECTED = "device_connected";
    private static final String DEVICE_CONNECTED_KEY = "last_connected";

    private static final String DEVICE_PASSWORD = "device_password";
    private static final String DEVICE_PASSWORD_KEY = "saved_password";


    /**
     * 保存成功连接的设备
     *
     * @param context    上下文
     * @param macAddress 设备 MAC 地址
     */
    public static void saveConnectedDevice(Context context, String macAddress) {
        SharedPreferences sp = context.getSharedPreferences(DEVICE_CONNECTED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(DEVICE_CONNECTED_KEY, macAddress);
        editor.apply();
    }

    /**
     * 读取上次成功连接的设备
     *
     * @param context 上下文
     * @return 设备 MAC 地址
     */
    public static String getConnectedDevice(Context context) {
        SharedPreferences sp = context.getSharedPreferences(DEVICE_CONNECTED, Context.MODE_PRIVATE);
        return sp.getString(DEVICE_CONNECTED_KEY, DEFAULT_MAC);
    }


    /**
     * 将开门密码保存在本地
     *
     * @param context  上下文
     * @param password 开门密码
     */
    public static void savePassword(Context context, String password) {
        SharedPreferences sp = context.getSharedPreferences(DEVICE_PASSWORD, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(DEVICE_PASSWORD_KEY, password);
        editor.apply();
    }

    /**
     * 获取保存在本地的密码
     *
     * @param context 上下文
     * @return 开门密码
     */
    public static String getSavedPassword(Context context) {
        SharedPreferences sp = context.getSharedPreferences(DEVICE_PASSWORD, Context.MODE_PRIVATE);
        return sp.getString(DEVICE_PASSWORD_KEY, DEFAULT_PSW);
    }

}
