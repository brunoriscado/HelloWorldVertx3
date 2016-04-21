package com.tesco.disco.browse;

import com.tesco.disco.browse.utis.ConfigurationUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 21/04/16.
 */
public class Starter extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class.getName());

    public void start() throws Exception {
        ConfigurationUtils.getConfig(vertx)
                .flatMap(config -> {
//                    return Observable.<JsonObject>from((List<>)Json.decodeValue(verticles.encode(), List.class));
                    return Observable.from(config.getJsonArray("verticles").getList());
                })
                .flatMap(v -> {
                    JsonObject verticle = new JsonObject(Json.encode(v));
                    return vertx.deployVerticleObservable(verticle.getString("main"),
                            new DeploymentOptions(verticle.getJsonObject("options")));
                })
                .subscribe(deploymentID -> {
                            LOGGER.info("Verticle: {} as been deployed", deploymentID);
                        },
                        error -> {
//                            LOGGER.error("Failed to deploy Verticle: {}", error.getMessage());
                        });
    }
}
