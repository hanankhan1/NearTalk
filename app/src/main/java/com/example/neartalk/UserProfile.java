package com.example.neartalk;

public class UserProfile {

    private String userId;
    private String userName;
    private String email;
    private String neighbourhood;
    private String about;
    private String profileImageUrl;

    public UserProfile() {
    }

    public UserProfile(String userId, String userName, String email, String neighbourhood, String about, String profileImageUrl) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.neighbourhood = neighbourhood;
        this.about = about;
        this.profileImageUrl = profileImageUrl;
    }

    // GETTERS
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getEmail() { return email; }
    public String getNeighbourhood() { return neighbourhood; }
    public String getAbout() { return about; }
    public String getProfileImageUrl() { return profileImageUrl; }

    // SETTERS
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setEmail(String email) { this.email = email; }
    public void setNeighbourhood(String neighbourhood) { this.neighbourhood = neighbourhood; }
    public void setAbout(String about) { this.about = about; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
