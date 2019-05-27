package io.github.fssantana.vertgo;

import io.github.fssantana.vertgo.exception.HttpException;
import io.github.fssantana.vertgo.request.LambdaRequest;

@FunctionalInterface
public interface ControllerFilter {

  void apply(LambdaRequest request) throws HttpException;

}
