package com.hns.download;



public class Message {
    private Integer progress;
    private String message;
    private long currentFilelength;
    private long progressLength;

    public Message(Integer progress, String message) {
        this.progress = progress;
        this.message = message;
    }


    public Message(Integer progress, long progressLength, long currentFilelength) {
        this.progress = progress;
        this.currentFilelength = currentFilelength;
        this.progressLength = progressLength;
        this.message = "";
    }


    public Message(String message) {
        this.progress = 0;
        this.message = message;
        System.out.println("message = " + message);
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCurrentFilelength() {
        return currentFilelength;
    }

    public void setCurrentFilelength(long currentFilelength) {
        this.currentFilelength = currentFilelength;
    }

    public long getProgressLength() {
        return progressLength;
    }

    public void setProgressLength(long progressLength) {
        this.progressLength = progressLength;
    }

    public String toString() {
        return "Message{" +
                "progress=" + progress +
                ", message='" + message + '\'' +
                '}';
    }
}