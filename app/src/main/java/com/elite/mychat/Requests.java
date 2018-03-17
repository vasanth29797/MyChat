package com.elite.mychat;

/**
 * Created by evk29 on 29-01-2018.
 */

public class Requests {

    private String request_type;
    private long timestamp;

    public Requests(String request_type, long timestamp) {
        this.request_type = request_type;
        this.timestamp = timestamp;
    }

    public Requests() {
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
