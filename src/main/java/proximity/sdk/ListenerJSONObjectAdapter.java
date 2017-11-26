package proximity.sdk;

import org.json.JSONObject;
import postman.Client;
import postman.response.Response;
import proximity.sdk.exception.HttpException;
import proximity.sdk.exception.UnathenticatedErrorException;
import proximity.sdk.exception.ValidatorErrorException;

abstract public class ListenerJSONObjectAdapter implements Client.Listener<JSONObject> {
    private SDK.ErrorCallback errorCallback;

    ListenerJSONObjectAdapter(SDK.ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
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
     * User implementation of 2xx code
     *
     * @param response is a server response
     */
    abstract public void handleSuccess(Response<JSONObject> response);

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
