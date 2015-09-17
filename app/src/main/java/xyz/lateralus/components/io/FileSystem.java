package xyz.lateralus.components.io;


import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class FileSystem {
    private static final String TAG = "FileSys";

    public static final String BASE_DIR = Environment.getExternalStorageDirectory().toString() + "/";
    public static final String TMP_DIR  = Environment.getExternalStorageDirectory().toString() + "/tmp/";

    public static Boolean saveImage(Bitmap img, Bitmap.CompressFormat format, String saveDir, String fileName){
        try {
            File dir = new File(saveDir);
            dir.mkdirs();
            File file = new File(saveDir, fileName);
            FileOutputStream fout = new FileOutputStream(file);
            img.compress(format, 100, fout);
            fout.flush();
            fout.close();
            return true;
        }
        catch(Exception e) {
            Log.e(TAG, "Save image Failed!");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean doesFileExist(String path){
        try {
            return new File(path).exists();
        }
        catch(Exception e){
            Log.e(TAG, "Error checking if a file exists");
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] getByteArrayOfFile(String path){
        try {
            File file = new File(path);
            if(file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int readNum;
                while((readNum = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, readNum);
                }
                bos.close();
                return bos.toByteArray();
            }
        }
        catch(Exception e){
            Log.e(TAG, "Error reading byte[] of file");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteFile(String path) {
        try{
            File file = new File(path);
            if(file.exists() && file.isFile()){
                return file.delete();
            }
        }
        catch(Exception e){
            Log.e(TAG, "Could not delete file!");
            e.printStackTrace();
        }
        return false;
    }
}
