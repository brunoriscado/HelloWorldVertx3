package com.tesco.disco.browse.service.elasticsearch;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/16.
 */
public class ElasticSearchClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchClientFactory.class);
    private static final Marker MARKER = MarkerFactory.getMarker("FACTORY");
    public static ElasticSearchClientFactory INSTANCE;
    private Settings settings;
    private TransportClient client;
    private static List transportAddresses;

    public static ElasticSearchClientFactory getINSTANCE(JsonObject esConfig) {
        if (INSTANCE == null) {
            transportAddresses = esConfig.getJsonArray("transport.addresses") != null ?
                    esConfig.getJsonArray("transport.addresses").getList() : new ArrayList<JsonObject>();
            INSTANCE = new ElasticSearchClientFactory(esConfig);
        }
        return INSTANCE;
    }

    private ElasticSearchClientFactory(JsonObject esConfig) {
        setup(esConfig);
    }

    public void setup(JsonObject esConfig) {
        LOGGER.info(MARKER, "Setup elasticsearch settings - cluster name: {}", esConfig.getString("cluster.name"));
        settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", esConfig.getString("cluster.name"))
                .put("client.transport.sniff", esConfig.getBoolean("client.transport.sniff"))
                .build();
    }

    public TransportClient getElasticsearchClient() {
        LOGGER.info(MARKER, "Creating Elasticsearch transport client instance");
        client = new TransportClient(settings);
        transportAddresses.forEach(address -> {
            JsonObject jsonAddress = new JsonObject(Json.encode(address));
            client.addTransportAddress(new InetSocketTransportAddress(
                    jsonAddress.getString("hostname"),
                    jsonAddress.getInteger("port")));
        });
        if (client.transportAddresses().isEmpty()) {
            client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        }
        return client;
    }
}
