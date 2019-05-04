package io.github.fssantana.vertgo.response;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public class LambdaResponseMessageCodec implements MessageCodec<LambdaResponse, LambdaResponse> {

  @Override
  public void encodeToWire(Buffer buffer, LambdaResponse lambdaResponse) {
    JsonObject jsonToEncode = JsonObject.mapFrom(lambdaResponse);

    String jsonToStr = jsonToEncode.encode();

    int length = jsonToStr.getBytes().length;

    buffer.appendInt(length);
    buffer.appendString(jsonToStr);
  }

  @Override
  public LambdaResponse decodeFromWire(int position, Buffer buffer) {
    int _pos = position;

    int length = buffer.getInt(_pos);

    String jsonStr = buffer.getString(_pos+=4, _pos+=length);
    JsonObject contentJson = new JsonObject(jsonStr);
    return contentJson.mapTo(LambdaResponse.class);
  }

  @Override
  public LambdaResponse transform(LambdaResponse customMessage) {
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