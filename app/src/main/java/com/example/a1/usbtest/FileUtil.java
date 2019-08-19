package com.example.a1.usbtest;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.chrisplus.rootmanager.RootManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fujiayi on 2017/5/19.
 */

public class FileUtil {

    // 创建一个临时目录，用于复制临时文件，如assets目录下的离线资源文件
    public static String createTmpDir(Context context) {
        String sampleDir = "baiduTTS";
        String tmpDir = Environment.getExternalStorageDirectory().toString() + "/" + sampleDir;
        if (FileUtil.makeDir(tmpDir)!=null) {
            tmpDir = context.getExternalFilesDir(sampleDir).getAbsolutePath();
            if (FileUtil.makeDir(sampleDir)!=null) {
                throw new RuntimeException("create model resources dir failed :" + tmpDir);
            }
        }
        return tmpDir;
    }

    public static boolean fileCanRead(String filename) {
        File f = new File(filename);
        return f.canRead();
    }

    public static File makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
            return file;
        } else {
            return null;
        }
    }

    public static File makeFile(String dirPath) {
        RootManager.getInstance().runCommand("su");
        File saveFile = new File(dirPath);
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
            RootManager.getInstance().runCommand("chmod -R 666 "+ saveFile.getParentFile().getPath());
        }
        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
                return saveFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void copyFromAssets(AssetManager assets, String source, String dest, boolean isCover)
            throws IOException {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                String path = dest;
                fos = new FileOutputStream(path);
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
    }
}
