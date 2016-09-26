package com.tesco.hello.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by bruno on 20/04/16.
 */
@ProxyGen
@VertxGen
public interface HelloWorldService {
    void getHelloWorldResults(JsonObject payload, Handler<AsyncResult<JsonObject>> response);
}
