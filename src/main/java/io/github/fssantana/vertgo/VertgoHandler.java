package io.github.fssantana.vertgo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.github.fssantana.vertgo.exception.HttpException;
import io.github.fssantana.vertgo.request.HttpExceptionMessageCodec;
import io.github.fssantana.vertgo.request.LambdaRequest;
import io.github.fssantana.vertgo.request.LambdaRequestMessageCodec;
import io.github.fssantana.vertgo.request.UnrecognizedPropertyExceptionCodec;
import io.github.fssantana.vertgo.response.LambdaResponse;
import io.github.fssantana.vertgo.response.LambdaResponseMessageCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;

/**
 * @author fsantana
 * @since 0.1.0
 *
 * handler that receives lambda request
 */
public abstract class VertgoHandler implements
    RequestHandler<Map<String, Object>, Map<String, Object>> {

  private static final Logger LOGGER = Logger.getLogger(VertgoHandler.class);
  private static final String HEADERS = "headers";
  private static final String PATH_PARAMETERS = "pathParameters";
  private static final String QUERY_STRING_PARAMETERS = "queryStringParameters";
  private static final String BODY = "body";
  private static final String HTTP_METHOD = "httpMethod";
  private static final String RESOURCE = "resource";
  private static final String IS_BASE_64 = "isBase64Encoded";
  private static final String STATUS_CODE = "statusCode";
  private static final int S_500 = 500;
  private static final int S_200 = 200;
  private final HttpExceptionMessageCodec exceptionCodec = new HttpExceptionMessageCodec();
  private final LambdaRequestMessageCodec requestCodec = new LambdaRequestMessageCodec();
  private final LambdaResponseMessageCodec responseCodec = new LambdaResponseMessageCodec();
  private final UnrecognizedPropertyExceptionCodec propertyExceptionCodec = new UnrecognizedPropertyExceptionCodec();

  protected final Vertx vertxInstance = getVertxInstance();

  /**
   * Returns the endpoint
   */
  private static String address(JsonObject input) {
    return ""
        .concat(
            String.valueOf(input.getString(HTTP_METHOD, ""))
        )
        .concat(
            ":"
        )
        .concat(
            String.valueOf(input.getString(RESOURCE, ""))
        );
  }

  /**
   * Handle lambda request calling a Controller based in adress method
   */
  @Override
  public Map<String, Object> handleRequest(final Map<String, Object> income, Context context) {
    JsonObject input = JsonObject.mapFrom(income);
    LOGGER.debug(String.format("Starting request handler :: %s", input.toString()));
    final CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
    final String eventBusAddress = address(input);

    LOGGER.debug(String.format("income service %s", eventBusAddress));
    LambdaRequest request = buildRequest(input);
    DeliveryOptions options = new DeliveryOptions()
        .setCodecName(requestCodec.name());

    vertxInstance.exceptionHandler(e -> {
      LOGGER.debug("Unexpected error", e);
    });

    vertxInstance
        .eventBus()
        .send(eventBusAddress, request, options, asyncResult ->
        {
          if (asyncResult.succeeded()) {
            future.complete(onSuccess(asyncResult));
          } else {
            future.complete(onError(asyncResult));
          }
        });

    try {
      return future.get(getTimeout(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.debug("Interrupted error", e);
    } catch (ExecutionException e) {
      LOGGER.debug("Execution error", e);
    } catch (TimeoutException e) {
      LOGGER.debug("Thread Timeout", e);
      future.cancel(true);
    }
    return null;
  }

  /**
   * Get lambda request and transforms it into a LambdaRequest instance
   */
  private LambdaRequest buildRequest(JsonObject input) {
    LambdaRequest request = new LambdaRequest();

    String body = input.getString(BODY, null);
    if (body != null) {
      try {
        request.setRequest(new JsonObject(body));
      } catch (DecodeException e) {
        LOGGER.debug("Decode exception - invalid input", e);
        return null;
      }
    } else if (input.getJsonObject(QUERY_STRING_PARAMETERS) != null) {
      request.setRequest(input.getJsonObject(QUERY_STRING_PARAMETERS));
    } else {
      request.setRequest(null);
    }

    if (input.getJsonObject(PATH_PARAMETERS) != null) {
      request.setPathParams(input.getJsonObject(PATH_PARAMETERS));
    } else {
      request.setPathParams(null);
    }

    if (input.getJsonObject(HEADERS) != null) {
      request.setHeaders(input.getJsonObject(HEADERS));
    } else {
      request.setHeaders(null);
    }

    return request;
  }

  /**
   * On unexpected error handler
   */
  private Map<String, Object> onError(AsyncResult<Message<Object>> asyncResult) {
    Map<String, Object> response = new HashMap<>();

    LOGGER.debug("Unexpected error, returning 500");

    response.put(BODY, null);
    response.put(IS_BASE_64, false);
    response.put(HEADERS, Collections.emptyMap());
    response.put(STATUS_CODE, S_500);

    return response;
  }

  /**
   * On success (should always execute this)
   */
  private Map<String, Object> onSuccess(AsyncResult<Message<Object>> asyncResult) {
    LOGGER.debug("Building success response");
    Object body = asyncResult.result().body();
    Map<String, Object> response = new HashMap<>();

    if (body == null) {
      response.put(BODY, null);
      response.put(IS_BASE_64, false);
      response.put(HEADERS, addCustomHeaders(new HashMap<>()));
      response.put(STATUS_CODE, S_200);
    } else if (body instanceof LambdaResponse) {
      LambdaResponse lambdaResponse = (LambdaResponse) body;
      response.put(BODY, Json.encode(lambdaResponse.getBody()));
      response.put(IS_BASE_64, lambdaResponse.isBase64());
      response.put(HEADERS, addCustomHeaders(
          lambdaResponse.getHeaders() != null ? lambdaResponse.getHeaders() : new HashMap<>()));
      response.put(STATUS_CODE, lambdaResponse.getStatusCode());
    } else if (body instanceof HttpException) {
      HttpException lambdaResponse = (HttpException) body;
      response.put(BODY, Json.encode(lambdaResponse.getResponseBody()));
      response.put(IS_BASE_64, false);
      response.put(HEADERS, addCustomHeaders(
          lambdaResponse.getHeaders() != null ? lambdaResponse.getHeaders() : new HashMap<>()));
      response.put(STATUS_CODE, lambdaResponse.getStatusCode());
    } else if (body instanceof UnrecognizedPropertyException) {
      response = buildParseException((UnrecognizedPropertyException) body);
    } else {
      response.put(BODY, Json.encode(body));
      response.put(IS_BASE_64, false);
      response.put(HEADERS, addCustomHeaders(new HashMap<>()));
      response.put(STATUS_CODE, S_200);
    }

    LOGGER.debug(String.format("%s", response));
    return response;
  }

  /**
   * Build response error for unrecognized property
   */
  private Map<String, Object> buildParseException(UnrecognizedPropertyException error) {
    Map<String, Object> response = new HashMap<>();
    LambdaResponse<?> lambdaResponse = unrecognizedPropertyError(error.getPropertyName(),
        error.getMessage());

    response.put(BODY, Json.encode(lambdaResponse.getBody()));
    response.put(IS_BASE_64, false);
    response.put(HEADERS, addCustomHeaders(
        lambdaResponse.getHeaders() != null ? lambdaResponse.getHeaders() : new HashMap<>()));
    response.put(STATUS_CODE, lambdaResponse.getStatusCode());

    return response;
  }

  /**
   * This method return default error response for json with unrecognized property Override this
   * method to set a default error response for unrecognized property
   */
  public LambdaResponse<? extends Object> unrecognizedPropertyError(String path, String message) {
    Map<String, String> map = new HashMap<>();
    map.put("message", "Invalid JSON property");
    map.put("field", path);

    LambdaResponse<Map> lambdaResponse = new LambdaResponse<>();
    lambdaResponse.setHeaders(new HashMap<>());
    lambdaResponse.setStatusCode(400);
    lambdaResponse.setBase64(false);
    lambdaResponse.setBody(map);

    return lambdaResponse;
  }

  /**
   * Start vertx instance
   */
  private final Vertx getVertxInstance() {
    LOGGER.debug("Creating Vertx instance");
    System.setProperty("vertx.disableFileCPResolving", "true");
    final Vertx vertx = Vertx.vertx(new VertxOptions()
        .setMaxEventLoopExecuteTime(40000L)
        .setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS)
    );

    vertx.deployVerticle(deployables());

    vertx
        .eventBus()
        .registerDefaultCodec(LambdaRequest.class, requestCodec)
        .registerDefaultCodec(LambdaResponse.class, responseCodec)
        .registerDefaultCodec(HttpException.class, exceptionCodec)
        .registerDefaultCodec(UnrecognizedPropertyException.class, propertyExceptionCodec);

    return vertx;
  }

  /**
   * Get all controllers and create vertx bus
   */
  private AbstractVerticle deployables() {
    LOGGER.debug("Deploying verticles");
    List<Controller> router = router();
    if (router == null || router.isEmpty()) {
      return null;
    }

    return new AbstractVerticle() {
      @Override
      public void start(Future<Void> future) throws Exception {
        final EventBus eventBus = getVertx().eventBus();
        router.forEach((Controller r) -> {
          r.setFilter(filter());
          eventBus.consumer(r.route(), r::execute);
        });
      }
    };
  }

  /**
   * Override this to execute before all controllers
   */
  protected ControllerFilter filter() {
    return new ControllerFilter() {
      @Override
      public void apply(LambdaRequest request) throws HttpException {
        LOGGER.debug("No filter implemented");
      }
    };
  }

  protected abstract List<Controller> router();

  /**
   * Override this method to set an application timeout
   */
  public int timeout() {
    return 60;
  }

  private int getTimeout() {
    return timeout();
  }

  protected Map<String, String> addHeaders() {
    return null;
  }

  private Map<String, String> addCustomHeaders(Map<String, String> headers) {
    Map<String, String> customHeaders = addHeaders();

    if (customHeaders == null || customHeaders.isEmpty()) {
      return headers;
    }

    HashMap<String, String> finalHeaders = new HashMap<>();
    finalHeaders.putAll(headers);

    for (Map.Entry<String, String> e : customHeaders.entrySet()) {
      if (!finalHeaders.containsKey(e.getKey())) {
        finalHeaders.put(e.getKey(), e.getValue());
      }
    }

    return finalHeaders;
  }

}
