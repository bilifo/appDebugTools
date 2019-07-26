package com.example.a1.usbtest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chrisplus.rootmanager.RootManager;
import com.chrisplus.rootmanager.container.Result;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    TextView tv;
    EditText ed;
    Button bt, bt2, bt3, bt4;
    UsbManager usbManager;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FormatStrategy formatStrategy =
                CsvFormatStrategy.newBuilder()
                        .tag(null)/** */
                        .build();
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));

        setContentView(R.layout.activity_main);
        quanxian6();
        ed = findViewById(R.id.ed);
        tv = findViewById(R.id.textView);
        bt = findViewById(R.id.button);
        bt2 = findViewById(R.id.button2);
        bt3 = findViewById(R.id.button3);
        bt4 = findViewById(R.id.button4);
        Logger.log(Logger.DEBUG, "pjl++", "test", null);
        //给密集架app设置激活码
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = ed.getText().toString().trim();
                if (!TextUtils.isEmpty(temp)) {
                    RootManager.getInstance().runCommand("su");
                    try {
                        String temp1 = EncryptHelper.desEncrypt("kn." + temp);
                        String shpath = "/data/data/com.dense.kuiniu.dense_frame/files/EncryptionCode";
                        Result aa = RootManager.getInstance().runCommand("echo " + "-e " + "\"" + temp1 + "\\c" + "\"" + " > " + shpath);//echo -e表示激活转移字符, 末尾加上\c表示不换行
//                        RootManager.getInstance().runCommand("chmod -R 666 " + shpath);
                        tv.setText("更新完成 " + temp1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        tv.setText("生成激活码出错");
                    }

                } else {
                    tv.setText("未输入年.月.日");
                }

            }
        });

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //设置调试端口5555
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接命令行
                StringBuffer command = new StringBuffer();
                Result runCommand = RootManager.getInstance().runCommand("setprop service.adb.tcp.port 5555");
                command.append(runCommand.getMessage() + "\n");
                Result runCommand2 = RootManager.getInstance().runCommand("stop adbd");
                command.append(runCommand2.getMessage() + "\n");
                Result runCommand3 = RootManager.getInstance().runCommand("start adbd");
                command.append(runCommand3.getMessage() + "\n");
                tv.setText(command.toString());
//                //使用assets下的sh脚本,用.运行
//                String shpath = "/data/data/" + getPackageName() + "/test.sh";
//                try {
//                    copyFromAssets(getAssets(), "test.sh", shpath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Result a = RootManager.getInstance().runCommand("chmod -R 666 " + "/data/data/" + getPackageName());//易失误点:复制完的文件,不进行chmod 666
//                Result c = RootManager.getInstance().runCommand("." + shpath);
//                tv.setText(c.getMessage());
//                //使用assets下的sh脚本,用sh运行
//                //                Result b = RootManager.getInstance().runCommand("chmod 666 /data/data/");
//                String shpath = "/data/data/" + getPackageName() + "/test.sh";
//                String shpath2 = "/data/data/" + getPackageName() + "/test";
//                Logger.wtf("##################");
//                try {
//                    copyFromAssets(getAssets(), "test.sh", shpath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Result a = RootManager.getInstance().runCommand("chmod 666 " + shpath);//易失误点:复制完的文件,不进行chmod 666
//                Result c = RootManager.getInstance().runCommand("sh " + shpath);
//                tv.setText(c.getMessage());


            }
        });
    }

    private void usbPermission(UsbDevice usbDevice) {
        if (!usbManager.hasPermission(usbDevice)) {
            Log.d("pjl++", "usbPermission:usbDevice:  " + usbDevice.getDeviceName());
            PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, (int) (Math.random() * 100), new Intent(ACTION_DEVICE_PERMISSION), 0);
            usbManager.requestPermission(usbDevice, pi);
        } else {
            Log.d("pjl++", "" + usbDevice.getDeviceName() + " is Permission");
        }
        int i = usbDevice.getInterfaceCount();
        for (int j = 0; j < i; j++) {
            UsbInterface in = usbDevice.getInterface(j);
//            Log.d("pjl++", "UsbInterface:  " + in.getName());
            int i2 = in.getEndpointCount();
            for (int j2 = 0; j2 < i2; j2++) {
                UsbEndpoint ue = in.getEndpoint(j2);
                Log.d("pjl++", "UsbEndpoint:  " + ue);
            }
        }

    }

    private String ACTION_DEVICE_PERMISSION = "1";
    private BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_DEVICE_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //授权成功,在这里进行打开设备操作
                        //获得了usb使用权限
                        Log.d("pjl++", "onReceive: 获得了usb使用权限,授权成功");
                    } else {
                        //授权失败
                    }
                }
            }
        }

    };

    public static void copyFromAssets(AssetManager assets, String source, String dest)
            throws IOException {
        File file = new File(dest);
        if (file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = assets.open(source);
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = is.read(buffer, 0, 1024)) >= 0) {
                fos.write(buffer, 0, size);
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        }
    }


    /**
     * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取
     */
    public void quanxian6() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }


    public static int ByteIndexOf(byte[] searched, byte[] find, int start) {
        boolean matched = false;
        int end = find.length - 1;
        int skip = 0;
        for (int index = start; index <= searched.length - find.length; ++index) {
            matched = true;
            if (find[0] != searched[index] || find[end] != searched[index + end]) continue;
            else skip++;
            if (end > 10)
                if (find[skip] != searched[index + skip] || find[end - skip] != searched[index + end - skip])
                    continue;
                else skip++;
            for (int subIndex = skip; subIndex < find.length - skip; ++subIndex) {
                if (find[subIndex] != searched[index + subIndex]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return index;
            }
        }
        return -1;
    }
}
