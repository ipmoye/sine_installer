package moye.installer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import cn.bavelee.pokeinstaller.apk.APKCommander;
import cn.bavelee.pokeinstaller.apk.ApkInfo;
import cn.bavelee.pokeinstaller.apk.ICommanderCallback;
import moye.sinetoolbox.xtc.AppTools;

public class InstallerActivity extends Activity implements ICommanderCallback {

    ImageView app_icon_view;
    TextView app_name_view;
    TextView app_packagename_view;
    TextView app_version_view;
    TextView install_log_view;
    Button install_btn_view;
    Button cancel_btn_view;

    APKCommander apkCommander;

    boolean is_installing = false;
    boolean is_install_finish = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        //用来锁定DPI为320
        Configuration origConfig = newBase.getResources().getConfiguration();
        origConfig.densityDpi = 320;
        Context confBase = newBase.createConfigurationContext(origConfig);
        super.attachBaseContext(confBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);

        //赋值页面元素
        app_icon_view = findViewById(R.id.app_icon);
        app_name_view = findViewById(R.id.app_name);
        app_packagename_view = findViewById(R.id.app_packagename);
        app_version_view = findViewById(R.id.app_version);
        install_log_view = findViewById(R.id.install_log);
        install_btn_view = findViewById(R.id.install_btn);

        cancel_btn_view = findViewById(R.id.cancel_btn);
        cancel_btn_view.setOnClickListener(view -> finish());
        install_btn_view.setOnClickListener(view -> {
            if(is_install_finish){
                Intent intent = getPackageManager().getLaunchIntentForPackage(apkCommander.getApkInfo().getPackageName());
                startActivity(intent);
                finish();
            }else apkCommander.startInstall();
        });

        if(getIntent().getData() == null) finish();

        apkCommander = new APKCommander(InstallerActivity.this,getIntent().getData(),InstallerActivity.this);

        //用于安装时的按钮文字动画
        new Thread(() -> {
            int index = 0;
            if (is_installing){
                if(index >= 3) index = 0;
                String s = "安装中";
                for (int i = 0;i < index;i++) s += ".";
                String finalS = s;
                InstallerActivity.this.runOnUiThread(() -> install_btn_view.setText(finalS));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        String selinux_status = AppTools.root_exec("getenforce");
        if (Objects.equals(selinux_status,"Permissive\n")){
        }else if(Objects.equals(selinux_status,"Disabled\n")){
        }else if(Objects.equals(selinux_status,"Enforcing\n")){
            Intent intent = new Intent(this,ErrorDialog.class);
            intent.putExtra("title","SELinux");
            intent.putExtra("content","请先将SELinux设置为宽容模式再使用“弦 - 安装器”");
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this,ErrorDialog.class);
            intent.putExtra("title","需要ROOT");
            intent.putExtra("content","您需要ROOT之后才能使用“弦 - 安装器”");
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onStartParseApk(Uri uri) {
        install_btn_view.setEnabled(false);
        app_name_view.setText("解析中...");
    }

    @Override
    public void onApkParsed(ApkInfo apkInfo) {
        //解析安装包
        if(apkInfo == null || TextUtils.isEmpty(apkInfo.getPackageName())) {
            app_name_view.setText("安装包解析失败");
            install_btn_view.setEnabled(false);
            install_btn_view.setBackground(getResources().getDrawable(R.drawable.button_error));
            install_btn_view.setText("无法安装");
        }
        else{
            install_btn_view.setEnabled(true);
            app_icon_view.setImageDrawable(apkInfo.getIcon());
            app_name_view.setText(apkInfo.getAppName());
            app_packagename_view.setText(apkInfo.getPackageName());
            if (apkInfo.isHasInstalledApp()) {
                app_version_view.setText(apkInfo.getVersion() + "  已安装:" + apkInfo.getInstalledVersion());
                if(apkInfo.getVersionCode() < apkInfo.getInstalledVersionCode()){
                    install_log_view.setVisibility(View.VISIBLE);
                    install_log_view.setText("即将降级安装\n");
                }
            }
            else app_version_view.setText(apkInfo.getVersion());
        }
    }

    @Override
    public void onApkPreInstall(ApkInfo apkInfo) {
        //开始安装
        cancel_btn_view.setEnabled(false);
        install_btn_view.setEnabled(false);
        install_btn_view.setText("安装中");
        install_log_view.setVisibility(View.VISIBLE);
        is_installing = true;
    }

    @Override
    public void onApkInstalled(ApkInfo apkInfo, int resultCode) {
        //安装完成
        is_installing = false;
        cancel_btn_view.setEnabled(true);
        if(resultCode == 0){
            install_btn_view.setText("打开");
            install_btn_view.setEnabled(true);
            Toast.makeText(this,"安装完成",Toast.LENGTH_SHORT).show();
            is_install_finish = true;
        }else{
            install_btn_view.setText("安装失败");
            install_btn_view.setBackground(getResources().getDrawable(R.drawable.button_error));
            install_log_view.setBackgroundColor(getResources().getColor(R.color.list_error_bg));
            install_log_view.setTextColor(getResources().getColor(R.color.font_white));
            Toast.makeText(this,"安装失败",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInstallLog(ApkInfo apkInfo, String logText) {
        install_log_view.setText(install_log_view.getText() + logText);
    }
}