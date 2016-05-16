package com.tesco.disco.browse.controller.impl;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.exceptions.ClientException;
import com.tesco.disco.browse.exceptions.ServiceException;
import com.tesco.disco.browse.model.enumerations.*;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.json.Json;
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

        subRouter.get("/browse/*").handler(this::browseProductsHandler);
        subRouter.get("/taxonomy/*").handler(this::browseHandler);
        subRouter.get("/_status").handler(this::statusHandler);

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

    private void statusHandler(RoutingContext context) {
        LOGGER.debug(MARKER, "Handling status request");
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
        validateOffset(context, query);
        validateLimit(context, query);
        validateResponseSet(context, query);
        validateFields(context, query);
        validateSort(context, query);
        validateStore(context, query);
        validateBrand(context, query);

        //If tpnb, new or offer as explicitly passed in the query string (outside filters),
        //then the initial filters are overriden
        validatePipedFilters(context, query);
        validateTPNB(context, query);
        validateNew(context, query);
        validateOffer(context, query);

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
        LOGGER.debug(MARKER, "Handling taxonomy request - {}", context.request().absoluteURI());
        browse(StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("index")) ?
                        context.<Map<String, String>>get("decodedParams").get("index") : IndicesEnum.GHS_TAXONOMY.getIndex(),
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("config")) ?
                        context.<Map<String, String>>get("decodedParams").get("config") : TEMPLATE_ID_SUFIX,
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("geo")) ?
                        context.<Map<String, String>>get("decodedParams").get("geo") : "uk",
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("distChanwl")) ?
                        context.<Map<String, String>>get("decodedParams").get("distChannel") : "ghs",
                validateResponseType(context,  ResponseTypesEnum.TAXONOMY.getType()),
                handleParameters(context),
                context.response());
    }

    private void browseProductsHandler(RoutingContext context) {
        LOGGER.debug(MARKER, "Handling browse request - {}", context.request().absoluteURI());
        browse(StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("index")) ?
                        context.<Map<String, String>>get("decodedParams").get("index") : IndicesEnum.GHS_PRODUCTS.getIndex(),
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("config")) ?
                        context.<Map<String, String>>get("decodedParams").get("config") : TEMPLATE_ID_SUFIX,
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("geo")) ?
                        context.<Map<String, String>>get("decodedParams").get("geo") : "uk",
                StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("distChannel")) ?
                        context.<Map<String, String>>get("decodedParams").get("distChannel") : "ghs",
                validateResponseType(context, ResponseTypesEnum.PRODUCTS.getType()),
                handleParameters(context),
                context.response());
    }

    //TODO - Annotate this method so that swagger definitions can be generated
    public void browse(String index, String templateId, String geo, String distChannel, String responseType, JsonObject payload, HttpServerResponse response) {
        LOGGER.info(MARKER, "Handling browse request using query params: {}", payload != null ? payload.encode() : "");
        ObservableHandler<AsyncResult<JsonObject>> handler = RxHelper.observableHandler();
        browseService.getBrowseResults(index, templateId, geo, distChannel, responseType, payload, handler.toHandler());
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

    private String validateResponseType(RoutingContext context, String defaultResponseType) {
        LOGGER.debug(MARKER, "Validating Response Type");
        String responseType = null;
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("resType"))) {
            String resType = context.<Map<String, String>>get("decodedParams").get("resType");
            if (resType.equals("products") || resType.equals("terms") || resType.equals("taxonomy")) {
                responseType = context.<Map<String, String>>get("decodedParams").get("resType");
            } else {
                throw new ClientException("Incorrect resType type!");
            }
        } else {
            responseType = defaultResponseType;
        }
        LOGGER.debug(MARKER, "Response Type - {}", responseType);
        return responseType;
    }

    private void validateFields(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating Fields");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("fields"))) {
            StringBuffer csvFields = new StringBuffer();
            List<String> fields = Arrays.asList(context.<Map<String, String>>get("decodedParams").get("fields").split(","));
            fields.forEach(field -> {
                if (FieldsEnum.getByName(field, null) != null) {
                    csvFields.append("\"").append(field).append("\"").append(",");
                }
            });
            query.put("fields", StringUtils.substring(csvFields.toString(), 0, csvFields.length()-1));
        } else {
            query.put("fields", "\"" + FieldsEnum.TPNB.getName() + "\"");
        }
        LOGGER.debug(MARKER, "Validating Fields - {}", query.encode());
    }

    private void validateResponseSet(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating Response Set");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("responseSet"))) {
            Arrays.asList(context.<Map<String, String>>get("decodedParams").get("responseSet").split(",")).forEach(res -> {
                if (ResponseSetEnum.getBySetName(res) != null) {
                    query.put(res, String.valueOf(true));
                }
            });
        } else {
            query.put(ResponseSetEnum.RESULTS.getSetName(), String.valueOf(true));
            query.put(ResponseSetEnum.TOTALS.getSetName(), String.valueOf(true));
        }
        LOGGER.debug(MARKER, "Validating Response Set - {}", query.encode());
    }

    private void validateOffset(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating Offset");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("offset"))) {
            try {
                int offset = Integer.valueOf(context.<Map<String, String>>get("decodedParams").get("offset"));
                if (offset <= 0 && offset <= 100000) {
                    query.put("offset", String.valueOf(offset));
                } else {
                    throw new ClientException("Incorrect offset type!");
                }

            } catch (NumberFormatException e) {
                throw new ClientException("Incorrect offset type!");
            }
        } else {
            query.put("offset", String.valueOf(0));
        }
        LOGGER.debug(MARKER, "Validating Offset - {}", query.encode());
    }

    private void validateLimit(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating limit");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("limit"))) {
            try {
                int limit = Integer.valueOf(context.<Map<String, String>>get("decodedParams").get("limit"));
                if (limit >= 0 && limit <= 100) {
                    query.put("limit", String.valueOf(limit));
                } else {
                    throw new ClientException("Incorrect limit type!");
                }

            } catch (NumberFormatException e) {
                throw new ClientException("Incorrect limit type!");
            }
        } else {
            query.put("limit", String.valueOf(10));
        }
        LOGGER.debug(MARKER, "Validating limit - {}", query);
    }

    private void validateStore(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating store");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("store"))) {
            try {
                int store = Integer.valueOf(context.<Map<String, String>>get("decodedParams").get("store"));
                query.put("store", String.valueOf(store));
            } catch (NumberFormatException e) {
                throw new ClientException("Incorrect store type!");
            }
        } else {
            query.put("store", String.valueOf(3060));
        }
        LOGGER.debug(MARKER, "Validating store - {}", query.encode());
    }

    private void validateTPNB(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating TPNB");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("tpnb"))) {
            validateTPNBArray(context.<Map<String, String>>get("decodedParams").get("tpnb"), query);
        }
        LOGGER.debug(MARKER, "Validating tpnb - {}", query.encode());
    }

    private void validateTPNBArray(String tpnbs, JsonObject query) {
        if (StringUtils.isNotBlank(tpnbs)) {
            String[] tpnbArray = tpnbs.split(",");
            String arrStr =  "\"" + StringUtils.join(tpnbArray, "\",\"") + "\"";
            query.put("tpnb", String.valueOf(arrStr));
        }
    }

    private void validateBrand(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating brand");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("brand"))) {
            List<String> brandList = Arrays.asList(context.<Map<String, String>>get("decodedParams").get("brand").split(","));
            brandList.forEach(brand -> {
                query.put("brands", brand);
            });
        }
        LOGGER.debug(MARKER, "Validating brand - {}", query);
    }

    private void validateSort(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating sort");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("sort"))) {
            String sort = context.<Map<String, String>>get("decodedParams").get("sort");
            if (sort.equals("price") || sort.equals("price:desc") || sort.equals("name") || sort.equals("name:desc")) {
                query.put("sort", sort);
            }
        }
        LOGGER.debug(MARKER, "Validating sort - {}", query.encode());
    }

    private void validateExplain(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating explain");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("explain"))) {
            boolean explain = Boolean.valueOf(context.<Map<String, String>>get("decodedParams").get("explain"));
            query.put("explain", explain);
        }
        LOGGER.debug(MARKER, "Validating explain - {}", query.encode());
    }

    private void validatePretty(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating pretty");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("pretty"))) {
            boolean pretty = Boolean.valueOf(context.<Map<String, String>>get("decodedParams").get("pretty"));
            query.put("pretty", pretty);
        }
        LOGGER.debug(MARKER, "Validating pretty - {}", query.encode());
    }

			 // Converts a pipe separated list of properties into boolean parameters
    // eg. filter=a:true|b:false => a=true; b=true
    private void validatePipedFilters(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating piped filters");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("filter"))) {
            String filters = context.<Map<String, String>>get("decodedParams").get("filter").replaceAll("\\%7C","|").replaceAll("\\%7c","|");
            String[] multiBoolEnumValues = filters.split("\\|");
            for (int i = 0; i < multiBoolEnumValues.length; i++) {
                String[] keyVal = multiBoolEnumValues[i].split(":");
                if (keyVal.length == 2 && PipedFilters.getByFilterName(keyVal[0]) != null) {
                    if (keyVal[1].equals("true")) {
                        query.put(keyVal[0], keyVal[1]);
                    }
                } else if (keyVal.length == 2 && PipedFilters.getByFilterName(keyVal[0]).equals(PipedFilters.TPNB)) {
                    validateTPNBArray(keyVal[1], query);
                } else {
                    throw new ClientException("Piped filters contain have been incorrectly specified");
                }
            }
        }
    }

    private void validateNew(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating new filter");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("new"))) {
            boolean newFilter = Boolean.valueOf(context.<Map<String, String>>get("decodedParams").get("new"));
            query.put("new", String.valueOf(newFilter));
        }
    }

    private void validateOffer(RoutingContext context, JsonObject query) {
        LOGGER.debug(MARKER, "Validating offer filter");
        if (StringUtils.isNotBlank(context.<Map<String, String>>get("decodedParams").get("offer"))) {
            boolean offerFilter = Boolean.valueOf(context.<Map<String, String>>get("decodedParams").get("offer"));
            query.put("offer", String.valueOf(offerFilter));
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
        response.end(new JsonObject().put("error", error.getMessage()).encode());
    }
}