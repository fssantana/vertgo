package tests;

import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.response.LambdaResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExampleController extends Controller<HashMap, LambdaResponse<Map>> {

    @Override
    public String route() {
        return "GET:/users";
    }

    @Override
    public LambdaResponse<Map> handle(HashMap input) {
        LambdaResponse<Map> response = new LambdaResponse<>();
        response.setBody(Collections.singletonMap("teste", "testeadas"));
        return response;
    }

}
