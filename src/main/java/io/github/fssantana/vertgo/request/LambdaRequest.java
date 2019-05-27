package io.github.fssantana.vertgo.request;

import io.vertx.core.json.JsonObject;
import java.util.Optional;

/**
 * Request converted from lambda income
 */
public class LambdaRequest {

  private JsonObject request;
  private JsonObject headers;
  private JsonObject pathParams;

  public Optional<String> header(String key) {
    return Optional.ofNullable(getHeaders().getString(key, null));
  }

  public Optional<String> path(String key) {
    return Optional.ofNullable(getPathParams().getString(key, null));
  }

  public JsonObject getRequest() {
    return request;
  }

  public void setRequest(JsonObject request) {
    this.request = request;
  }

  public JsonObject getHeaders() {
    return headers;
  }

  public void setHeaders(JsonObject headers) {
    this.headers = headers;
  }

  public JsonObject getPathParams() {
    return pathParams;
  }

  public void setPathParams(JsonObject pathParams) {
    this.pathParams = pathParams;
  }
}
