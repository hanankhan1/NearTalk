package com.example.neartalk;
import java.util.List;

public class Post {

    private String postId;
    private String userId;
    private String userName;
    private String type;
    private String title;
    private String description;
    private String price;
    private List<String> imageUrls;
    private long timestamp;

    public Post() {}

    public Post(String postId, String userId, String userName, String type,
                String title, String description, String price,
                List<String> imageUrls, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;
        this.timestamp = timestamp;
    }

    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public List<String> getImageUrls() { return imageUrls; }
    public long getTimestamp() { return timestamp; }
}
