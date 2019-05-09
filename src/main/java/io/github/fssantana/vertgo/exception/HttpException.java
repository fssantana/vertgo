package io.github.fssantana.vertgo.exception;

import java.util.Map;

/**
 * Exception that should be thrown in Controller for http errors
 *
 */
public class HttpException extends Throwable {

    private int statusCode;
    private Object responseBody;
    private Map<String, String> headers;


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
