package com.hns.download;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class UnpackDataAsyncTask extends AsyncTask<String, Integer, Integer> {
    private static final int SUCCESS = 0;
    private static final int ERROR_UNPACK = 1;
    private final Context mContext;
    private OnDataUnpackListener mOnDataUnpackListener;
    private String zipFileLocation, unzipLocation;
    public UnpackDataAsyncTask(Context context, OnDataUnpackListener aOnDataUnpackListener) {
        mContext = context;
        mOnDataUnpackListener = aOnDataUnpackListener;
    }
    @Override
    protected Integer doInBackground(String[] params) {
        // Start Unpacking
        int retVal = SUCCESS;
        File targetDirectory;
        try {
            zipFileLocation = params[0];
            unzipLocation = params[1];
            File zipFile = new File(zipFileLocation);
            targetDirectory = new File(unzipLocation);
            long total_len = zipFile.length();
            long total_installed_len = 0;
            InputStream is = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    if (mOnDataUnpackListener != null) {
                        total_installed_len += ze.getCompressedSize();
                        String file_name = ze.getName();
                        int percent = (int) (total_installed_len * 100 / total_len);
                        publishProgress(percent);
                    }
                    File file = new File(targetDirectory, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } finally {
                        fout.close();
                    }
                    // if time should be restored as well
                    long time = ze.getTime();
                    if (time > 0)
                        file.setLastModified(time);
                }
            } finally {
                zis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            retVal = ERROR_UNPACK;
        }
        listFilesAndDirs(0, mContext.getFilesDir());
        return retVal;
    }
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mOnDataUnpackListener != null) {
            mOnDataUnpackListener.onUnpackProgress(values[0]);
        }
    }
    @Override
    protected void onPostExecute(Integer aResult) {
        super.onPostExecute(aResult);
        if (mOnDataUnpackListener != null) {
            if (aResult == SUCCESS) {
                File file = new File(zipFileLocation);
                if (file.exists()) {
                    file.delete();
                    System.out.println("success delete");
                }
                mOnDataUnpackListener.onUnpackDone();
            } else {
                mOnDataUnpackListener.onUnpackError();
            }
        }
    }
    private void listFilesAndDirs(int aLev, File file) {
        System.out.println("aLev = " + aLev);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            String space = " ";
            for (int j = 0; j < aLev; j++) {
                space = space + "    ";
            }
            if (files[i].isDirectory()) {
                Log.d("Dir Struct", "DIR" + space + files[i].getName());
                listFilesAndDirs(aLev + 1, files[i]);
            } else {
                Log.d("Dir Struct", "FILE" + space + files[i].getName());
            }
        }
    }
    public interface OnDataUnpackListener {
        void onUnpackDone();
        void onUnpackProgress(int aPercent);
        void onUnpackError();
    }
}
