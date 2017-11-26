package proximity.sdk.entity;

public class Session {
    private String token;
    private Integer expiration;

    public Session(String token, Integer expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    public String getToken() {
        return token;
    }

    /**
     * @return expiration in minutes
     */
    public Integer getExpiration() {
        return expiration;
    }
}
