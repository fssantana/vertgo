package io.github.fssantana.vertgo;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.common.reflect.TypeToken;
import io.github.fssantana.vertgo.exception.HttpException;
import io.github.fssantana.vertgo.request.LambdaRequest;
import io.github.fssantana.vertgo.response.LambdaResponse;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import org.apache.log4j.Logger;

/**
 * @author fsantana
 *
 * Controller class which should be extended
 * @since 0.1.0
 */
public abstract class Controller<I, O> {

  private static final Logger LOGGER = Logger.getLogger(VertgoHandler.class);
  private Class<I> inputType;
  private Class<O> outputType;
  private LambdaRequest lambdaRequest;
  private ControllerFilter filter;

  public Controller() {
    TypeToken<I> inputTypeToken = new TypeToken<I>(getClass()) {
    };
    inputType = (Class<I>) inputTypeToken.getRawType();

    TypeToken<O> outputTypeToken = new TypeToken<O>(getClass()) {
    };
    outputType = (Class<O>) outputTypeToken.getRawType();
  }

  /**
   * Execute method will be called when lambda is invoked
   *
   * @since 0.1.0
   */
  void execute(Message<LambdaRequest> event) {
    lambdaRequest = event.body();

    try {
      I input = null;
      if (inputType != Void.class && event.body().getRequest() != null) {
        input = event.body().getRequest().mapTo(inputType);
      } else if (inputType != Void.class && this.shouldCreateInstanceIfNull()) {
        input = inputType.newInstance();
      }

      applyFilter(event.body());
      before();
      O response = this.handle(input);

      if (response != null && response instanceof LambdaResponse) {
        event.reply(response);
      } else {
        event.reply(JsonObject.mapFrom(response));
      }
    } catch (HttpException e) {
      event.reply(e);
    } catch (Exception e) {
      if (e.getCause() != null && e.getCause() instanceof UnrecognizedPropertyException) {
        LOGGER.error("JSON parse error", e);
        UnrecognizedPropertyException cause = (UnrecognizedPropertyException) e.getCause();
        event.reply(cause);
      } else {
        LOGGER.error(e);
        event.fail(500, e.getMessage());
      }
    }
  }

  /**
   * filter function setter
   */
  protected void setFilter(ControllerFilter filter) {
    this.filter = filter;
  }

  /**
   * Apply filter before all handle
   */
  protected void applyFilter(LambdaRequest request) throws HttpException {
    filter.apply(request);
  }


  /**
   * Returns AWS Lambda Context
   */
  protected String getRawBody() {
    if (lambdaRequest == null || lambdaRequest.getRequest() == null) {
      return null;
    }
    return lambdaRequest.getRequest().toString();
  }

  /**
   * This filter function is always executed in all controllers
   * it should be Override in VertgoHandler
   */

  /**
   * Override this method to execute before controller handle
   */
  protected void before() throws HttpException {
  }

  /**
   * Return a header entry
   */
  public Optional<String> getHeader(String key) {
    if (lambdaRequest.getHeaders() == null || lambdaRequest.getHeaders().isEmpty()) {
      return Optional.empty();
    }
    return lambdaRequest.header(key);
  }

  /**
   * Return a path parameter value
   */
  public Optional<String> getPath(String key) {
    if (lambdaRequest.getPathParams() == null || lambdaRequest.getPathParams().isEmpty()) {
      return Optional.empty();
    }
    return lambdaRequest.path(key);
  }

  /**
   * Should be implemented and return a string like {HTTP_METHOD}:{PATH}
   */
  public abstract String route();

  /**
   * Should be implemented with controller logic
   */
  public abstract O handle(I input) throws HttpException;

  /**
   * If this method returns true it will always create an instance from input type even if request
   * body is empty
   *
   * Default is true. Override this to change its behavior
   */
  public boolean shouldCreateInstanceIfNull() {
    return true;
  }
}
