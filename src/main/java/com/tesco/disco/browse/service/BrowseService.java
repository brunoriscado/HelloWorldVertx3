package com.tesco.disco.browse.service;

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
public interface BrowseService {
    void getBrowseResults(JsonObject payload, Handler<AsyncResult<JsonObject>> response);
}
