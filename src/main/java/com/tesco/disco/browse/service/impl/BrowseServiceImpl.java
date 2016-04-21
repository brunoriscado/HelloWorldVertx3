package com.tesco.disco.browse.service.impl;

import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;
import io.vertx.core.Future;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceImpl implements BrowseService {
    @Override
    public void getBrowseResults(JsonObject query, Handler<AsyncResult<JsonObject>> response) {
        response.handle(Future.succeededFuture(new JsonObject().put("cenas", "cenas")));
    }
}
