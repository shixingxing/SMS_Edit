package com.example.ducdung.sms_edit.model;

public class SmsThread {
    public int theadId;
    public String avatar;
    public String from;
    public String displayName;
    public String content;
    public long timestamp;


    public SmsThread(int theadId, String from, String content, long timestamp) {
        this.theadId = theadId;
        this.from = from;
        this.content = content;
        this.timestamp = timestamp;
    }
    public String toString() {
        return "SmsThread{theadId=" + this.theadId + ", avatar='" +
                this.avatar + '\'' + ", from='" + this.from + '\'' +
                ", displayName='" + this.displayName + '\'' + ", content='" +
                this.content + '\'' + ", timestamp=" + this.timestamp + '}' ;
    }
}
