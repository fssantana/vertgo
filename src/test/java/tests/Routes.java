package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.VertgoHandler;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.*;

public class Routes extends VertgoHandler {

    @Override
    protected List<Controller> router() {
        return Arrays.asList(
                new ExampleController()
        );
    }

    @Override
    protected Map<String, String> addHeaders(){
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("aaa", "bbb");
        return objectObjectHashMap;
    }

    public static void  main(String[] args) throws IOException {
        Routes routes = new Routes();
        Map<String, String> queryStringParameters = null;
        Map<String, String> pathParameters = null;
        Map<String, String> headers = null;
        Map<String, Object> body = new HashMap<>();

        body.put("pet", "foo");

        Map<String, Object> input = new HashMap<>();
        input.put("httpMethod", "GET");
        input.put("resource", "/users");
        input.put("queryStringParameters", queryStringParameters);
        input.put("pathParameters", pathParameters);
        input.put("headers", headers);
        input.put("body", new ObjectMapper().writeValueAsString(body));

        Map<String, Object> resp = routes.handleRequest(input, null);
        System.out.println(resp);
    }

}