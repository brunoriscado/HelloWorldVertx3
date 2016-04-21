package com.tesco.disco.browse.integration;

import com.tesco.disco.browse.integration.utils.TestingUtilities;
import com.tesco.disco.browse.integration.utils.Utils;
import com.tesco.disco.browse.utis.ConfigurationUtils;
import com.tesco.search.commons.util.EmbeddedElasticsearchServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.fail;

public abstract class AbstractElasticsearchTestVerticle extends AbstractVerticle {
    private static EmbeddedElasticsearchServer embeddedElasticsearchServer;

    public static void startEmbeddedElasticsearchServer() {
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

    protected final Handler<Throwable> handler = new Handler<Throwable>(){
        @Override
        public void handle(Throwable event) {
            fail("Application failed to start.\n" + event.getMessage());
        }
    };

    @BeforeClass
    public static void start_embedded_elasticsearch() {
        System.setProperty("CONFIG_ENV", "local");
        System.setProperty("es.local", "false");

        startEmbeddedElasticsearchServer();
    }

    @Override
    public void start(Future<Void> startedResult) {
        // use embedded elasticsearch test config for tests
        System.setProperty(ConfigurationUtils.ENV_PROPERTY_NAME, "test");

        vertx.deployVerticle(AbstractElasticsearchTestVerticle.class.getName(), new AsyncResultHandler<String>() {
            public void handle(AsyncResult<String> ar) {
                if (ar.succeeded()) {
                    logger.info("Starting Tests");
                    initialize();
                    startedResult.complete();
                } else {
                    ar.cause().printStackTrace();
                    fail("Deployment failed.");
                }
            }
        });
    }

    public void initialize(){
        try {
            createIndexes();
        } catch (Exception e) {
            logger.info(e.getMessage());
            logger.info(e.getStackTrace().toString());
            throw new RuntimeException("Failed to create search index.");
        }
    }

    public void createIndexes() throws IOException, InterruptedException, ExecutionException {
        createIndex("ghs.taxonomy");

        // load data
        BulkRequest bulkRequest = new BulkRequest();
        loadData(bulkRequest, "ghs.taxonomy");

        // load query
        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.products.default").source(TestingUtilities.buildQuery("ghs.products.default")));

        // load secondary query
        bulkRequest.add(new IndexRequest(".scripts", "mustache", "ghs.products.default-secondary").source(TestingUtilities.buildQuery("ghs.products.default")));

        getClient().bulk(bulkRequest).get();
        logger.info("Loaded data into Elasticsearch");
    }

    public void createIndex(String indexName) throws IOException {
        final CreateIndexRequestBuilder createIndexRequestBuilder = getClient().admin().indices().prepareCreate(indexName);
        JsonObject schema = Utils.getJsonFile("src/test/resources/taxonomyMapping/taxonomyMapping.json");
        // apply settings
        if (schema.getJsonObject("indexers").containsKey("settings")) {
            JsonObject settings = schema.getJsonObject("indexers").getJsonObject("settings");
            createIndexRequestBuilder.setSettings(settings.encodePrettily());
        }

        // apply mappings
        if (schema.getJsonObject("indexers").containsKey("mappings") &&
                schema.getJsonObject("indexers").getJsonObject("mappings").containsKey(schema.getJsonObject("indexers").getString("type"))) {
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

    public BulkRequest loadData(BulkRequest bulkRequest, String index) throws IOException, InterruptedException, ExecutionException {

        String dataPath = "src/test/resources/indexData/";
        String[] files = Utils.getFiles(dataPath);
        Integer productLength = files.length;

        // to test a different index, change the size of it by half
        if(index!="ghs.taxonomy"){ productLength=(files.length / 2); }

        for (Integer i=0; i < productLength; i++) {
            if (!files[i].substring(0,1).equals(".")) {
                try {
                    JsonObject document = Utils.getJsonFile(dataPath + files[i]);
                    bulkRequest.add(new IndexRequest(index, "taxonomy", document.getString("id")).source(document.encode()));
                } catch (Exception e) {
                    logger.warn("Failed to add " + files[i] + "\n" + e.getMessage());
                }
            }
        }
        return bulkRequest;
    }

    @AfterClass
    public static void stop_embedded_elasticsearch() {
        shutdownEmbeddedElasticsearchServer();
    }
}