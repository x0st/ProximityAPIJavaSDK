package proximity.sdk.entity;

import org.json.JSONObject;

import java.math.BigInteger;

public class User {
    private Integer id;
    private String name;
    private String email;
    private String avatar;
    private String googleUserId;
    private BigInteger createdAt;
    private BigInteger updatedAt;
    private Boolean confirmedProfile;
    private Boolean liked;

    public User(
            Integer id,
            String name,
            String email,
            String avatar,
            String googleUserId,
            BigInteger createdAt,
            BigInteger updatedAt,
            Boolean confirmedProfile,
            Boolean liked
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.googleUserId = googleUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.confirmedProfile = confirmedProfile;
        this.liked = liked;
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

    public BigInteger getCreatedAt() {
        return createdAt;
    }

    public BigInteger getUpdatedAt() {
        return updatedAt;
    }

    public Boolean confirmedProfile() {
        return confirmedProfile;
    }

    public Boolean getLiked() {
        return liked;
    }

    public static User fromJSONObject(JSONObject json) {
        return new User(
                json.getInt("id"),
                String.valueOf(json.get("name")),
                String.valueOf(json.get("email")),
                String.valueOf(json.get("avatar")),
                String.valueOf(json.get("google_user_id")),
                json.getBigInteger("created_at"),
                json.isNull("updated_at") ? null : json.getBigInteger("updated_at"),
                json.getBoolean("confirmed_profile"),
                json.has("liked") ? json.getBoolean("liked") : null
        );
    }
}
