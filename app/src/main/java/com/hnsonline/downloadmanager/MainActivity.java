package com.hnsonline.downloadmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hns.download.AppInfo;
import com.hns.download.CommonUtils;
import com.hns.download.DownloadService;
import com.hns.download.UnpackDataAsyncTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.hns.download.ResumableDownloader.COMPLETE;
import static com.hns.download.ResumableDownloader.DOWNLOADING;
import static com.hns.download.ResumableDownloader.ERROR;
import static com.hns.download.ResumableDownloader.PAUSE;


public class MainActivity extends AppCompatActivity {
    private int downloadResourceListCount = 0;
    private int count = 0;
    DownloadReceiver mReceiver;
    String path;
    TextView tvProgressSize,tvTotalFiles,tvTotalSize,progressStatus;
    ProgressBar progressbar;
    File directory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvProgressSize=findViewById(R.id.tvProgressSize);
        tvTotalFiles=findViewById(R.id.tvTotalFiles);
        tvTotalSize=findViewById(R.id.tvTotalSize);
        progressbar=findViewById(R.id.progressbar);
        progressStatus=findViewById(R.id.progressStatus);

        directory = this.getExternalFilesDir("Data");
        String resourcename = 1 + "_" + "data" + "_";
        String videoname = resourcename + ".zip";
        File file = new File(directory, videoname);
        path = file.getPath();
       // count=1;
        List<AppInfo> downloadResourceList = new ArrayList<>();
        downloadResourceList.add(new AppInfo(1, "NAME", "URL", path, "Zip"));
        DownloadService.intentDownload(MainActivity.this, count , "HEADER","GET", downloadResourceList.get(0));
        register();
    }

    private void register() {
        tvProgressSize.setText("Please wait.");
        tvTotalFiles.setVisibility(View.VISIBLE);
        tvTotalSize.setVisibility(View.VISIBLE);
        mReceiver = new DownloadReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
        }
    }
    public void downloadinterruped() {
    }
    private void updateTextInfo(String fileSize,int percentage, int infocount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvProgressSize.setText("Please wait for the download to complete.");
                tvTotalFiles.setText("Completed " + count + " out of " + infocount);
                tvTotalSize.setText(fileSize);
                progressbar.setProgress(percentage);
                progressStatus.setText("Downloading..");
                Log.d("Downloading :",  "  Progress :" + percentage);
            }
        });
    }

    public class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null || !action.equals(DownloadService.ACTION_DOWNLOAD_BROAD_CAST)) {
                return;
            }
            final int position = intent.getIntExtra(DownloadService.EXTRA_POSITION, -1);
            final String downloadType = intent.getStringExtra(DownloadService.EXTRA_TAG);
            final AppInfo tmpInfo = (AppInfo) intent.getSerializableExtra(DownloadService.EXTRA_APP_INFO);
            if (tmpInfo == null || position == -1) {
                return;
            }
            final int status = tmpInfo.getStatus();
            switch (status) {
                case DOWNLOADING:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String totalDownloadFileLength = CommonUtils.convertFileLengthFromkbToKBMB(tmpInfo.getFilelength());
                            String completedDownloadFileLength = CommonUtils.convertFileLengthFromkbToKBMB(tmpInfo.getCurrentdownloadlength());
                            String tagSuccess = String.format(context.getString(R.string.download_content_size_text), totalDownloadFileLength, completedDownloadFileLength);
                            int downloadPercentatge = tmpInfo.getProgress();
                            updateTextInfo(tagSuccess, downloadPercentatge, downloadResourceListCount);
                        }
                    });
                    break;
                case COMPLETE:
                    unRegister();
                    tvProgressSize.setText("");
                    tvTotalFiles.setText("");
                    tvTotalSize.setText("Completed");
                    tmpInfo.setStatus(PAUSE);
                    if(downloadType.equals("Zip"))
                    {
                        unzipDataPack(tmpInfo, downloadType);
                    }
                    count++;
                    break;
                case ERROR:
                    if (tvProgressSize != null) {
                        tvProgressSize.setText(R.string.download_interrupted);
                        downloadinterruped();
                    }
                    break;
                case PAUSE:
                    break;
                default:
                    break;
            }
        }
    }

    public void unPackData(Context context, String localPath, UnpackDataAsyncTask.OnDataUnpackListener onDataUnpackListener) {
        UnpackDataAsyncTask unpackDataAsyncTask = new UnpackDataAsyncTask(context, onDataUnpackListener);
        unpackDataAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, localPath, directory.toString());
    }

    private void unzipDataPack(final AppInfo appInfo, final String downloadType) {
        WeakReference<MainActivity> mainActivityWeakRef = new WeakReference<>(this);;
        if (mainActivityWeakRef.get() != null && !mainActivityWeakRef.get().isFinishing()) {
            tvTotalFiles.setText("Unpacking...");
            tvTotalSize.setText("");
            progressbar.setProgress(0);
            progressStatus.setText(String.valueOf(0));
            unPackData(this, appInfo.getLocalPath(), new UnpackDataAsyncTask.OnDataUnpackListener() {
                @Override
                public void onUnpackDone() {
                    tvTotalFiles.setText("Unpacking Done");
                    progressbar.setProgress(0);
                    progressStatus.setText("Completed");
                    File file=new File(appInfo.getLocalPath());
                    file.delete();
                }
                @Override
                public void onUnpackProgress(int aPercent) {
                    if (progressbar != null) {
                        progressbar.setProgress(aPercent);
                        progressStatus.setText(String.valueOf(aPercent) + " %");
                    }
                }
                @Override
                public void onUnpackError() {
                    if (tvTotalFiles != null) {
                        tvTotalFiles.setText("UnZip Interupted");
                    }
                }
            });
        }
    }
}