package com.tesco.disco.browse.service.impl;

import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;
import io.vertx.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceImpl implements BrowseService {
    MessageConsumer<JsonObject> consumer;

    public BrowseServiceImpl(Vertx vertx) {
        consumer = ProxyHelper
                .registerService(BrowseService.class, (io.vertx.core.Vertx)vertx.getDelegate(), this, BrowseService.class.getName());
    }

    @Override
    public void getBrowseResults(JsonObject query, Handler<AsyncResult<JsonObject>> response) {
        response.handle(Future.succeededFuture(new JsonObject().put("cenas", "cenas")));
    }

    public void unregister() {
        ProxyHelper.unregisterService(consumer);
    }
}
