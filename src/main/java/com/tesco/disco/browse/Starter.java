package com.tesco.disco.browse;

import com.tesco.disco.browse.utils.ConfigurationUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rx.Observable;

/**
 * Created by bruno on 21/04/16.
 */
public class Starter extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class.getName());
    private static final Marker MARKER = MarkerFactory.getMarker("VERTICLE");

    public void start() throws Exception {
        LOGGER.info(MARKER, "Starter verticle initiated - deploying app verticles");
        ConfigurationUtils.getConfig(vertx)
                .flatMap(config -> {
                    return Observable.from(config.getJsonArray("verticles").getList());
                })
                .flatMap(verticleConfig -> {
                    JsonObject verticle = new JsonObject(Json.encode(verticleConfig));
                    return vertx.deployVerticleObservable(verticle.getString("main"),
                            new DeploymentOptions(verticle.getJsonObject("options")));
                })
                .subscribe(deploymentID -> {
                            LOGGER.info(MARKER, "Verticle: {} as been deployed", deploymentID);
                        },
                        error -> {
                            LOGGER.error(MARKER, "Failed to deploy Verticle: {}", error);
                        });
    }
}
