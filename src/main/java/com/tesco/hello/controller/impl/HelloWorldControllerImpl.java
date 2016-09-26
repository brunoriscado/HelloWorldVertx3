package com.tesco.hello.controller.impl;

import com.tesco.hello.controller.HelloWorldController;
import com.tesco.hello.exceptions.ClientException;
import com.tesco.hello.exceptions.ServiceException;
import com.tesco.hello.service.rxjava.HelloWorldService;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bruno on 20/04/16.
 */
public class HelloWorldControllerImpl implements HelloWorldController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldControllerImpl.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("CONTROLLER");
    private Vertx vertx;
    private HelloWorldService helloWorldService;

    public HelloWorldControllerImpl(Vertx vertx, Router router, HelloWorldService helloWorldService) {
        this.vertx = vertx;
        this.helloWorldService = helloWorldService;
        init(router);
    }

    public void init(Router router) {
        LOGGER.info(MARKER, "Initializing routing definitions for controller");
        Router subRouter = Router.router(vertx);
        subRouter.route().handler(this::queryStringDecoder);

        subRouter.get("/test/*").handler(this::testHandler);
        router.mountSubRouter("/", subRouter);
    }

    private void queryStringDecoder(RoutingContext context) {
        Map<String, String> decoded = new HashMap<String, String>();
        context.request().params().names().forEach(param -> {
            decoded.put(param, QueryStringDecoder.decodeComponent(context.request().params().get(param), Charset.forName("UTF-8")));
        });
        LOGGER.debug(MARKER, "Decoded params: {}", Json.encode(decoded));
        context.put("decodedParams", decoded);
        context.next();
    };

    private void testHandler(RoutingContext context) {
        LOGGER.debug(MARKER, "Handling test request - {}", context.request().absoluteURI());
        test(new JsonObject().put("test", context.<Map<String, String>>get("decodedParams").get("q")), context.response());
    }

    public void test(JsonObject payload, HttpServerResponse response) {
        LOGGER.info(MARKER, "Handling test request using query params: {}", payload != null ? payload.encode() : "");
        if(!response.ended()) {
            response.setChunked(true);
            helloWorldService.getHelloWorldResultsObservable(payload)
                .subscribe(
                        next -> {
                            LOGGER.debug(MARKER, "response from elastic test service: {}", next.encode());
                            response.headers().add(HttpHeaders.CONTENT_TYPE.toString(), MimeMapping.getMimeTypeForExtension("json"));
                            if (payload != null && payload.containsKey("pretty") && payload.getBoolean("pretty")) {
                                response.write(next.encodePrettily());
                            } else {
                                response.write(next.encode());
                            }
                        },
                        error -> {
                            LOGGER.error(MARKER, "error obtaining response from elastic test service verticle: {}", error.getMessage());
                            handlerError(error, response);
                        },
                        () -> {
                            response.setStatusCode(200);
                            response.end();
                        });
        }
    }

    private void handlerError(Throwable error,  HttpServerResponse response) {
        if (error instanceof ClientException) {
            LOGGER.warn(MARKER, "bad request error occurred - {}", error.getMessage());
            response.setStatusCode(400);
        } else if (error instanceof ServiceException) {
            LOGGER.error(MARKER, "error occurred - {}", error.getMessage());
            response.setStatusCode(500);
        } else {
            LOGGER.error(MARKER, "error occurred - {}", error.getMessage());
            response.setStatusCode(500);
        }
        response.headers().add(HttpHeaders.CONTENT_TYPE.toString(), MimeMapping.getMimeTypeForExtension("json"));
        response.end(new JsonObject().put("error", error.getMessage()).encode());
    }
}