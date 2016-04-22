package com.tesco.disco.browse.integration;

import com.tesco.disco.browse.integration.utils.TestingUtilities;
import com.tesco.disco.browse.integration.utils.Utils;
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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AbstractElasticsearchTestVerticle extends AbstractVerticle {
    private static EmbeddedElasticsearchServer embeddedElasticsearchServer;

    public void startEmbeddedElasticsearchServer() {
        embeddedElasticsearchServer = new EmbeddedElasticsearchServer();
    }

    public void shutdownEmbeddedElasticsearchServer() {
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
        startEmbeddedElasticsearchServer();
        // use embedded elasticsearch test config for tests
        System.setProperty(ConfigurationUtils.ENV_PROPERTY_NAME, "test");
        try {
            createIndexes();
        } catch (Exception e) {
            logger.info(e.getMessage());
            logger.info(e.getStackTrace().toString());
            startedResult.fail(new RuntimeException("Failed to create search index."));
        }
        startedResult.complete();
    }


    public void createIndexes() throws IOException, InterruptedException, ExecutionException {
        createIndex("ghs.taxonomy");

        // load data
        BulkRequest bulkRequest = new BulkRequest();
        loadData(bulkRequest, "ghs.taxonomy");

        // load query
        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.taxonomy.default").source(TestingUtilities.buildQuery("templatedQuery.mustache")));

        getClient().bulk(bulkRequest).get();
        logger.info("Loaded data into Elasticsearch");
    }

    public void createIndex(String indexName) throws IOException {
        final CreateIndexRequestBuilder createIndexRequestBuilder = getClient().admin().indices().prepareCreate(indexName);

        JsonObject schema = Utils.getJsonFile("src/test/resources/taxonomyMapping/taxonomyMapping.json");

        // apply mappings
        if (schema.containsKey("mappings")) {
            JsonObject mappings = new JsonObject().put("mappings", schema.getJsonObject("mappings"));
            createIndexRequestBuilder.addMapping("taxonomy", mappings.getJsonObject("mappings").getJsonObject("taxonomy").encode());
        }

        try {
            createIndexRequestBuilder.execute().actionGet();
        } catch (org.elasticsearch.indices.IndexAlreadyExistsException e) {
            //index already exists - not picked up because an alias for it doesn't exist. re-increment and try again.
            logger.warn(e.getMessage());
        }
    }

    public BulkRequest loadData(BulkRequest bulkRequest, String index) throws IOException, InterruptedException, ExecutionException {
        JsonArray taxonomy = new JsonArray(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResource("taxonomyData/taxonomy.json")));
        taxonomy.forEach(entry -> {
            JsonObject doc = new JsonObject(Json.encode(entry));
            bulkRequest.add(new IndexRequest(index, "taxonomy", doc.getString("id")).source(doc.encode()));
        });
        return bulkRequest;
    }

    @Override
    public void stop() {
        shutdownEmbeddedElasticsearchServer();
    }
}