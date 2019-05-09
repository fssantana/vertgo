## Vertgo
---
[![Build Status](https://travis-ci.org/fssantana/vertgo.svg?branch=master)](https://travis-ci.org/fssantana/vertgo)

A tool to ease Lambda Functions development for Applications using Api Gateway

### About Vertgo
---
Vertgo is a microframework that provides tools to make 
it easier to develop api's using the AWS Lambda Function and API Gateway. 
Lambda Forest attempts to make the development faster by providing a way to create a single Lambda
Function to serve multiple endpoints. That is achieved using [Vert.x](https://vertx.io/).

Some tools:
* Routing
* Serializations for inputs and responses
* Custom configurations

How to use:
* Each controller will have one route associated

```java
public class ExampleController extends Controller<HashMap, LambdaResponse<Map>> {

    /**
    *  route {HTTP_METHOD}:{PATH} 
    */
    @Override
    public String route() {
        return "GET:/users";
    }

    /**
    * Service execution override handle method
    * This method can return any bean or a LambdaResponse<O> instance.
    * If a LambdaResponse instance is returned, you can set status code and headers;
    * If another instance is returned, default status code (200) and empty headers will be returned
    **/
    @Override
    public LambdaResponse<Map> handle(HashMap input) {
        LambdaResponse<Map> response = new LambdaResponse<>();
        response.setBody(Collections.singletonMap("teste", "testeadas"));
        return response;
    }
}
```

* Create a Route class wich needs to contains all Controllers you want to use
```java
public class Routes extends VertgoHandler {

    /**
    * All controllers should be here 
    */
    @Override
    protected List<Controller> router() {
        return Arrays.asList(
                new ExampleController()
        );
    }

}
```

}