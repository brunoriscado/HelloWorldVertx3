package com.tesco.disco.browse.service.impl;

import com.tesco.disco.browse.exceptions.ServiceException;
import com.tesco.disco.browse.model.enumerations.FieldsEnum;
import com.tesco.disco.browse.model.enumerations.IndicesEnum;
import com.tesco.disco.browse.model.enumerations.ResponseTypesEnum;
import com.tesco.disco.browse.model.taxonomy.*;
import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchClientFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.script.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rx.Observable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceImpl implements BrowseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceImpl.class);
    private static final Marker MARKER = MarkerFactory.getMarker("SERVICE");
    private MessageConsumer<JsonObject> consumer;
    private TransportClient client;
    private Vertx vertx;

    public BrowseServiceImpl() {
    }

    public BrowseServiceImpl(Vertx vertx, ElasticSearchClientFactory elasticSearchClientFactory) {
        this.vertx = vertx;
        client = elasticSearchClientFactory.getElasticsearchClient();
        consumer = ProxyHelper.registerService(BrowseService.class,
                (io.vertx.core.Vertx) vertx.getDelegate(), this, BrowseService.class.getName());
    }

    @Override
    public void getBrowseResults(String index,
            String templateId,
            String geo,
            String distChannel,
            String responseType,
            JsonObject query,
            Handler<AsyncResult<JsonObject>> response) {
        LOGGER.info(MARKER, "Fetching browse results for index: {}, templateId: {} - using query params: {} ",
                index, templateId, query != null ? query.encode() : "");
        vertx.<JsonObject>executeBlockingObservable(handleBlocking -> {
            JsonObject result = null;
            SearchResponse res = client.prepareSearch()
                    .setIndices(index)
                    .setTemplateName(IndicesEnum.getByIndexName(index).getIndex() + "." + templateId)
                    .setTemplateType(ScriptService.ScriptType.INDEXED)
                    .setTemplateParams(query == null ? new HashMap<String, Object>() : convertJsonArrays(query))
                    .execute().actionGet();
            try {
                XContentBuilder builder = XContentFactory.jsonBuilder();
                builder.startObject();
                res.toXContent(builder, SearchResponse.EMPTY_PARAMS);
                builder.endObject();
                result = new JsonObject(builder.string());
            } catch (IOException e) {
                handleBlocking.fail(new ServiceException(e.getMessage()));
            }
            handleBlocking.complete(result);
        })
        .flatMap(elasticResponse -> {
            JsonObject params = new JsonObject()
                    .put("geo", geo)
                    .put("index", index)
                    .put("config", templateId)
                    .put("distChannel", distChannel)
                    .put("resType", responseType);
            if (query != null) {
                params.mergeIn(query);
            }
            return getApiResponse(elasticResponse, params);
        })
        .subscribe(
                next -> {
                    response.handle(Future.succeededFuture(next));
                },
                error -> {
                    LOGGER.error(MARKER, "Error obtaining/mapping elasticsearch response");
                    response.handle(Future.failedFuture(error));
                },
                () -> {
                    LOGGER.debug(MARKER, "Finished sending elasticsearch browse request to controller verticle");
                });
    }

    public Observable<JsonObject> getApiResponse(JsonObject elasticResponse, JsonObject params){
        Observable<JsonObject> apiResponse = null;
        ResponseTypesEnum type = ResponseTypesEnum.getByType(params.getString("resType"));
        if (elasticResponse.getString("status") == "error") {
            // elasticsearch vertx mod responsed with an exception
            apiResponse = Observable.error(new ServiceException("Elasticsearch response error"));
        } else {
            apiResponse = getApiResponseBody(elasticResponse, params, type);
        }
        return apiResponse;
    }

    protected Observable<JsonObject> getApiResponseBody(JsonObject elasticsearchResponse, JsonObject params, ResponseTypesEnum type) {
        return Observable.just(new JsonObject())
                .map(apiResult -> {
                    JsonObject resType = new JsonObject();
                    if (type.equals(ResponseTypesEnum.PRODUCTS)) {
                        if (params.getBoolean("totals", false)) {
                            resType.put("totals", getResultsSet(elasticsearchResponse));
                        }
                        if (params.getBoolean("results", false)) {
                            resType.put("results", parseResults(elasticsearchResponse, params));
                        }
                        if (params.getBoolean("suggestions", false)) {
                            resType.put("suggestions", parseDidYouMeanResults(elasticsearchResponse));
                        }
                    }
                    JsonObject distChannel = new JsonObject();
                    distChannel.put(params.getString("resType"), resType);
                    JsonObject geo = new JsonObject();
                    geo.put(params.getString("distChannel"), distChannel);
                    return apiResult.put(params.getString("geo"), geo);
                })
                .flatMap(apiResult -> {
                    if (type.equals(ResponseTypesEnum.PRODUCTS) && params.getBoolean("taxonomy", false)) {
                        return getBrowsingTaxonomy(elasticsearchResponse)
                                .map(taxonomyResponse -> {
                                    apiResult.getJsonObject(params.getString("geo"))
                                            .getJsonObject(params.getString("distChannel"))
                                            .getJsonObject(params.getString("resType"))
                                            .put("taxonomy", taxonomyResponse);
                                    return apiResult;
                                });
                    } else if (type.equals(ResponseTypesEnum.TAXONOMY)) {
                        return getBrowsingTaxonomy(elasticsearchResponse)
                                .map(taxonomyResponse -> {
                                    apiResult.getJsonObject(params.getString("geo"))
                                            .getJsonObject(params.getString("distChannel"))
                                            .put("taxonomy", taxonomyResponse);
                                    return apiResult;
                                });
                    } else {
                        return Observable.just(apiResult);
                    }
                });
    }

    protected JsonObject getResultsSet(JsonObject elasticResponse) {
        JsonObject total = new JsonObject();
        total.put("all", elasticResponse.getJsonObject("hits").getLong("total"));
        // TODO not yet sent from elastic.
        // total.put("IsFavourite", ESResponse.getObject("hits").getLong("IsFavourite"));
        // total.put("IsNewlyRangedInStore", ESResponse.getObject("hits").getLong("IsNew"));
        // total.put("IsOnPromotion", ESResponse.getObject("hits").getLong("IsSpecialOffer"));
        return total;
    }

    protected JsonArray parseResults(JsonObject elasticsearchResponse, JsonObject params) {
        JsonArray hits = elasticsearchResponse.getJsonObject("hits").getJsonArray("hits");
        Iterator<Object> i = hits.iterator() ;
        JsonArray entries = new JsonArray();
        while ( i.hasNext() ) {
            Object hit = i.next();
            JsonObject entry = (JsonObject) hit;
            JsonObject properties = entry.getJsonObject("_source");
            // append explanation as a property if it exists
            if (entry.containsKey("_explanation")) {
                properties.put("_explanation", entry.getJsonObject("_explanation"));
            }
            // augment availability, price and unit price properties if they exists
            if (properties.containsKey(FieldsEnum.PRICE.getRemapName())) {
                properties = extractProperty(FieldsEnum.PRICE.getRemapName(),  properties, params);
            }
            if (properties.containsKey(FieldsEnum.UNIT_PRICE.getRemapName())) {
                properties = extractProperty(FieldsEnum.UNIT_PRICE.getRemapName(), properties, params);
            }
            if (properties.containsKey(FieldsEnum.AVAILABILITY.getRemapName())) {
                properties = extractProperty(FieldsEnum.AVAILABILITY.getRemapName(), properties, params);
            }
            //TODO: REMOVE WHEN MAPI CONSUMER THE NEW FILTERS FROM QUERY PARAMETER
            if (properties.containsKey(FieldsEnum.IS_NEW.getName())) {
                parseFilter(properties, FieldsEnum.IS_NEW.getName(), params);
            }
            //TODO: REMOVE WHEN MAPI CONSUMER THE NEW FILTERS FROM QUERY PARAMETER
            if (properties.containsKey(FieldsEnum.IS_SPECIAL_OFFER.getName())) {
                parseFilter(properties, FieldsEnum.IS_SPECIAL_OFFER.getName(), params);
            }
            if (properties.containsKey(FieldsEnum.NEW.getName())) {
                parseFilter(properties, FieldsEnum.NEW.getName(), params);
            }
            if (properties.containsKey(FieldsEnum.OFFER.getName())) {
                parseFilter(properties, FieldsEnum.OFFER.getName(), params);
            }
            entries.add(properties);
        }
        return entries;
    }

    private JsonObject extractProperty(String property, JsonObject properties, JsonObject params) {
        JsonArray numbers = properties.getJsonArray(property);
        Iterator<Object> p = numbers.iterator() ;
        Number value = null;

        while (p.hasNext() ) {
            JsonObject storeNumber = (JsonObject) p.next();
            if (storeNumber.getLong("store").toString().equals(params.getString("store"))) {
                value = storeNumber.getLong(property);
            }
        }
        properties.put(property, value);
        properties.remove("store_" + property);
        return properties;
    }

    protected JsonObject parseFilter(JsonObject properties, String filter, JsonObject params){
        JsonArray filterArr = properties.getJsonArray(filter);
        Iterator<Object> n = filterArr.iterator() ;
        boolean filterBool = false;
        while (n.hasNext() ) {
            String is = (String) n.next();
            if (is.equals(params.getString("store"))) {
                filterBool = true;
                break;
            }
        }
        properties.remove(filter);
        properties.put(filter, filterBool);
        return properties;
    }

    protected JsonArray parseDidYouMeanResults(JsonObject elasticsearchResponse) {
        JsonArray entries = new JsonArray();
        if (elasticsearchResponse.getJsonObject("suggest").getJsonArray("check").size() == 0) {
            return entries;
        }
        JsonObject s = elasticsearchResponse.getJsonObject("suggest").getJsonArray("check").getJsonObject(0);
        JsonArray suggestions = s.getJsonArray("options");
        Iterator<Object> i = suggestions.iterator() ;
        while (i.hasNext()) {
            Object hit = i.next();
            JsonObject entry = (JsonObject) hit;
            entries.add(entry);
        }
        // resultSet.putArray("didYouMeanResults", entries);
        return entries;
    }

    /**
     * This method maps the elasticsearch aggregation response to a taxonomy response that the client expects
     * @param elasticResponse
     * @return
     */
    public Observable<JsonObject> getBrowsingTaxonomy(JsonObject elasticResponse) {
        LOGGER.debug(MARKER, "Mapping elastic aggregations response to API taxonomy");
        Taxonomy taxonomy = new Taxonomy();
        return Observable.just(elasticResponse)
                .filter(elasticResp -> {
                    return elasticResponse.containsKey("aggregations");
                })
                .switchIfEmpty(Observable.error(new RuntimeException("no aggregations found")))
                .flatMap(elasticResp -> {
                    return Observable.from(elasticResp.getJsonObject("aggregations")
                            .getJsonObject("superDepartments")
                            .getJsonArray("buckets").getList().toArray());
                })
                .switchIfEmpty(
                        Observable.error(new RuntimeException("SuperDepartments are empty")))
                .flatMap(superDepartment -> {
                    LOGGER.debug(MARKER, "Mapping superDepartments");
                    JsonObject sd = new JsonObject(Json.encode(superDepartment));
                    return Observable.from(sd.getJsonObject("departments").getJsonArray("buckets").getList().toArray())
                            .switchIfEmpty(
                                    Observable.error(new RuntimeException("Departments are empty")))
                            .flatMap(department -> {
                                LOGGER.debug(MARKER, "Mapping departments");
                                JsonObject dep = new JsonObject(Json.encode(department));
                                return Observable.from(dep.getJsonObject("aisles").getJsonArray("buckets").getList().toArray())
                                        .switchIfEmpty(
                                                Observable.error(new RuntimeException("Aisles are empty")))
                                        .flatMap(aisle -> {
                                            LOGGER.debug(MARKER, "Mapping aisles");
                                            JsonObject jAisle = new JsonObject(Json.encode(aisle));
                                            return Observable.from(jAisle.getJsonObject("shelves").getJsonArray("buckets").getList().toArray())
                                                    .switchIfEmpty(
                                                            Observable.error(new RuntimeException("Shelves are empty")))
                                                    .map(shelf -> {
                                                        LOGGER.debug(MARKER, "Mapping shelves");
                                                        JsonObject jShelf = new JsonObject(Json.encode(shelf));
                                                        return new Shelf(jShelf.getString("key"));
                                                    })
                                                    .collect(() -> new ArrayList<Shelf>(),
                                                            (shelves, she) -> shelves.add(she))
                                                    .map(shelves -> {
                                                        return new Aisle(jAisle.getString("key")).addShelves(shelves);
                                                    });
                                        })
                                        .collect(() -> new ArrayList<Aisle>(),
                                                (aisles, aisle) -> aisles.add(aisle))
                                        .map(aisles -> {
                                            return new Department(dep.getString("key")).addAisles(aisles);
                                        });
                            })
                            .collect(() -> new ArrayList<Department>(),
                                    (departments, department) -> departments.add(department))
                            .map(departments -> {
                                return new SuperDepartment(sd.getString("key")).addDepartments(departments);
                            })
                            .collect(() -> new ArrayList<SuperDepartment>(),
                                    (superDeps, superDep) -> superDeps.add(superDep))
                            .map(supeDeps -> {
                                return taxonomy.addSuperDepartments(supeDeps).toJson();
                            });
                })
                .onErrorResumeNext(Observable.just(taxonomy.toJson()));
    }

				/**
     * a bit of a hack to keep the elastic client happy with lists rather then jsonArrays
     */
    private Map<String, Object> convertJsonArrays(JsonObject query) {
        Map<String, Object> resultingQuery = null;
        if (query != null && !query.isEmpty()) {
            resultingQuery = new HashMap<String, Object>(query.size());
            for (Iterator<Map.Entry<String, Object>> it = query.<Map.Entry<String, Object>>iterator(); it.hasNext();) {
                Map.Entry<String, Object> entry = it.next();
                if (entry.getValue() instanceof JsonArray && !((JsonArray) entry.getValue()).isEmpty()) {
                    resultingQuery.put(entry.getKey(), ((JsonArray)entry.getValue()).getList());
                } else {
                    resultingQuery.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return resultingQuery;
    }

    public void unregister() {
        LOGGER.info(MARKER, "Unregistering verticle address: {} from the eventbus", BrowseService.class.getName());
        ProxyHelper.unregisterService(consumer);
    }
}
