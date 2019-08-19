package com.example.a1.usbtest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisplus.rootmanager.RootManager;
import com.chrisplus.rootmanager.container.Result;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.function.ToDoubleFunction;

public class MainActivity extends AppCompatActivity{
    TextView tv;
    EditText ed;
    Button bt, bt2, bt3, bt4;
    UsbManager usbManager;
    Result aa;
    Switch aSwitch;
    String usbPath;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int value = msg.what;
            if (value == 1) {
                handler.removeMessages(1);
                catchLog();
                handler.sendEmptyMessageDelayed(1, 1000 * 30);
            }
        }
    };
    private static final String ACTION_USB_PERMISSION = "com.demo.otgusb.USB_PERMISSION";
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;
            switch (action) {
                case ACTION_USB_PERMISSION://用户授权广播
                    synchronized (this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) { //允许权限申请
                        } else {
                        }
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://USB设备插入广播
                    // 获取挂载路径, 读取U盘文件
                    usbPath = intent.getDataString();

                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://USB设备拔出广播
                    Toast.makeText(MainActivity.this, "U盘拔出", Toast.LENGTH_SHORT).show();
                    usbPath = null;
                    break;
            }
        }
    };

    private void initUSB() {
        //USB管理器
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //注册广播,监听USB插入和拔出
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, intentFilter);

        //读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 111);
        }
    }

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FormatStrategy formatStrategy =
                CsvFormatStrategy.newBuilder()
                        .tag(null)/** */
                        .build();
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
        initUSB();
        setContentView(R.layout.activity_main);
        quanxian6();
        ed = findViewById(R.id.ed);
        tv = findViewById(R.id.textView);
        bt = findViewById(R.id.button);
        bt2 = findViewById(R.id.button2);
        bt3 = findViewById(R.id.button3);
        bt4 = findViewById(R.id.button4);
        aSwitch = findViewById(R.id.switch1);
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
                        RootManager.getInstance().runCommand("chmod -R 666 " + shpath);
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

        //抓取日志
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RootManager.getInstance().runCommand("su");
                catchLog2();
            }
        });
        //导出日志到upan
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RootManager.getInstance().runCommand("su");
                if (usbPath == null) {
                    Toast.makeText(MainActivity.this, "没有U盘插入", Toast.LENGTH_SHORT).show();
                } else {

                }
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

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    RootManager.getInstance().runCommand("rm -R "+saveLogPath);
//                    handler.removeMessages(1);
//                    handler.sendEmptyMessage(1);
//                } else {
//                    handler.removeMessages(1);
//                }
                if (isChecked) {
                    RootManager.getInstance().runCommand("rm -R " + saveLogPath);
                    RootManager.getInstance().runCommand("su");


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");

                            String date = sDateFormat.format(new java.util.Date());
                            File saveFile = FileUtil.makeFile(saveLogPath + date);
                            RootManager.getInstance().runCommand("logcat -v time -f " + saveFile.getPath());
                        }
                    }).start();

                } else {
                    RootManager.getInstance().runCommand("su");
                    RootManager.getInstance().runCommand("logcat -c");
                }
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

    private final String saveLogPath = "/sdcard/log/";

    private void catchLog() {
        RootManager.getInstance().runCommand("su");
        RootManager.getInstance().runCommand("logcat -c");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");

        String date = sDateFormat.format(new java.util.Date());
        File saveFile = FileUtil.makeFile(saveLogPath + date);
        RootManager.getInstance().runCommand("logcat -d -v time -f " + saveFile.getPath());


    }

    private void catchLog2() {
        RootManager.getInstance().runCommand("su");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");

        String date = sDateFormat.format(new java.util.Date());
        File saveFile = FileUtil.makeFile(saveLogPath + date);
        RootManager.getInstance().runCommand("logcat -d -v time -f " + saveFile.getPath());
    }
}
