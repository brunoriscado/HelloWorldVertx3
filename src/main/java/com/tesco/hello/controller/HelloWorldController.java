package com.tesco.hello.controller;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerResponse;

/**
 * Created by bruno on 20/04/16.
 */
public interface HelloWorldController {
    void test(HttpServerResponse response);
}
