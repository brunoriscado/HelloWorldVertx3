package com.tesco.disco.browse.service.impl;

import com.tesco.disco.browse.exceptions.ServiceException;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Future;
import io.vertx.rxjava.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rx.Observable;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceImpl implements BrowseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("SERVICE");
    private MessageConsumer<JsonObject> consumer;
    private Vertx vertx;

    public BrowseServiceImpl() {
    }

    public BrowseServiceImpl(Vertx vertx) {
        this.vertx = vertx;
        consumer = ProxyHelper.registerService(BrowseService.class,
                (io.vertx.core.Vertx) vertx.getDelegate(), this, BrowseService.class.getName());
    }

    @Override
    public void getBrowseResults(JsonObject payload, Handler<AsyncResult<JsonObject>> response) {
        LOGGER.info(MARKER, "Fetching browse results using payload: {} ", payload != null ? payload.encode() : "");
        blockingContext(payload)
                .subscribe(
                        next -> {
                            response.handle(io.vertx.core.Future.succeededFuture(next));
                        },
                        error -> {
                            LOGGER.error(MARKER, "Error obtaining/mapping elasticsearch response");
                            response.handle(io.vertx.core.Future.failedFuture(error));
                        },
                        () -> {
                            LOGGER.debug(MARKER, "Finished sending elasticsearch browse request to controller verticle");
                        });
    }

    private Observable<JsonObject> blockingContext(JsonObject payload) {
        return vertx.<JsonObject>executeBlockingObservable(handleBlocking -> {
            executeSearchQueryRequest(payload, handleBlocking);
        });
    }

    private void executeSearchQueryRequest(JsonObject payload, Future<JsonObject> handleBlocking) {
        LOGGER.debug(MARKER, "Doing some blocking work - {}", payload.encode());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOGGER.warn(MARKER, "error doing some blocking stuff - {}", e.getMessage());
            handleBlocking.fail(new ServiceException(e.getMessage()));
        }
        handleBlocking.complete(new JsonObject().put("test", "payload response"));
    }

    public void unregister() {
        LOGGER.info(MARKER, "Unregistering verticle address: {} from the eventbus", BrowseService.class.getName());
        ProxyHelper.unregisterService(consumer);
    }
}
