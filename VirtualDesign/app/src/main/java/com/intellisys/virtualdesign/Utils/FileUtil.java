package com.intellisys.virtualdesign.Utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by serpe_000 on 17/12/2014.
 */
public class FileUtil {

    public final String TAG = "Utils.java";
    public File createFileInStorage(Context context, String url){
        File file = null;

        try{
            String filename = Uri.parse(url).getLastPathSegment();

            if(isExternalStorageWritable()){
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/virtualdesign/");

                myDir.mkdirs();

                file = new File(myDir, filename);
            }else{
                file = File.createTempFile(filename, null, context.getCacheDir());
            }

        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        return file;
    }

    public File createFileCache(Context context, String url){

        File file = null;

        try{
            String filename = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(filename, null, context.getCacheDir());

        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        return file;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
