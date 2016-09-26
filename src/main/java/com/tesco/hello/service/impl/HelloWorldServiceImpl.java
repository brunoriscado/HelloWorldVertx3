package com.tesco.hello.service.impl;

import com.tesco.hello.exceptions.ServiceException;
import com.tesco.hello.service.HelloWorldService;
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
public class HelloWorldServiceImpl implements HelloWorldService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldServiceImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("SERVICE");
    private MessageConsumer<JsonObject> consumer;
    private Vertx vertx;

    public HelloWorldServiceImpl() {
    }

    public HelloWorldServiceImpl(Vertx vertx) {
        this.vertx = vertx;
        consumer = ProxyHelper.registerService(HelloWorldService.class,
                (io.vertx.core.Vertx) vertx.getDelegate(), this, HelloWorldService.class.getName());
    }

    @Override
    public void getHelloWorldResults(JsonObject payload, Handler<AsyncResult<JsonObject>> response) {
        LOGGER.info(MARKER, "Fetching test results using payload: {} ", payload != null ? payload.encode() : "");
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
                            LOGGER.debug(MARKER, "Finished sending elasticsearch test request to controller verticle");
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
        handleBlocking.complete(payload);
    }

    public void unregister() {
        LOGGER.info(MARKER, "Unregistering verticle address: {} from the eventbus", HelloWorldService.class.getName());
        ProxyHelper.unregisterService(consumer);
    }
}
