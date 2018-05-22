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
import java.util.List;

public class SDK {
    private String host;
    private String sessionToken;
    private Client postman;

    private String refreshToken;
    private Boolean automaticTokenUpdate = false;
    private SessionTokenRenewalCallback onTokenUpdateCallback;

    public enum Action {
        USER_INFO,
        RENEW_SESSION_TOKEN,
        SIGN_IN_VIA_GOOGLE,
        SIGN_UP_VIA_GOOGLE,
        UPDATE_PROFILE,
        GET_NEARBY_PLACES,
        LEAVE_CURRENT_PLACES,
        CHECK_IN_AT_PLACES,
        GET_NEARBY_USERS,
        LIKE_USER,
        ADD_FCM_TOKEN,
        HAS_LEFT_PLACES,
        PUSH_WIFI_NETWORKS,
        CONFIRM_CHECK_IN
    }

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
    public void userInfo(final SuccessCallback callback, final ErrorCallback errorCallback) {
        BodyLessRequest request;

        // a request
        request = new BodyLessRequest(BodyLessRequest.Method.GET, this.castUrl("/user/me"));
        request.setHeader("X-Authorization", this.sessionToken);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                User user = User.fromJSONObject(response.getBody().getJSONObject("user"));

                callback.onSDKActionSuccess(Action.USER_INFO, user);
            }

