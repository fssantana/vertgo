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
* Controller input will be body OR query string parameters (never both)
* Any bean can be used as input type

How to use:
* Each controller will have one route associated
* Caught exceptions will be returned as 500 status code, EXCEPT Vertgo HttpException

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
    * 
    **/
    @Override
    public LambdaResponse<Map> handle(HashMap input) throws io.github.fssantana.vertgo.exception.HttpException{
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

If you want to set a default header value that will go in every response, just override the Route method like this:
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
    
    /**
    * These headers will be add to every response.
    * Preference is given for LambdaResponse header value in case of conflict
    */
    @Override
    protected Map<String, String> addHeaders(){
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("aaa", "bbb");
        return objectObjectHashMap;
    }

}
```

Override filter method to execute before all controllers handlers
```java
public class Routes extends VertgoHandler {
 //...
  protected ControllerFilter filter() {
      return new ControllerFilter() {
        @Override
        public void apply(LambdaRequest request) throws HttpException {
          //Your code here
        }
      };
    }
}
```


Override filter method to execute before all controllers handlers
```java
public class MyController extends Controller {
 //...
   @Override
    protected void before(){
      //getRawBody();
      //Your code
    }
}
```


### Add as Maven dependency
* Add as maven dependency
```xml
<dependency>
  <groupId>io.github.fssantana</groupId>
  <artifactId>vertgo</artifactId>
  <version>1.4.0</version>
</dependency>
```