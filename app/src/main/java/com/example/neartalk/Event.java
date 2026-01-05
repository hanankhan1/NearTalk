package com.example.neartalk;

import java.io.Serializable;

public class Event implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String category;
    private String title;
    private String description;
    private String date;
    private String time;
    private String location;
    private long timestamp;
    private long attendees;


    public Event() {}


    public Event(String id, String userId, String userName, String category, String title, String description,
                 String date, String time, String location, long timestamp, long attendees) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.category = category;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.timestamp = timestamp;
        this.attendees = attendees;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getAttendees() { return attendees; }
    public void setAttendees(long attendees) { this.attendees = attendees; }
}