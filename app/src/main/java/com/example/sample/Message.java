package com.example.sample;

public class Message {
    String message,from,type;
    long time;
    boolean seen;

    Message(){}
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Message(String message, String from, String type, long time, boolean seen) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }
}
