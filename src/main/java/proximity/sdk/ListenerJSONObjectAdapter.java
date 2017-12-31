package proximity.sdk;

import org.json.JSONObject;
import postman.Client;
import postman.response.Response;
import proximity.sdk.entity.RefreshToken;
import proximity.sdk.entity.Session;
import proximity.sdk.exception.HttpException;
import proximity.sdk.exception.UnathenticatedErrorException;
import proximity.sdk.exception.ValidatorErrorException;

abstract public class ListenerJSONObjectAdapter implements Client.Listener<JSONObject> {
    private SDK.ErrorCallback errorCallback;
    private SDK sdk;
    private Boolean tryUpdatingSessionToken = true;

    /**
     * @param sdk {@link SDK}
     * @param errorCallback {@link SDK.ErrorCallback}
     */
    ListenerJSONObjectAdapter(SDK sdk, SDK.ErrorCallback errorCallback) {
        this.sdk = sdk;
        this.errorCallback = errorCallback;
    }

    /**
     * @param tryUpdatingSessionToken whether the listener should try to request a new session token
     */
    public void setTryUpdatingSessionToken(Boolean tryUpdatingSessionToken) {
        this.tryUpdatingSessionToken = tryUpdatingSessionToken;
    }

    /**
     * @param response is a server response
     * @return whether server response is the validation error
     */
    private boolean isValidationError(Response<JSONObject> response) {
        return response.getBody().getString("error").equals("VALIDATION_ERROR");
    }

    /**
     * @param response is a server response
     * @return whether server response is the unauthenticated error
     */
    private boolean isUnauthenticatedError(Response<JSONObject> response) {
        return response.getBody().getString("error").equals("UNAUTHENTICATED");
    }

    /**
     * 2xx code
     *
     * @param response is a server response
     */
    @Override
    public void success(Response<JSONObject> response) {
        try {
            this.handleSuccess(response);
        } catch (Throwable exception) {
            this.errorCallback.runtime(exception);
        }
    }

    /**
     * Client implementation of 2xx code
     *
     * @param response is a server response
     */
    abstract public void handleSuccess(Response<JSONObject> response);

    /**
     * Requests a new session token with a refresh token
     */
    private void requestNewSessionToken() {
        sdk.renewSessionToken(sdk.getRefreshToken(), new SDK.SessionTokenRenewalCallback() {
            @Override
            public void renewed(Session session, RefreshToken refreshToken) {
                sdk.setRefreshToken(refreshToken.getToken());
                sdk.setSessionToken(session.getToken());

                sdk.getOnTokenUpdateCallback().renewed(session, refreshToken);

                sessionTokenUpdated();
            }
        }, new SDK.ErrorCallback() {
            @Override
            public void http(HttpException exception) {
                errorCallback.http(exception);
            }

            @Override
            public void runtime(Throwable exception) {
                errorCallback.runtime(exception);
            }
        });
    }

    /**
     * Invoked after an automatic session token renewal
     */
    public void sessionTokenUpdated() {

    }

    /**
     * 4xx or 5xx
     *
     * @param response is a server response
     */
    @Override
    public void error(Response<JSONObject> response) {
        try {
            String errorCode = response.getBody().getString("error");
            HttpException exception;

            if (isValidationError(response)) {
                exception = new ValidatorErrorException(errorCode, response);
            } else if (isUnauthenticatedError(response)) {
                if (sdk.shouldUpdateTokenAutomatically() && tryUpdatingSessionToken) {
                    this.requestNewSessionToken();
                    return;
                }

                exception = new UnathenticatedErrorException(errorCode, response);
            } else {
                exception = new HttpException(errorCode, response);
            }

            errorCallback.http(exception);
        } catch (Throwable e) {
            errorCallback.runtime(e);
        }
    }
}
