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
        System.setProperty(ConfigurationUtils.ENV_PROPERTY_NAME, "test");
        try {
            vertx.executeBlockingObservable(handle -> {
                try {
                    startEmbeddedElasticsearchServer();
                    createIndexes();
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
        createIndex("src/test/resources/taxonomyMapping/taxonomyMapping.json", "ghs.taxonomy", "taxonomy");
        createIndex("src/test/resources/productsMapping/productsMapping.json", "ghs.products", "products");

        // load data
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.refresh(true);
        loadData(bulkRequest, "ghs.taxonomy", "taxonomy", "taxonomyData/taxonomy.json");
        loadSplitData(bulkRequest, "ghs.products", "products", "src/test/resources/productsData");

        // load query
        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.taxonomy.default").source(
                TestingUtilities.buildQuery("src/test/resources/taxonomyTemplate/", "ghs.taxonomy.default")));
        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.products.default").source(
                TestingUtilities.buildQuery("src/test/resources/productsTemplate/", "ghs.products.default")));

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

    public BulkRequest loadSplitData(BulkRequest bulkRequest, String index, String indexType, String productsDataPath) throws IOException, InterruptedException, ExecutionException {


        String[] files = Utils.getFiles(productsDataPath);
        Integer productLength = files.length;

        // to test a different index, change the size of it by half
        if(!index.equals(index)){ productLength=(files.length / 2); }

        for (Integer i=0; i < productLength; i++) {
            if (!files[i].substring(0,1).equals(".")) {
                try {
                    JsonObject document = Utils.getJsonFile(productsDataPath + "/" + files[i]);
                    bulkRequest.add(new IndexRequest(index, indexType, document.getString("id")).source(document.encode()));
                } catch (Exception e) {
                    logger.warn("Failed to add " + files[i] + "\n" + e.getMessage());
                }
            }
        }
        return bulkRequest;
    }

    @Override
    public void stop() {
        shutdownEmbeddedElasticsearchServer();
    }
}