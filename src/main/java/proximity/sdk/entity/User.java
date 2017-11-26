package proximity.sdk.entity;

import org.json.JSONObject;

public class User {
    private Integer id;
    private String name;
    private String email;
    private String avatar;
    private String googleUserId;
    private String createdAt;
    private String updatedAt;

    public User(Integer id, String name, String email, String avatar, String googleUserId, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.googleUserId = googleUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public static User fromJSONObject(JSONObject json) {
        return new User(
                json.getInt("id"),
                String.valueOf(json.get("name")),
                String.valueOf(json.get("email")),
                String.valueOf(json.get("avatar")),
                String.valueOf(json.get("google_user_id")),
                json.getString("created_at"),
                json.getString("updated_at")
        );
    }
}
