package com.hns.download;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import static com.hns.download.ResumableDownloader.DOWNLOADING;
import static com.hns.download.ResumableDownloader.ERROR;

public class DownloadService extends Service {
    public static final String ACTION_DOWNLOAD_BROAD_CAST = "com.hns.download:action_download_broad_cast";
    public static final String ACTION_DOWNLOAD = "com.hns.download:action_download";
    public static final String ACTION_PAUSE = "com.hns.download:action_pause";
    public static final String ACTION_CANCEL = "com.hns.download:action_cancel";
    public static final String EXTRA_POSITION = "extra_position";
    public static final String EXTRA_METHODTYPE= "method_type";
    public static final String EXTRA_HEADER= "header";
    public static final String EXTRA_TAG = "extra_tag";
    public static final String EXTRA_APP_INFO = "extra_app_info";
    private static final String TAG = DownloadService.class.getSimpleName();
    ResumableDownloader mDownloadThread;
    private boolean checkUIStatus = true;
    static Intent intent;
    public static void intentDownload(Context context, int position, String header,String methodtype, AppInfo info) {
        intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_METHODTYPE, methodtype);
        intent.putExtra(EXTRA_HEADER, header);
        intent.putExtra(EXTRA_TAG, info.getContenttype());
        intent.putExtra(EXTRA_APP_INFO, info);
        context.startService(intent);
    }
    public static void intentPause(Context context, String tag) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(EXTRA_TAG, tag);
        context.startService(intent);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            int position = intent.getIntExtra(EXTRA_POSITION, 0);
            String methodtype = intent.getStringExtra(EXTRA_METHODTYPE);
            String header = intent.getStringExtra(EXTRA_HEADER);
            AppInfo appInfo = (AppInfo) intent.getSerializableExtra(EXTRA_APP_INFO);
            String tag = intent.getStringExtra(EXTRA_TAG);
            switch (action) {
                case ACTION_DOWNLOAD:
                    download(position, appInfo, tag, methodtype, header);
                    break;
                case ACTION_PAUSE:
                    //  pause(tag);
                    break;
                case ACTION_CANCEL:
                    // cancel(tag);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void download(final int position, final AppInfo appInfo, String tag, String methodtype, String header) {
        checkUIStatus = true;
        if (NetworkUtils.isConnectingToInternet(this)) {
            mDownloadThread = new ResumableDownloader(appInfo, appInfo.getLocalPath(), methodtype,header,new DownLoadCallBack(appInfo, position, tag));
            mDownloadThread.start();
        } else {
            appInfo.setStatus(ERROR);
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
            intent.putExtra(EXTRA_APP_INFO, appInfo);
            intent.putExtra(EXTRA_TAG, "");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d("ClearFromRecentService", "Service Destroyed");
        if (mDownloadThread != null && mDownloadThread.isAlive()) {
            mDownloadThread.interrupt();
        }
        stopService(intent);
        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        stopSelf();
        if (mDownloadThread != null && mDownloadThread.isAlive()) {
            mDownloadThread.interrupt();
        }
    }
    public class DownLoadCallBack implements ResumableDownloader.DownloadListener {
        AppInfo appInfo;
        private int mPosition;
        private String downloadType;
        private LocalBroadcastManager mLocalBroadcastManager;

        DownLoadCallBack(AppInfo appInfo, int postion, String downloadType) {
            this.appInfo = appInfo;
            this.mPosition = postion;
            this.downloadType = downloadType;
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        }
        @Override
        public void progressUpdate(Message value) {
            appInfo.setProgress(value.getProgress());
            appInfo.setCurrentdownloadlength(value.getCurrentFilelength());
            appInfo.setFilelength(value.getProgressLength());
            //sendBroadCast(appInfo);
            if (appInfo.getStatus() == DOWNLOADING) {
                if ((appInfo.getProgress() > 95) || checkUIStatus)
                    sendBroadCast(appInfo);
            } else {
                sendBroadCast(appInfo);
            }
        }
        private void sendBroadCast(AppInfo appInfo) {
            Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
            intent.putExtra(EXTRA_POSITION, mPosition);
            intent.putExtra(EXTRA_APP_INFO, appInfo);
            intent.putExtra(EXTRA_TAG, downloadType);
            mLocalBroadcastManager.sendBroadcast(intent);
            if (appInfo.getStatus() == DOWNLOADING && checkUIStatus) {
                checkUIStatus = false;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkUIStatus = true;
                    }
                }, 50);
            }
        }
    }

}
