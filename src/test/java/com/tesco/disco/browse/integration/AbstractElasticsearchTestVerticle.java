package com.tesco.disco.browse.integration;

import com.tesco.disco.browse.integration.utils.TestingUtilities;
import com.tesco.disco.browse.integration.utils.Utils;
import com.tesco.disco.browse.model.enumerations.IndicesEnum;
import com.tesco.disco.browse.utis.ConfigurationUtils;
import com.tesco.search.commons.util.EmbeddedElasticsearchServer;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class AbstractElasticsearchTestVerticle extends AbstractVerticle {
    private static EmbeddedElasticsearchServer embeddedElasticsearchServer;

    public void startEmbeddedElasticsearchServer() {
        embeddedElasticsearchServer = new EmbeddedElasticsearchServer();
    }

    public static void shutdownEmbeddedElasticsearchServer() {
        embeddedElasticsearchServer.shutdown();
    }

    /**
     * By using this method you can access the embedded server.
     */
    protected Client getClient() {
        return embeddedElasticsearchServer.getClient();
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    //TODO Should really use next available port if it's already taken
    protected int PORT = 9003;

    @Override
    public void start(Future<Void> startedResult) {
        System.setProperty("CONFIG_ENV", "local");
        System.setProperty("es.local", "false");
        System.setProperty(ConfigurationUtils.ENV_PROPERTY_NAME, "test");
        try {
            vertx.executeBlockingObservable(handle -> {
                try {
                    startEmbeddedElasticsearchServer();
                    createIndexes();
                    getClient().prepareSearch().setIndices("ghs.products").execute().get().toString();
                } catch (IOException | InterruptedException | ExecutionException e) {
                    handle.fail(e);
                }
                handle.complete();
                startedResult.complete();
            });

        } catch (Exception e) {
            logger.info(e.getMessage());
            logger.info(e.getStackTrace().toString());
            startedResult.fail(new RuntimeException("Failed to create search index."));
        }
    }

    public void createIndexes() throws IOException, InterruptedException, ExecutionException {
        Utils.touch("synonyms/synonyms.txt");

        createIndex("src/test/resources/taxonomyMapping/taxonomyMapping.json", "ghs.taxonomy", "taxonomy");
        createIndex("src/test/resources/productsMapping/productsMapping.json", "ghs.products", "product");

        // load data - no need to separate bulk requests
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.refresh(true);
        loadData(bulkRequest, "ghs.taxonomy", "taxonomy", "taxonomyData/taxonomy.json");

        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.taxonomy.browse.default").source(
                TestingUtilities.buildQuery("src/test/resources/taxonomyTemplate/", "ghs.taxonomy.browse.default")));

        getClient().bulk(bulkRequest).get();

        bulkRequest = new BulkRequest();
        bulkRequest.refresh(true);
        loadData(bulkRequest, "ghs.products", "product", "productsData/products.json");

        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.products.browse.default").source(
                TestingUtilities.buildQuery("src/test/resources/productsTemplate/", "ghs.products.browse.default")));

        getClient().bulk(bulkRequest).get();
        logger.info("Loaded data into Elasticsearch");
    }

    public void createIndex(String mappingPath, String indexName, String indexType) throws IOException {
        final CreateIndexRequestBuilder createIndexRequestBuilder = getClient().admin().indices().prepareCreate(indexName);

        JsonObject schema = Utils.getJsonFile(mappingPath);

        if (schema.containsKey("indexers") && schema.getJsonObject("indexers").containsKey("settings")) {
            JsonObject settings = schema.getJsonObject("indexers").getJsonObject("settings");
            createIndexRequestBuilder.setSettings(settings.encodePrettily());
        }

        // apply mappings
        if (schema.containsKey("mappings")) {
            JsonObject mappings = new JsonObject().put("mappings", schema.getJsonObject("mappings"));
            createIndexRequestBuilder.addMapping(indexType, mappings.getJsonObject("mappings").getJsonObject(indexType).encode());
        } else if (schema.getJsonObject("indexers").containsKey("mappings") && schema.getJsonObject("indexers")
                .getJsonObject("mappings").containsKey(schema.getJsonObject("indexers").getString("type"))) {
            JsonObject mappings = new JsonObject().put("mappings", schema.getJsonObject("indexers").getJsonObject("mappings"));
            String type = schema.getJsonObject("indexers").getString("type");
            createIndexRequestBuilder.addMapping(type, mappings.getJsonObject("mappings").getJsonObject(type).encode());
        }

        try {
            createIndexRequestBuilder.execute().actionGet();
        } catch (org.elasticsearch.indices.IndexAlreadyExistsException e) {
            //index already exists - not picked up because an alias for it doesn't exist. re-increment and try again.
            logger.warn(e.getMessage());
        }
    }

    public BulkRequest loadData(BulkRequest bulkRequest, String index, String indexType, String productsDataPath) throws IOException, InterruptedException, ExecutionException {
        JsonArray data = new JsonArray(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource(productsDataPath)));
        data.forEach(entry -> {
            JsonObject doc = new JsonObject(Json.encode(entry));
            String id = doc.getString("id");
            doc.remove("id");
            bulkRequest.add(new IndexRequest(index, indexType, id).source(doc.encode()));
        });
        return bulkRequest;
    }

    @Override
    public void stop() {
        shutdownEmbeddedElasticsearchServer();
    }
}
