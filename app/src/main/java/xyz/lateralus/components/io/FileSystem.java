package xyz.lateralus.components.io;


import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;


public class FileSystem {

    private static final String LOG = "LateralusFile";

    public static Boolean saveJpg(Bitmap img, String saveDir, String fileName){
        Log.d(LOG, "Saving JPG to: " + saveDir + fileName);
        try {
            File dir = new File(saveDir);
            dir.mkdirs();
            File file = new File(saveDir, fileName);
            FileOutputStream fout = new FileOutputStream(file);
            img.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
            return true;
        }
        catch(Exception e) {
            Log.d(LOG, "Save JPG Failed!");
            e.printStackTrace();
            return false;
        }
    }
}
