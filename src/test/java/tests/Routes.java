package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.VertgoHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Routes extends VertgoHandler {

    @Override
    protected List<Controller> router() {
        return Arrays.asList(
                new ExampleController()
        );
    }

    public static void  main(String[] args) throws IOException {
        Routes routes = new Routes();
        String json = "{\"httpMethod\": \"GET\", \"resource\": \"/users\", \"body\": \"{}\"}";
        Map<String, Object> income = new ObjectMapper().readValue(json, Map.class);

        Map<String, Object> stringObjectMap = routes.handleRequest(income, null);
        System.out.println(stringObjectMap);
    }

}