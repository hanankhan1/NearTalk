package com.example.neartalk;

import java.util.List;

public class AreaGroup {

    private String id;
    private String areaName;
    private long createdAt;
    private int memberCount;
    private List<String> members;

    private String lastMessage;
    private long lastMessageTime;
    private String lastMessageSenderId;

    public AreaGroup() {}

    public AreaGroup(String id,
                     String areaName,
                     long createdAt,
                     int memberCount,
                     List<String> members,
                     String lastMessage,
                     long lastMessageTime,
                     String lastMessageSenderId) {

        this.id = id;
        this.areaName = areaName;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.members = members;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    // ---------------- GETTERS ----------------

    public String getId() { return id; }
    public String getAreaName() { return areaName; }
    public long getCreatedAt() { return createdAt; }
    public int getMemberCount() { return memberCount; }
    public List<String> getMembers() { return members; }

    public String getLastMessage() {
        return lastMessage == null ? "No messages yet" : lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    // ---------------- SETTERS ----------------

    public void setId(String id) { this.id = id; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public void setMembers(List<String> members) { this.members = members; }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }
}
