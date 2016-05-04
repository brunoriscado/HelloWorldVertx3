package com.tesco.disco.browse.controller.impl;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.exceptions.ClientException;
import com.tesco.disco.browse.model.enumerations.FieldsEnum;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.json.JsonArray;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseControllerImpl implements BrowseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseControllerImpl.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("CONTROLLER");
    private static final String TAXONOMY_INDEX = "ghs.taxonomy";
    private static final String PRODUCTS_INDEX = "ghs.products";
    private static final String TEMPLATE_ID_SUFIX = "default";
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
        subRouter.get("/browse/products").handler(this::browseProductsHandler);
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
        context.response().headers().set(HttpHeaders.CONTENT_TYPE.toString(), MimeMapping.getMimeTypeForExtension("txt"));
        context.response().end("keepalive");
    }

    private JsonObject handleParameters(RoutingContext context) {
        JsonObject query = new JsonObject();
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("version"))) {
            query.put("version", context.<Map<String, String>>get("decodedParams").get("version"));
        }

        validatePretty(context, query);
        validateExplain(context, query);
        validateResponseType(context, query);
        validateOffset(context, query);
        validateLimit(context, query);
        validateResponseSet(context, query);
        validateFields(context, query);
        validateResponseType(context, query);
        validateSort(context, query);
        validateStore(context, query);


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
        return query;
    }

    private void browseHandler(RoutingContext context) {
        browse(StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("index")) ?
                        context.<Map<String, String>>get("decodedParams").get("index") : TAXONOMY_INDEX,
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("config")) ?
                        context.<Map<String, String>>get("decodedParams").get("config") : TEMPLATE_ID_SUFIX,
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("geo")) ?
                        context.<Map<String, String>>get("decodedParams").get("geo") : "uk",
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("distChannel")) ?
                        context.<Map<String, String>>get("decodedParams").get("distChannel") : "ghs",
                handleParameters(context),
                context.response());
    }

    private void browseProductsHandler(RoutingContext context) {
        browse(StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("index")) ?
                        context.<Map<String, String>>get("decodedParams").get("index") : PRODUCTS_INDEX,
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("config")) ?
                        context.<Map<String, String>>get("decodedParams").get("config") : TEMPLATE_ID_SUFIX,
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("geo")) ?
                        context.<Map<String, String>>get("decodedParams").get("geo") : "uk",
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("distChannel")) ?
                        context.<Map<String, String>>get("decodedParams").get("distChannel") : "ghs",
                handleParameters(context),
                context.response());
    }

    //TODO - Annotate this method so that swagger definitions can be generated
    public void browse(String index, String templateId, String geo, String distChannel, JsonObject payload, HttpServerResponse response) {
        LOGGER.info(MARKER, "Handling browse request using query params: {}", payload != null ? payload.encode() : "");
        ObservableHandler<AsyncResult<JsonObject>> handler = RxHelper.observableHandler();
        browseService.getBrowseResults(index, templateId, geo, distChannel, payload, handler.toHandler());
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

    private void validateResponseType(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("resType"))) {
            String resType = context.<Map<String, String>>get("decodedParams").get("resType");
            if (resType.equals("products") || resType.equals("terms")) {
                query.put("resType", context.<Map<String, String>>get("decodedParams").get("resType"));
            } else {
                throw new ClientException("Incorrect resType type!");
            }
        } else {
            query.put("resType", "products");
        }
    }

    private void validateFields(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("fields"))) {
            JsonArray array = new JsonArray();
            List<String> fields = Arrays.asList(context.<Map<String, String>>get("decodedParams").get("fields").split(","));
            fields.forEach(field -> {
                if (FieldsEnum.getField(field) != null) {
                    array.add(field);
                }
            });
            query.put("fields", array);
        } else {
            query.put("fields", new JsonArray().add(FieldsEnum.TPNB.getName()));
        }
    }

    private void validateResponseSet(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("responseSet"))) {
            String responseSet = context.<Map<String, String>>get("decodedParams").get("responseSet");
            JsonArray array = new JsonArray();
            List<String> set = Arrays.asList(context.<Map<String, String>>get("decodedParams").get("responseSet").split(","));
            set.forEach(res -> {
                if (res.equals("results") || res.equals("totals") || res.equals("suggestions") || res.equals("taxonomy")) {
                    array.add(res);
                }
            });
            query.put("responseSet", array);
        } else {
            query.put("resType", new JsonArray().add("results").add("totals").add("suggestions"));
        }
    }

    private void validateOffset(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("offset"))) {
            try {
                int offset = Integer.valueOf(context.<Map<String, String>>get("decodedParams").get("offset"));
                if (offset <= 0 && offset <= 100000) {
                    query.put("offset", offset);
                } else {
                    throw new ClientException("Incorrect offset type!");
                }

            } catch (NumberFormatException e) {
                throw new ClientException("Incorrect offset type!");
            }
        } else {
            query.put("offset", 0);
        }
    }

    private void validateLimit(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("limit"))) {
            try {
                int limit = Integer.valueOf(context.<Map<String, String>>get("decodedParams").get("limit"));
                if (limit <= 0 && limit <= 100) {
                    query.put("limit", limit);
                } else {
                    throw new ClientException("Incorrect limit type!");
                }

            } catch (NumberFormatException e) {
                throw new ClientException("Incorrect limit type!");
            }
        } else {
            query.put("limit", 10);
        }
    }

    private void validateStore(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("store"))) {
            try {
                int store = Integer.valueOf(context.<Map<String, String>>get("decodedParams").get("store"));
                query.put("store", store);
            } catch (NumberFormatException e) {
                throw new ClientException("Incorrect store type!");
            }
        } else {
            query.put("store", 3060);
        }
    }

    private void validateSort(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("sort"))) {
            String sort = context.<Map<String, String>>get("decodedParams").get("sort");
            if (sort.equals("price") || sort.equals("price:desc") || sort.equals("name") || sort.equals("name:desc")) {
                query.put("sort", sort);
            }
        }
    }

    private void validateExplain(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("explain"))) {
            boolean explain = Boolean.valueOf(context.<Map<String, String>>get("decodedParams").get("explain"));
            query.put("explain", explain);
        }
    }

    private void validatePretty(RoutingContext context, JsonObject query) {
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("pretty"))) {
            boolean pretty = Boolean.valueOf(context.<Map<String, String>>get("decodedParams").get("pretty"));
            query.put("pretty", pretty);
        }
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