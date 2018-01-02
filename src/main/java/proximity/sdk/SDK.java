package proximity.sdk;

import org.json.JSONArray;
import org.json.JSONObject;

import postman.Client;
import postman.PostmanException;
import postman.request.BodyLessRequest;
import postman.request.JSONObjectRequest;
import postman.response.Response;
import proximity.sdk.entity.*;
import proximity.sdk.exception.HttpException;

import java.util.ArrayList;

public class SDK {
    private String host;
    private String sessionToken;
    private Client postman;

    private String refreshToken;
    private Boolean automaticTokenUpdate = false;
    private SessionTokenRenewalCallback onTokenUpdateCallback;

    /**
     * @param host         to work with
     * @param sessionToken to identify a user
     */
    public SDK(String host, String sessionToken) {
        this.host = host;
        this.sessionToken = sessionToken;
        this.postman = new Client();
    }

    /**
     * @param host to work with
     */
    public SDK(String host) {
        this(host, null);
    }

    /**
     * @param postman http client
     */
    public void setPostman(Client postman) {
        this.postman = postman;
    }

    /**
     * @return whether an session token should be updated automatically in case of the "UNAUTHENTICATED"
     */
    public Boolean shouldUpdateTokenAutomatically() {
        return automaticTokenUpdate;
    }

    /**
     * @param automaticTokenUpdate whether an session token should be updated automatically in case of the "UNAUTHENTICATED"
     */
    public void setAutomaticTokenUpdate(Boolean automaticTokenUpdate) {
        this.automaticTokenUpdate = automaticTokenUpdate;
    }

    /**
     * @param sessionToken to identify a user
     */
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * @return a refresh token to update request an session token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * @param refreshToken a refresh token to update request an session token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return a callback that will be invoked when an session token is automatically updated
     */
    public SessionTokenRenewalCallback getOnTokenUpdateCallback() {
        return onTokenUpdateCallback;
    }

    /**
     * @param onTokenUpdateCallback a callback that will be invoked when an session token is automatically updated
     */
    public void setOnTokenUpdateCallback(SessionTokenRenewalCallback onTokenUpdateCallback) {
        this.onTokenUpdateCallback = onTokenUpdateCallback;
    }

