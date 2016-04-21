package com.tesco.disco.browse.service.elasticsearch;

import io.vertx.core.json.JsonObject;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/16.
 */
public class ElasticSearchManager {
    public static ElasticSearchManager INSTANCE;
    private Settings settings;
    private TransportClient client;
    private static List<JsonObject> transportAddresses;

    public static ElasticSearchManager getINSTANCE(JsonObject esConfig) {
        if (INSTANCE == null) {
            transportAddresses = esConfig.getJsonArray("transport.addresses") != null ?
                    esConfig.getJsonArray("transport.addresses").getList() : new ArrayList<JsonObject>();
            INSTANCE = new ElasticSearchManager(esConfig);
        }
        return INSTANCE;
    }

    private ElasticSearchManager (JsonObject esConfig) {
        setup(esConfig);
    }

    public void setup(JsonObject esConfig) {
        settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", esConfig.getString("cluster.name"))
                .put("client.transport.sniff", esConfig.getBoolean("client.transport.sniff"))
                .build();
    }

    public TransportClient getElasticsearchClient() {
        client = new TransportClient(settings);
        transportAddresses.forEach(address -> {
            client.addTransportAddress(new InetSocketTransportAddress(
                    address.getString(address.getString("hostname")),
                    address.getInteger("port")));
        });
        if (client.transportAddresses().isEmpty()) {
            client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        }
        return client;
    }
}
