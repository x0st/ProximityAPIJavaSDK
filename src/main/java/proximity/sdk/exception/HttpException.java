package proximity.sdk.exception;

import postman.response.Response;

public class HttpException extends java.lang.Exception implements ExceptionInterface {
    private String errorCode;
    private Response response;

    public HttpException(String errorCode, Response response) {
        super(String.format(
                "Proximity API has returned unexpected response code: %s", response.getStatusCode()
                ));

        this.errorCode = errorCode;
        this.response = response;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Response getResponse() {
        return response;
    }
}
