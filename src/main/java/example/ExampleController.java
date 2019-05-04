package example;

import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.response.LambdaResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExampleController extends Controller<HashMap, LambdaResponse<Map>> {

    @Override
    public String route() {
        return "GET:/users";
    }

    @Override
    public LambdaResponse<Map> handle(HashMap input) {

        Optional<String> accept = getHeader("Accepts");
        Optional<String> proxy = getPath("proxy");


            LambdaResponse<Map> response = new LambdaResponse<>();
        response.setBody(Collections.singletonMap("teste", "testeadas"));
        throw new RuntimeException("");
//        return response;
    }

}
