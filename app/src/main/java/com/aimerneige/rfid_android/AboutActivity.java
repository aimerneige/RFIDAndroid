package com.aimerneige.rfid_android;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutActivity extends AppCompatActivity {

    private static final String openSourceUrl = "https://github.com/aimerneige/RFIDAndroid";
    private static final String githubUrl = "https://github.com/aimerneige";
    private static final String blogUrl = "https://aimerneige.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        RelativeLayout aboutApp = findViewById(R.id.about_about_app);
        aboutApp.setOnClickListener(v -> onAboutAppClicked());

        RelativeLayout openSource = findViewById(R.id.about_open_source);
        openSource.setOnClickListener(v -> onOpenSourceClicked());

        RelativeLayout github = findViewById(R.id.about_github);
        github.setOnClickListener(v -> onGitHubClicked());

        RelativeLayout blog = findViewById(R.id.about_blog);
        blog.setOnClickListener(v -> onBlogClicked());
    }

    private void onAboutAppClicked() {
        new AlertDialog.Builder(this)
                .setTitle("关于本软件")
                .setMessage("本软件是 RFID 课程设计智能门锁系统安卓端程序，" +
                        "可以通过蓝牙串口通信离线开门以及修改 WiFi 信息，" +
                        "同时可以通过网络获取服务器保存的温湿度传感器数据。")
                .setCancelable(true)
                .setPositiveButton("确认", null)
                .show();
    }

    private void onOpenSourceClicked() {
        startUrlActivity(openSourceUrl);
    }

    private void onGitHubClicked() {
        startUrlActivity(githubUrl);
    }

    private void onBlogClicked() {
        startUrlActivity(blogUrl);
    }

    private void startUrlActivity(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}