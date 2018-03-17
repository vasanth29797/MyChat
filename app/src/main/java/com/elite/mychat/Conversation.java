package com.elite.mychat;

/**
 * Created by evk29 on 29-01-2018.
 */

public class Conversation {
    private boolean seen;
    private long timestamp;

    public Conversation() {
    }

    public Conversation(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
