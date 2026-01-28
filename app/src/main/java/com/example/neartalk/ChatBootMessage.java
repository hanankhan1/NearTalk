package com.example.neartalk;

public class ChatBootMessage {
    public String message;
    public boolean isUser;
    public long timestamp;

    public ChatBootMessage() {}

    public ChatBootMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }
}
