package io.github.fssantana.vertgo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.github.fssantana.vertgo.exception.HttpException;
import io.github.fssantana.vertgo.request.HttpExceptionMessageCodec;
import io.github.fssantana.vertgo.request.LambdaRequest;
import io.github.fssantana.vertgo.request.LambdaRequestMessageCodec;
import io.github.fssantana.vertgo.response.LambdaResponse;
import io.github.fssantana.vertgo.response.LambdaResponseMessageCodec;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
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

/**
 * @author fsantana
 * @since 0.1.0
 *
 * handler that receives lambda request
 *
 */
public abstract class VertgoHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String HEADERS =                    "headers";
    private static final String PATH_PARAMETERS =            "pathParameters";
    private static final String QUERY_STRING_PARAMETERS =    "queryStringParameters";
    private static final String BODY =                       "body";
    private static final String HTTP_METHOD =                "httpMethod";
    private static final String RESOURCE =                   "resource";
    private static final String IS_BASE_64 =                 "isBase64";
    private static final String STATUS_CODE =                "statusCode";
    private static final int S_500 =                         500;
    private static final int S_200 =                         200;

    private final HttpExceptionMessageCodec exceptionCodec = new HttpExceptionMessageCodec();
    private final LambdaRequestMessageCodec requestCodec =   new LambdaRequestMessageCodec();
    private final LambdaResponseMessageCodec responseCodec = new LambdaResponseMessageCodec();

    protected final Vertx vertxInstance =                    getVertxInstance();

    /**
     *
     * Returns the endpoint
     *
     * @param input
     * @return
     */
    private static String address(JsonObject input){
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
     *
     * @param income
     * @param context
     * @return
     */
    @Override
    public Map<String, Object> handleRequest(final Map<String, Object> income, Context context) {
        JsonObject input = JsonObject.mapFrom(income);
        final CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        final String eventBusAddress = address(input);

        LambdaRequest request = buildRequest(input);
        DeliveryOptions options = new DeliveryOptions()
                .setCodecName(requestCodec.name());

        vertxInstance.exceptionHandler(e -> {
            e.printStackTrace();
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
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get lambda request and transforms it into a LambdaRequest instance
     * @param input
     * @return
     */
    private LambdaRequest buildRequest(JsonObject input) {
        LambdaRequest request = new LambdaRequest();

        String body = input.getString(BODY, null);
        if(body != null){
            request.setRequest(new JsonObject(body));
        }else if(input.getJsonObject(QUERY_STRING_PARAMETERS) != null){
            request.setRequest(input.getJsonObject(QUERY_STRING_PARAMETERS));
        }

        if(input.getJsonObject(PATH_PARAMETERS) != null){
            request.setPathParams(input.getJsonObject(PATH_PARAMETERS));
        }

        if(input.getJsonObject(HEADERS) != null){
            request.setHeaders(input.getJsonObject(HEADERS));
        }
        return request;
    }

    /**
     * On unexpected error handler
     *
     * @param asyncResult
     * @return
     */
    private Map<String, Object> onError(AsyncResult<Message<Object>> asyncResult) {
        Map<String, Object> response = new HashMap<>();

        response.put(BODY, null);
        response.put(IS_BASE_64, false);
        response.put(HEADERS, Collections.emptyMap());
        response.put(STATUS_CODE, S_500);

        return response;
    }

    /**
     * On success (should always execute this)
     *
     * @param asyncResult
     * @return
     */
    private Map<String, Object> onSuccess(AsyncResult<Message<Object>> asyncResult) {
        Object body = asyncResult.result().body();
        Map<String, Object> response = new HashMap<>();

        if (body == null){
            response.put(BODY, null);
            response.put(IS_BASE_64, false);
            response.put(HEADERS, Collections.emptyMap());
            response.put(STATUS_CODE, S_200);
        } else if(body instanceof LambdaResponse){
            LambdaResponse lambdaResponse = (LambdaResponse) body;
            response.put(BODY, Json.encode(lambdaResponse.getBody()));
            response.put(IS_BASE_64, lambdaResponse.isBase64());
            response.put(HEADERS, lambdaResponse.getHeaders() != null ? lambdaResponse.getHeaders() : null);
            response.put(STATUS_CODE, lambdaResponse.getStatusCode());
        }else if(body instanceof HttpException){
            HttpException lambdaResponse = (HttpException) body;
            response.put(BODY, Json.encode(lambdaResponse.getResponseBody()));
            response.put(IS_BASE_64, false);
            response.put(HEADERS, lambdaResponse.getHeaders() != null ? lambdaResponse.getHeaders() : null);
            response.put(STATUS_CODE, lambdaResponse.getStatusCode());
        }else{
            response.put(BODY, Json.encode(body));
            response.put(IS_BASE_64, false);
            response.put(HEADERS, Collections.emptyMap());
            response.put(STATUS_CODE, S_200);
        }

        return response;
    }

    /**
     * Start vertx instance
     * @return
     */
    private final Vertx getVertxInstance() {
        System.setProperty("vertx.disableFileCPResolving", "true");
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(deployables());

        vertx
                .eventBus()
                .registerDefaultCodec(LambdaRequest.class, requestCodec)
                .registerDefaultCodec(LambdaResponse.class, responseCodec)
                .registerDefaultCodec(HttpException.class, exceptionCodec);

        return vertx;
    }

    /**
     *
     * Get all controllers and create vertx bus
     * @return
     */
    private AbstractVerticle deployables(){
        List<Controller> router = router();
        if(router == null || router.isEmpty()){
            return null;
        }

        return new AbstractVerticle() {
            @Override
            public void start(Future<Void> future) throws Exception {
                final EventBus eventBus = getVertx().eventBus();
                router.forEach(r -> eventBus.consumer(r.route(), r::execute));
            }
        };
    }

    protected abstract List<Controller> router();

}
