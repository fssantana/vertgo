package io.github.fssantana.vertgo;

import io.github.fssantana.vertgo.exception.HttpException;
import io.github.fssantana.vertgo.request.LambdaRequest;
import io.github.fssantana.vertgo.response.LambdaResponse;
import com.google.common.reflect.TypeToken;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.util.Optional;

/**
 * @author fsantana
 *
 * Controller class which should be extended
 *
 * @since 0.1.0
 * @param <I>
 * @param <O>
 */
public abstract class Controller<I, O> {

    private Class<I> inputType;
    private Class<O> outputType;

    public Controller() {
        TypeToken<I> inputTypeToken = new TypeToken<I>(getClass()) { };
        inputType = (Class<I>) inputTypeToken.getRawType();

        TypeToken<O> outputTypeToken = new TypeToken<O>(getClass()) { };
        outputType = (Class<O>) outputTypeToken.getRawType();

    }

    private LambdaRequest lambdaRequest;

    /**
     *
     * Execute method will be called when lambda is invoked
     *
     * @since 0.1.0
     * @param event
     */
    void execute(Message<LambdaRequest> event){
        lambdaRequest = event.body();

        try{
            I input = null;
            if(inputType != Void.class && event.body().getRequest() != null){
                input = event.body().getRequest().mapTo(inputType);
            }
            O response = this.handle(input);
            if(response != null && response instanceof LambdaResponse){
                event.reply(response);
            }else{
                event.reply(JsonObject.mapFrom(response));
            }
        } catch (HttpException e) {
            LambdaResponse<Object> errorResponse = new LambdaResponse<>();
            event.reply(e);
        } catch (Exception e){
            e.printStackTrace();
            event.fail(500, e.getMessage());
        }

    }

    /**
     * Return a header entry
     * @param key
     *
     */
    public Optional<String> getHeader(String key){
        if(lambdaRequest.getHeaders() == null || lambdaRequest.getHeaders().isEmpty()){
            return Optional.empty();
        }
        return lambdaRequest.header(key);
    }

    /**
     * Return a path parameter value
     *
     * @param key
     */
    public Optional<String> getPath(String key){
        if(lambdaRequest.getPathParams() == null || lambdaRequest.getPathParams().isEmpty()){
            return Optional.empty();
        }
        return lambdaRequest.path(key);
    }

    /**
     * Should be implemented and return a string like {HTTP_METHOD}:{PATH}
     * @return
     */
    public abstract String route();

    /**
     *
     * Should be implemented with controller logic
     *
     * @param input
     * @return
     */
    public abstract O handle(I input) throws HttpException;
}