    /**
     * Get information about the owner of session token.
     *
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void userInfo(final UserInfoCallback callback, final ErrorCallback errorCallback) {
        BodyLessRequest request;

        // a request
        request = new BodyLessRequest(BodyLessRequest.Method.GET, this.castUrl("/user/me"));
        request.setHeader("X-Authorization", this.sessionToken);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                User user = User.fromJSONObject(response.getBody().getJSONObject("user"));

                callback.info(user);
            }

            @Override
            public void sessionTokenUpdated() {
                userInfo(callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.runtime(e);
            }
        });
    }

    /**
     * To renew session token if current one has expired.
     *
     * @param refreshToken  which will be used to produce a new session token
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void renewSessionToken(final String refreshToken, final SessionTokenRenewalCallback callback, final ErrorCallback errorCallback) {
        ListenerJSONObjectAdapter listener;
        JSONObjectRequest request;
        JSONObject body;

        // a request body
        body = new JSONObject();
        body.put("token", refreshToken);

        // a request
        request = new JSONObjectRequest(JSONObjectRequest.Method.PATCH, this.castUrl("/session"));
        request.setBody(body);

        // listener invoked after the server responded
        listener = new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                Session session = new Session(
                        response.getBody().getString("session_token"),
                        response.getBody().getInt("session_token_expiration")
                );

                RefreshToken refreshToken = new RefreshToken(
                        response.getBody().getString("refresh_token")
                );

                callback.renewed(session, refreshToken);
            }
        };

        listener.setTryUpdatingSessionToken(false);

        this.postman.asJSONObjectAsync(request, listener, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.runtime(e);
            }
        });
    }

    /**
     * Sign in a user through the use of a google session token.
     *
     * @param token         google session token
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void signInViaGoogle(String token, final LoginCallback callback, final ErrorCallback errorCallback) {
        ListenerJSONObjectAdapter listener;
        JSONObjectRequest request;
        JSONObject body;

        // a request body
        body = new JSONObject();
        body.put("token", token);

        // a request
        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/sign-in/google"));
        request.setBody(body);

        // listener invoked after the server responded
        listener = new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                User user = User.fromJSONObject(response.getBody().getJSONObject("user"));

                Session session = new Session(
                        response.getBody().getString("session_token"),
                        response.getBody().getInt("session_token_expiration")
                );

                RefreshToken refreshToken = new RefreshToken(
                        response.getBody().getString("refresh_token")
                );

                callback.loggedIn(user, session, refreshToken);
            }
        };

        listener.setTryUpdatingSessionToken(false);

        this.postman.asJSONObjectAsync(request, listener, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.runtime(e);
            }
        });
    }

    /**
     * Register a user through the use of a google session token.
     *
     * @param token         google session token
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void registerViaGoogle(String token, final RegistrationCallback callback, final ErrorCallback errorCallback) {
        ListenerJSONObjectAdapter listener;
        JSONObjectRequest request;
        JSONObject body;

        body = new JSONObject();
        body.put("token", token);

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/register/google"));
        request.setBody(body);

        listener = new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                User user = User.fromJSONObject(response.getBody().getJSONObject("user"));

                Session session = new Session(
                        response.getBody().getString("session_token"),
                        response.getBody().getInt("session_token_expiration")
                );

                RefreshToken refreshToken = new RefreshToken(
                        response.getBody().getString("refresh_token")
                );

                callback.registered(user, session, refreshToken);
            }
        };
        listener.setTryUpdatingSessionToken(false);

        this.postman.asJSONObjectAsync(request, listener, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.runtime(e);
            }
        });
    }

    /**
     * @param intent contains fields to be updated
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void updateProfile(
            final ProfileUpdateIntent intent,
            final ProfileUpdateCallback callback,
            final ErrorCallback errorCallback
    ) {
        JSONObjectRequest request;
        JSONObject body;

        body = intent.toJSONObject();

        request = new JSONObjectRequest(JSONObjectRequest.Method.PATCH, this.castUrl("/user"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(body);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                User user = User.fromJSONObject(response.getBody().getJSONObject("user"));

                callback.updatedProfile(user);
            }

            @Override
            public void sessionTokenUpdated() {
                updateProfile(intent, callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.runtime(e);
            }
        });
    }

    /**
     * @param types         'cafe', 'restaurant'
     * @param radius        20
     * @param location      latitude and longitude
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void getNearbyPlaces(
            final ArrayList<String> types,
            final Integer radius,
            final Location location,
            final NearbyPlacesCallback callback,
            final ErrorCallback errorCallback
    ) {
        BodyLessRequest request;

        request = new BodyLessRequest(BodyLessRequest.Method.GET, this.castUrl("/nearby-places"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.addField("latitude", String.valueOf(location.getLatitude()));
        request.addField("longitude", String.valueOf(location.getLongitude()));
        request.addField("radius", radius.toString());

        for (String type : types) {
            request.addField("types[]", type);
        }

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                ArrayList<Place> places = new ArrayList<Place>();
                JSONArray placesAsJSONArray = response.getBody().getJSONArray("places");

                for (Object place : placesAsJSONArray) {
                    JSONObject placeAsJSONObject = ((JSONObject) place);

                    Location location = new Location(
                            Double.valueOf(placeAsJSONObject.getJSONObject("location").getString("latitude")),
                            Double.valueOf(placeAsJSONObject.getJSONObject("location").getString("longitude"))
                    );
                    String address = placeAsJSONObject.getString("address");
                    String placeId = placeAsJSONObject.getString("placeId");
                    String name = placeAsJSONObject.getString("name");

                    places.add(new Place(name, address, placeId, location));
                }

                callback.places(places);
            }

            @Override
            public void sessionTokenUpdated() {
                getNearbyPlaces(types, radius, location, callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.runtime(e);
            }
        });
    }

    public interface ErrorCallback {
        public void http(HttpException exception);

        public void runtime(Throwable exception);
    }

    public interface SessionTokenRenewalCallback {
        public void renewed(Session session, RefreshToken refreshToken);
    }

    public interface ProfileUpdateCallback {
        public void updatedProfile(User user);
    }

    public interface NearbyPlacesCallback {
        public void places(ArrayList<Place> places);
    }

    public interface UserInfoCallback {
        public void info(User user);
    }

    public interface LoginCallback {
        public void loggedIn(User user, Session session, RefreshToken refreshToken);
    }

    public interface RegistrationCallback {
        public void registered(User user, Session session, RefreshToken refreshToken);
    }

    public static class ProfileUpdateIntent {
        private String avatarAsBase64;
        private String name;

        public String getName() {
            return name;
        }

        public String getAvatarAsBase64() {
            return avatarAsBase64;
        }

        public void setAvatarAsBase64(String avatarAsBase64) {
            this.avatarAsBase64 = avatarAsBase64;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JSONObject toJSONObject() {
            JSONObject jsonObject;
            jsonObject = new JSONObject();

            if (getName() != null) jsonObject.put("name", getName());
            if (getAvatarAsBase64() != null) jsonObject.put("avatar_as_base64", getAvatarAsBase64());

            return jsonObject;
        }
    }

    private String castUrl(String path) {
        return String.format("%s%s", this.host, path);
    }
}
