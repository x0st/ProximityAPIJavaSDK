package meetnow.sdk.entity;

import org.json.JSONObject;

public class User {
    private Integer gender;
    private Integer id;
    private String name;
    private String email;
    private String avatar;
    private String googleUserId;
    private long createdAt;
    private Boolean confirmedProfile;
    private Boolean liked;

    public User(
            Integer id,
            String name,
            String email,
            String avatar,
            String googleUserId,
            long createdAt,
            Boolean confirmedProfile,
            Boolean liked,
            Integer gender
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.googleUserId = googleUserId;
        this.createdAt = createdAt;
        this.confirmedProfile = confirmedProfile;
        this.liked = liked;
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Boolean confirmedProfile() {
        return confirmedProfile;
    }

    public Boolean getLiked() {
        return liked;
    }
    
    public Integer getGender() {
        return gender;
    }

    public void setLiked(Boolean value) {
        liked = value;
    }

    public static User fromJSONObject(JSONObject json) {
        return new User(
                json.getInt("id"),
                String.valueOf(json.get("name")),
                String.valueOf(json.get("email")),
                String.valueOf(json.get("avatar")),
                String.valueOf(json.get("google_user_id")),
                json.getLong("created_at"),
                json.getBoolean("confirmed_profile"),
                json.has("liked") ? json.getBoolean("liked") : null,
                json.isNull("gender") ? null : json.getInt("gender")
        );
    }
}
