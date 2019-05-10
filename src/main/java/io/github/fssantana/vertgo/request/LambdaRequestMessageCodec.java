package io.github.fssantana.vertgo.request;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

/**
 * Vertex message codec
 */
public class LambdaRequestMessageCodec implements MessageCodec<LambdaRequest, LambdaRequest> {

  @Override
  public void encodeToWire(Buffer buffer, LambdaRequest lambdaRequest) {
    JsonObject jsonToEncode = JsonObject.mapFrom(lambdaRequest);

    String jsonToStr = jsonToEncode.encode();

    int length = jsonToStr.getBytes().length;

    buffer.appendInt(length);
    buffer.appendString(jsonToStr);
  }

  @Override
  public LambdaRequest decodeFromWire(int position, Buffer buffer) {
    int _pos = position;

    int length = buffer.getInt(_pos);

    String jsonStr = buffer.getString(_pos+=4, _pos+=length);
    JsonObject contentJson = new JsonObject(jsonStr);
    return contentJson.mapTo(LambdaRequest.class);
  }

  @Override
  public LambdaRequest transform(LambdaRequest customMessage) {
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