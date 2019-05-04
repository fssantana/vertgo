package io.github.fssantana.vertgo.response;

import java.util.HashMap;
import java.util.Map;

public class LambdaResponse<T> {

    private T body;
    private int statusCode = 200;
    private boolean isBase64 = false;
    private Map<String, String> headers = new HashMap<>();


    public static LambdaResponse of(Object body, int statusCode, boolean isBase64, Map<String, String> headers){
        LambdaResponse response = new LambdaResponse();
        response.setBody(body);
        response.setStatusCode(statusCode);
        response.setBase64(isBase64);
        response.setHeaders(headers);
        return response;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isBase64() {
        return isBase64;
    }

    public void setBase64(boolean base64) {
        isBase64 = base64;
    }
}
