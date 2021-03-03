package com.hns.download;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResumableDownloader extends Thread {
    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;
    public static final int PAUSE = 2;
    public static final int ERROR = 3;
    public static final int CANCEL = 4;
    public static final int FILENOTDOWNLOAD = 5;
    public static final int BUFFER_SIZE = 8 * 1024;
    HttpURLConnection connection;
    DownloadListener downloadListener;
    private String lastModified;
    private int timeout;
    private File downloadedFile;
    private boolean startNewDownload;
    private long fileLength = 0;
    private int status;
    private String[] statuses;
    private AppInfo appInfo;
    private String toFile;
    private String methodtype;
    private String header;

    ResumableDownloader(AppInfo appInfo, String tofile, String methodtype, String header, DownloadListener downloadListener) {
        this.appInfo = appInfo;
        this.downloadListener = downloadListener;
        this.lastModified = appInfo.getLastModified();
        timeout = 15000;
        startNewDownload = true;
        this.status = appInfo.getStatus();
        this.toFile = tofile;
        statuses = new String[]{"Downloading", "Complete", "Pause", "Error"};
        this.methodtype = methodtype;
        this.header = header;
    }

    @Override
    public void run() {
        try {
            downloadFile(appInfo);
        } catch (IOException e) {
            e.printStackTrace();
            File deteleFile = new File(appInfo.getLocalPath());
            if (deteleFile.exists())
                deteleFile.delete();
            setStatus(ERROR);
            appInfo.setStatus(ERROR);
            connection.disconnect();
            downloadListener.progressUpdate(new Message(e.getMessage()));
            System.out.println("e.getMessage() = " + e.getMessage());
            System.out.println("progress = ERROR");
        }
    }

    /**
     * @throws IOException
     */

    public void downloadFile(AppInfo appInfo) throws IOException {
        long alreadyDownloadFile = 0;
        URL url = new URL(appInfo.getUrl());
        connection = createConnection(url, downloadListener,header,methodtype);
        fileLength = connection.getContentLength();
        appInfo.setTotlefilelength(fileLength);
        if (downloadedFile.exists()) {
            if (downloadedFile.delete()) {
                System.out.println(" = Delete download file ");
            }

        }
        startNewDownload = false;
        setStatus(DOWNLOADING);
        appInfo.setStatus(DOWNLOADING);
        InputStream in = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
        FileOutputStream writer;
        long progressLength = 0;
        int count = 0;
        if (!startNewDownload) {
            progressLength += downloadedFile.length();
            writer = new FileOutputStream(toFile, true);
        } else {
            writer = new FileOutputStream(toFile);
            lastModified = connection.getHeaderField("Last-Modified");
        }
        try {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (getStatus() == DOWNLOADING && (count = in.read(buffer)) != -1) {
                progressLength += count;
                writer.write(buffer, 0, count);
                downloadListener.progressUpdate(new Message((int) (progressLength * 100 / (fileLength)), progressLength, fileLength));
                if (progressLength == fileLength) {
                    if (progressLength > 0) {
                        appInfo.setStatus(COMPLETE);
                        setStatus(COMPLETE);
                        downloadListener.progressUpdate(new Message((int) (progressLength * 100 / (fileLength)), progressLength, fileLength));
                        System.out.println("progress = " + progressLength + " >><< fileLength = " + fileLength);
                        progressLength = 0;
                        alreadyDownloadFile = 0;
                        System.out.println("progress = COMPLETED");
                    } else {
                        File delteFile = new File(appInfo.getLocalPath());
                        if (delteFile.exists())
                            delteFile.delete();
                        setStatus(ERROR);
                        appInfo.setStatus(ERROR);
                        connection.disconnect();
                        downloadListener.progressUpdate(new Message((int) (progressLength * 100 / (fileLength)), progressLength, fileLength));
                        System.out.println("progress = ERROR");
                    }
                } else if (fileLength <= 0) {
                    File delteFile = new File(appInfo.getLocalPath());
                    if (delteFile.exists())
                        delteFile.delete();
                    setStatus(ERROR);
                    appInfo.setStatus(ERROR);
                    connection.disconnect();
                    downloadListener.progressUpdate(new Message((int) (progressLength * 100 / (fileLength)), progressLength, fileLength));
                    System.out.println("progress = ERROR");
                }
            }
        } finally {
            writer.close();
            in.close();
            connection.disconnect();
        }
    }



    /**
     * @param url              url string
     * @param downloadListener
     * @return An URLConnection for HTTP
     * @throws IOException
     */
    private HttpURLConnection createConnection(URL url, DownloadListener downloadListener,String header,String methodtype) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Open connection to URL.
        conn.setDoInput(true);
        // conn.setDoOutput(true);
        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(timeout);
        conn.setRequestMethod(methodtype);
        downloadedFile = new File(toFile);
        if (!startNewDownload) {
            conn.setRequestProperty("Range", "bytes=" + downloadedFile.length() + "-");
        }
        return conn;
    }

    /**
     * @return status as a String
     */
    public String getStatusStr() {
        return statuses[getStatus()];
    }

    /**
     * @return status corresponding number
     */
    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;

    }

    public String getLastModified() {
        return lastModified;
    }

    public interface DownloadListener {

        void progressUpdate(Message value);
    }
}
