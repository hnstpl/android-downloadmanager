package com.hns.download;


import java.io.Serializable;


public class AppInfo implements Serializable {

    private String name;
    private String packageName;
    private int id;
    private String lastModified;
    private String url;
    private String localPath;
    private int progress;
    private String downloadPerSize;
    private int status;
    private int contentId;
    private long filelength;
    private long currentdownloadlength;
    private int count;
    private String contenttype;

    public long getTotlefilelength() {
        return totlefilelength;
    }

    public void setTotlefilelength(long totlefilelength) {
        this.totlefilelength = totlefilelength;
    }

    private long totlefilelength;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getContenttype() {
        return contenttype;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public AppInfo(int contentId, String name, String url, String localPath, String contenttype) {
        this.name = name;
        this.contentId = contentId;
        this.url = url;
        this.localPath = localPath;
        //this.count = count;
        this.contenttype=contenttype;
    }

    public long getCurrentdownloadlength() {
        return currentdownloadlength;
    }

    public void setCurrentdownloadlength(long currentdownloadlength) {
        this.currentdownloadlength = currentdownloadlength;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDownloadPerSize() {
        return downloadPerSize;
    }

    public void setDownloadPerSize(String downloadPerSize) {
        this.downloadPerSize = downloadPerSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public long getFilelength() {
        return filelength;
    }

    public void setFilelength(long filelength) {
        this.filelength = filelength;
    }
}