package com.aimerneige.rfid_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private RelativeLayout mac;
    private RelativeLayout psw;
    private TextView textMac;
    private TextView textPsw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar();
        initView();
        applyClickAction();
        updateView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initView() {
        mac = findViewById(R.id.settings_mac);
        textMac = findViewById(R.id.text_settings_mac);
        psw = findViewById(R.id.settings_psw);
        textPsw = findViewById(R.id.text_settings_psw);
    }

    private void applyClickAction() {
        mac.setOnClickListener(v -> onMacClicked());
        psw.setOnClickListener(v -> onPswClicked());
    }

    private void updateView() {
        textMac.setText(SPUtils.getConnectedDevice(getApplicationContext()));
        textPsw.setText(SPUtils.getSavedPassword(getApplicationContext()));
    }

    private void onMacClicked() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_change_mac, null);
        final TextInputEditText etMac = alertLayout.findViewById(R.id.tiet_mac);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("请设置门锁 MAC");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        alert.setPositiveButton("确定", (dialog, which) -> {
            String macAddress;
            if (etMac.getText() == null || etMac.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "请输入 MAC 地址", Toast.LENGTH_SHORT).show();
                return;
            } else {
                macAddress = etMac.getText().toString();
            }
            SPUtils.saveConnectedDevice(getApplicationContext(), macAddress);
            textMac.setText(macAddress);
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void onPswClicked() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_set_password, null);
        final TextInputEditText etPassword = alertLayout.findViewById(R.id.tiet_password);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("设置开门密码");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        alert.setPositiveButton("确定", (dialog, which) -> {
            String password;
            if (etPassword.getText() == null || etPassword.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            } else {
                password = etPassword.getText().toString();
            }
            SPUtils.savePassword(getApplicationContext(), password);
            textPsw.setText(password);
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }
}
