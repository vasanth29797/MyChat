package com.elite.mychat;

/**
 * Created by evk29 on 28-01-2018.
 */

public class Messages {
    private String message;
    private String type;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    private String from;
    private Boolean seen;
    private long timestamp;

    public Messages() {
    }

    public Messages(String message, String type, Boolean seen, long timestamp, String from) {
        this.message = message;
        this.type = type;
        this.seen = seen;
        this.timestamp = timestamp;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
