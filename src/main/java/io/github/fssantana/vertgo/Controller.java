package io.github.fssantana.vertgo;

import io.github.fssantana.vertgo.request.LambdaRequest;
import io.github.fssantana.vertgo.response.LambdaResponse;
import com.google.common.reflect.TypeToken;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.util.Optional;

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

    void execute(Message<LambdaRequest> event){
        lambdaRequest = event.body();

        try{
            O response = this.handle(event.body().getRequest().mapTo(inputType));
            if(response != null && response instanceof LambdaResponse){
                event.reply(response);
            }else{
                event.reply(JsonObject.mapFrom(response));
            }
        }catch (Exception e){
            e.printStackTrace();
            event.fail(500, e.getMessage());
        }

    }

    public Optional<String> getHeader(String key){
        return lambdaRequest.header(key);
    }
    public Optional<String> getPath(String key){
        return lambdaRequest.path(key);
    }

    public abstract String route();
    public abstract O handle(I input);
}
