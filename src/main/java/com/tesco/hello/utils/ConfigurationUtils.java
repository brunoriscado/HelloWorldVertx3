package com.tesco.hello.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rx.Observable;

/**
 * Created by bruno on 20/04/16.
 */
public class ConfigurationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtils.class);
    private static final Marker MARKER = MarkerFactory.getMarker("UTIL");

    public static final String CONF_PROPERTY_NAME = "CONFIG";

    public static Observable<JsonObject> getConfig(Vertx vertx) {
        String conf = System.getProperty(CONF_PROPERTY_NAME);
        LOGGER.info(MARKER, "fetching configuration from json file: {}", conf);
        return vertx.fileSystem()
                .readFileObservable(conf)
                .map(fileBuffer -> {
                    return new JsonObject(fileBuffer.toString());
                });
    }
}