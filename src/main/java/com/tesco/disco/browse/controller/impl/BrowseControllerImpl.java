package com.tesco.disco.browse.controller.impl;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.exceptions.ClientException;
import com.tesco.disco.browse.exceptions.ServiceException;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rx.Observable;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseControllerImpl implements BrowseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseControllerImpl.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("CONTROLLER");
    private static final String INDEX = "ghs.taxonomy";
    private static final String TEMPLATE_ID = "ghs.taxonomy.default";
    private Vertx vertx;
    private BrowseService browseService;

    public BrowseControllerImpl(Vertx vertx, Router router, BrowseService browseService) {
        this.vertx = vertx;
        this.browseService = browseService;
        init(router);
    }

    public void init(Router router) {
        LOGGER.info(MARKER, "Initializing routing definitions for controller");
        Router subRouter = Router.router(vertx);
        subRouter.route().handler(this::queryStringDecoder);

        subRouter.get("/browse/*").handler(this::browseHandler);
        subRouter.get("/_status").handler(this::statusHandler);

        router.mountSubRouter("/", subRouter);
    }

    private void queryStringDecoder(RoutingContext context) {
        Map<String, String> decoded = new HashMap<String, String>();
        context.request().params().names().forEach(param -> {
            decoded.put(param, QueryStringDecoder.decodeComponent(context.request().params().get(param), Charset.forName("UTF-8")));
        });
        context.put("decodedParams", decoded);
        context.next();
    };

    private void statusHandler(RoutingContext context) {
        context.response().setStatusCode(200);
        context.response().end("keepalive");
    }

    private void browseHandler(RoutingContext context) {
        JsonObject query = new JsonObject();
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("superDepartment"))) {
            query.put("superDepartment", context.<Map<String, String>>get("decodedParams").get("superDepartment"));
        }
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("department"))) {
            query.put("department", context.<Map<String, String>>get("decodedParams").get("department"));
        }
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("aisle"))) {
            query.put("aisle", context.<Map<String, String>>get("decodedParams").get("aisle"));
        }
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("shelf"))) {
            query.put("shelf", context.<Map<String, String>>get("decodedParams").get("shelf"));
        }
        if (query.isEmpty()) {
            query = null;
        }
        browse(StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("geo")) ?
                        context.<Map<String, String>>get("decodedParams").get("geo") : "uk",
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("distChannel")) ?
                        context.<Map<String, String>>get("decodedParams").get("distChannel") : "ghs",
                query,
                context.response());
    }

    //TODO - Annotate this method so that swagger definitions can be generated
    public void browse(String geo, String distChannel, JsonObject payload, HttpServerResponse response) {
        LOGGER.info(MARKER, "Handling browse request using query params: {}", payload != null ? payload.encode() : "");
        ObservableHandler<AsyncResult<JsonObject>> handler = RxHelper.observableHandler();
        browseService.getBrowseResults(INDEX, TEMPLATE_ID, geo, distChannel, payload, handler.toHandler());
        response.setChunked(true);
        handler.flatMap(esResponse -> {
                    if (esResponse.succeeded()) {
                        return Observable.just(esResponse.result());
                    } else {
                        return Observable.error(esResponse.cause());
                    }
                })
                .subscribe(
                        next -> {
                            LOGGER.debug(MARKER, "response from elastic browse service: {}", next.encode());
                            response.headers().add(HttpHeaders.CONTENT_TYPE.toString(), MimeMapping.getMimeTypeForExtension("json"));
                            response.write(next.encode());
                        },
                        error -> {
                            LOGGER.error(MARKER, "error obtaining response from elastic browse service verticle: {}", error.getMessage());
                            handlerError(error, response);
                        },
                        () -> {
                            response.setStatusCode(200);
                            response.end();
                        });
    }

    private void handlerError(Throwable error,  HttpServerResponse response) {
        if (error instanceof ClientException) {
            response.setStatusCode(400);
        } else if (error instanceof ClientException) {
            response.setStatusCode(500);
        } else {
            response.setStatusCode(500);
        }
        response.end(new JsonObject().put("error", error.getMessage()).encode());
    }
}