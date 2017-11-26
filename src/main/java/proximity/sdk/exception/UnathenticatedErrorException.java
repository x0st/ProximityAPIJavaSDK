package proximity.sdk.exception;

import postman.response.Response;

public class UnathenticatedErrorException extends HttpException {
    public UnathenticatedErrorException(String errorCode, Response response) {
        super(errorCode, response);
    }
}
