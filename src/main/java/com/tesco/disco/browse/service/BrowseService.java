package com.tesco.disco.browse.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Created by bruno on 20/04/16.
 */
@ProxyGen
public interface BrowseService {
    void getBrowseResults(String index, String templateId, String geo, String distChannel, String responseType, JsonObject query, Handler<AsyncResult<JsonObject>> response);
}