            @Override
            public void sessionTokenUpdated() {
                userInfo(callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
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
                errorCallback.exception(e);
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
    public void signInViaGoogle(String token, final SuccessCallback callback, final ErrorCallback errorCallback) {
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

                callback.onSDKActionSuccess(Action.SIGN_IN_VIA_GOOGLE, user, session, refreshToken);
            }
        };

        listener.setTryUpdatingSessionToken(false);

        this.postman.asJSONObjectAsync(request, listener, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
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
    public void registerViaGoogle(String token, final SuccessCallback callback, final ErrorCallback errorCallback) {
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

                callback.onSDKActionSuccess(Action.SIGN_UP_VIA_GOOGLE, user, session, refreshToken);
            }
        };

        listener.setTryUpdatingSessionToken(false);

        this.postman.asJSONObjectAsync(request, listener, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
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
            final SuccessCallback callback,
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

                callback.onSDKActionSuccess(Action.UPDATE_PROFILE, user);
            }

            @Override
            public void sessionTokenUpdated() {
                updateProfile(intent, callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
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
            final SuccessCallback callback,
            final ErrorCallback errorCallback
    ) {
        BodyLessRequest request;

        request = new BodyLessRequest(BodyLessRequest.Method.GET, this.castUrl("/nearby-places"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.addField("latitude", String.valueOf(location.getLatitude()));
        request.addField("longitude", String.valueOf(location.getLongitude()));
        request.addField("radius", radius.toString());
        request.addArray("types", types);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                ArrayList<Place> places = new ArrayList<Place>();
                JSONArray placesAsJSONArray = response.getBody().getJSONArray("places");

                for (int i = 0; i < placesAsJSONArray.length(); i++) {
                    JSONObject placeAsJSONObject = placesAsJSONArray.getJSONObject(i);

                    Location location = new Location(
                            Double.valueOf(placeAsJSONObject.getJSONObject("location").getString("latitude")),
                            Double.valueOf(placeAsJSONObject.getJSONObject("location").getString("longitude"))
                    );
                    Boolean isHere = placeAsJSONObject.getBoolean("is_here");
                    String address = placeAsJSONObject.getString("address");
                    String placeId = placeAsJSONObject.getString("place_id");
                    String name = placeAsJSONObject.getString("name");

                    places.add(new Place(name, address, placeId, isHere, location));
                }

                callback.onSDKActionSuccess(Action.GET_NEARBY_PLACES, places);
            }

            @Override
            public void sessionTokenUpdated() {
                getNearbyPlaces(types, radius, location, callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void leaveCurrentPlaces(final SuccessCallback callback, final ErrorCallback errorCallback) {
        JSONObjectRequest request;

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/nearby-places/leave"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(new JSONObject());

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                callback.onSDKActionSuccess(Action.LEAVE_CURRENT_PLACES);
            }

            @Override
            public void sessionTokenUpdated() {
                leaveCurrentPlaces(callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * @param placeIds      ['123', 'asd', 'cc']
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void checkInAtPlaces(final ArrayList<String> placeIds, final SuccessCallback callback, final ErrorCallback errorCallback) {
        JSONObjectRequest request;
        JSONObject body;

        body = new JSONObject();
        body.put("place_ids", new JSONArray(placeIds));

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/nearby-places/check-in"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(body);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                ArrayList<User> users;
                JSONArray usersNearbyAsJSONArray;

                users = new ArrayList<User>();
                usersNearbyAsJSONArray = response.getBody().getJSONArray("users_nearby");

                for (int i = 0; i < usersNearbyAsJSONArray.length(); i++) {
                    User user = User.fromJSONObject((JSONObject) usersNearbyAsJSONArray.get(i));
                    users.add(user);
                }

                callback.onSDKActionSuccess(Action.CHECK_IN_AT_PLACES, users);
            }

            @Override
            public void sessionTokenUpdated() {
                checkInAtPlaces(placeIds, callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * Confirm current user location.
     *
     * @param callback
     * @param errorCallback
     */
    public void confirmCheckIn(final SuccessCallback callback, final ErrorCallback errorCallback) {
        JSONObjectRequest request;

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/nearby-places/check-in/confirm"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(new JSONObject());

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                callback.onSDKActionSuccess(Action.CONFIRM_CHECK_IN);
            }

            @Override
            public void sessionTokenUpdated() {
                confirmCheckIn(callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * @param callback      in case of 2xx, 4xx or 5xx response from server
     * @param errorCallback in case of runtime error
     */
    public void getNearbyUsers(final SuccessCallback callback, final ErrorCallback errorCallback) {
        BodyLessRequest request;

        request = new BodyLessRequest(BodyLessRequest.Method.GET, this.castUrl("/nearby-places/users-nearby"));
        request.setHeader("X-Authorization", this.sessionToken);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                ArrayList<User> users;
                JSONArray usersNearbyAsJSONArray;

                users = new ArrayList<User>();
                usersNearbyAsJSONArray = response.getBody().getJSONArray("users_nearby");

                for (int i = 0; i < usersNearbyAsJSONArray.length(); i++) {
                    User user = User.fromJSONObject((JSONObject) usersNearbyAsJSONArray.get(i));
                    users.add(user);
                }

                callback.onSDKActionSuccess(Action.GET_NEARBY_USERS, users);
            }

            @Override
            public void sessionTokenUpdated() {
                getNearbyUsers(callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * @param user user to be liked
     * @param callback in case of success
     * @param errorCallback in case of http or runtime error
     */
    public void likeUser(final User user, final SuccessCallback callback, final ErrorCallback errorCallback) {
        JSONObjectRequest request;
        JSONObject body;

        body = new JSONObject();
        body.put("user_id", user.getId());

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/likes"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(body);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                Integer userId = response.getBody().getInt("user_id");

                callback.onSDKActionSuccess(Action.LIKE_USER, userId);
            }

            @Override
            public void sessionTokenUpdated() {
                likeUser(user, callback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * Put the user firebase cloud messaging token into the DB on the server.
     *
     * @param token
     * @param successCallback
     * @param errorCallback
     */
    public void addFCMToken(final String token, final SuccessCallback successCallback, final ErrorCallback errorCallback) {
        JSONObjectRequest request;
        JSONObject body;

        body = new JSONObject();
        body.put("token", token);

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/android-fcm-tokens"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(body);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                successCallback.onSDKActionSuccess(SDK.Action.ADD_FCM_TOKEN);
            }

            @Override
            public void sessionTokenUpdated() {
                addFCMToken(token, successCallback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * Check if the user has left the places using their current location.
     *
     * @param accuracy
     * @param latitude
     * @param longitude
     * @param successCallback
     * @param errorCallback
     */
    public void hasLeftPlacesNearby(
            final int accuracy,
            final long latitude,
            final long longitude,
            final SuccessCallback successCallback,
            final ErrorCallback errorCallback
    ) {
        BodyLessRequest request;

        request = new BodyLessRequest(BodyLessRequest.Method.GET, this.castUrl("/nearby-places/has-left"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.addField("accuracy", String.valueOf(accuracy));
        request.addField("latitude", String.valueOf(latitude));
        request.addField("longitude", String.valueOf(longitude));

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                Boolean left = response.getBody().getBoolean("left");

                successCallback.onSDKActionSuccess(Action.HAS_LEFT_PLACES, left);
            }

            @Override
            public void sessionTokenUpdated() {
                hasLeftPlacesNearby(accuracy, latitude, longitude, successCallback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });

    }

    /**
     *
     * @param location
     * @param locationAccuracy
     * @param networks
     * @param successCallback
     * @param errorCallback
     */
    public void pushWiFiNetworks(
            final Location location,
            final int locationAccuracy,
            final List<WiFiNetwork> networks,
            final SuccessCallback successCallback,
            final ErrorCallback errorCallback
    ) {
        JSONObjectRequest request;
        JSONObject body;
        JSONObject locationAsJSON;
        JSONArray networksAsJSONArray;
        JSONObject networkAsJSON;

        locationAsJSON = new JSONObject();
        locationAsJSON.put("latitude", location.getLatitude());
        locationAsJSON.put("longitude", location.getLongitude());
        locationAsJSON.put("accuracy", locationAccuracy);

        networksAsJSONArray = new JSONArray();

        for (int i = 0; i < networks.size(); i++) {
            networkAsJSON = new JSONObject();

            networkAsJSON.put("SSID", networks.get(i).getSSID());
            networkAsJSON.put("BSSID", networks.get(i).getBSSID());
            networkAsJSON.put("signal", networks.get(i).getSignal());

            networksAsJSONArray.put(networkAsJSON);
        }

        body = new JSONObject();
        body.put("location", locationAsJSON);
        body.put("networks", networksAsJSONArray);

        request = new JSONObjectRequest(JSONObjectRequest.Method.POST, this.castUrl("/wifi-networks"));
        request.setHeader("X-Authorization", this.sessionToken);
        request.setBody(body);

        this.postman.asJSONObjectAsync(request, new ListenerJSONObjectAdapter(this, errorCallback) {
            @Override
            public void handleSuccess(Response<JSONObject> response) {
                successCallback.onSDKActionSuccess(Action.PUSH_WIFI_NETWORKS);
            }

            @Override
            public void sessionTokenUpdated() {
                pushWiFiNetworks(location, locationAccuracy, networks, successCallback, errorCallback);
            }
        }, new Client.ErrorListener() {
            @Override
            public void exception(PostmanException e) {
                errorCallback.exception(e);
            }
        });
    }

    /**
     * Callback for successful response from server.
     */
    public interface SuccessCallback {
        public void onSDKActionSuccess(Action action, Object ...params);
    }

    /**
     * Callback for exceptions, bad responses from server, 40x/50x codes.
     */
    public interface ErrorCallback {
        public void exception(Throwable exception);
    }

    public interface SessionTokenRenewalCallback {
        public void renewed(Session session, RefreshToken refreshToken);
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
