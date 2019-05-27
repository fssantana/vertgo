package tests;

import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.exception.HttpException;
import io.github.fssantana.vertgo.response.LambdaResponse;
import java.util.Collections;
import java.util.Map;

public class ExampleController extends Controller<InputExample, LambdaResponse<Map>> {

  @Override
  public String route() {
    return "GET:/users";
  }

  @Override
  public LambdaResponse<Map> handle(InputExample input) throws HttpException {
    LambdaResponse<Map> response = new LambdaResponse<>();
    response.setBody(Collections.singletonMap("teste", "testeadas"));
    HttpException httpException = new HttpException();

    httpException.setHeaders(Collections.singletonMap("aaa", "112312"));
    httpException.setStatusCode(400);
    httpException.setResponseBody(Collections.singletonMap("teste", "testeadas"));

    throw httpException;
  }

}
