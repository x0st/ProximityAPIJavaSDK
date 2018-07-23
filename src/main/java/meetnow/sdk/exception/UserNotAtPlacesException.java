package meetnow.sdk.exception;

import postman.response.Response;

public class UserNotAtPlacesException extends HttpException {
    public UserNotAtPlacesException(String errorCode, Response response) {
        super(errorCode, response);
    }
}
