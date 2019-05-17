package io.github.fssantana.vertgo.request;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.github.fssantana.vertgo.exception.HttpException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Vertx message codec
 */
public class UnrecognizedPropertyExceptionCodec implements MessageCodec<UnrecognizedPropertyException, UnrecognizedPropertyException> {

  @Override
  public void encodeToWire(Buffer buffer, UnrecognizedPropertyException exception) {
    JsonObject jsonToEncode = JsonObject.mapFrom(exception);

    String jsonToStr = jsonToEncode.encode();

    int length = jsonToStr.getBytes().length;

    buffer.appendInt(length);
    buffer.appendString(jsonToStr);
  }

  @Override
  public UnrecognizedPropertyException decodeFromWire(int position, Buffer buffer) {
    int _pos = position;

    int length = buffer.getInt(_pos);

    String jsonStr = buffer.getString(_pos+=4, _pos+=length);
    JsonObject contentJson = new JsonObject(jsonStr);
    return contentJson.mapTo(UnrecognizedPropertyException.class);
  }

  @Override
  public UnrecognizedPropertyException transform(UnrecognizedPropertyException customMessage) {
    return customMessage;
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}