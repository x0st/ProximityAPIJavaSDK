package meetnow.sdk.exception;

import postman.response.Response;

public class NoPlacesOfIntersectionException extends HttpException {
    public NoPlacesOfIntersectionException(String errorCode, Response response) {
        super(errorCode, response);
    }
}
