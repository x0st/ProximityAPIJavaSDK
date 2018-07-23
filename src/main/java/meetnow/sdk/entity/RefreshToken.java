package meetnow.sdk.entity;

public class RefreshToken {
    private String token;

    public RefreshToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
