package com.example.sven.bsdiff;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static final int REQUEST_CODE = 0;
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView version = findViewById(R.id.version);
        version.setText(BuildConfig.VERSION_NAME);

        boolean isAllGranted = checkPermissionAllGranted(perms);
        if (!isAllGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    perms,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (!hasAllPermissionsGranted(grantResults)) {
                showMissingPermissionDialog();
            }
        }
    }

    // 显示缺失权限提示(普通权限)
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.string_help_text);

        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });

        builder.setCancelable(false);
        builder.create().show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivityForResult(intent,0);
    }


    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    public void update(View view) {
        //合成APK
        new AsyncTask<Void,Void,File>(){

            @Override
            protected File doInBackground(Void... voids) {

                String old = getApplication().getApplicationInfo().sourceDir;
                bspatch(old,"/sdcard/patch","/sdcard/new.apk");
                return new File("/sdcard/new.apk");
            }

            @Override
            protected void onPostExecute(File file) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }else{
                    // // 声明需要的临时权限
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            //第二个参数，
                    Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.example.sven.bsdiff.fileprovider",file);
                    intent.setDataAndType(contentUri,"application/vnd.android.package-archive");
                }
                startActivity(intent);
            }
        }.execute();
    }

    /**
     *
     * @param oldapk 当前apk
     * @param patch  差分包
     * @param output  合成后的新的apk输出到
     */
    native void bspatch(String oldapk,String patch,String output);

}
