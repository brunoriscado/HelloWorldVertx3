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

    public static final String ENV_PROPERTY_NAME = "CONFIG_ENV";
    public static final String ENV_SEPARATOR_CHAR = "-";
    public static final String APPLICATION_CONFIG_NAME = "application";

    public static Observable<JsonObject> getConfig(Vertx vertx) {
        String environment = System.getProperty(ENV_PROPERTY_NAME);
        LOGGER.info(MARKER, "fetching configuration from json file on environment: {}", environment);
        JsonObject config = new JsonObject();
        LOGGER.info("Environment is : " + environment);
        String transformedConfigName;
        if (StringUtils.isNotBlank(environment)) {
            transformedConfigName = APPLICATION_CONFIG_NAME + ENV_SEPARATOR_CHAR + environment;
        } else {
            LOGGER.info("Loading base application config.");
            transformedConfigName = APPLICATION_CONFIG_NAME;
        }
        return vertx.fileSystem()
                .readFileObservable("config/" + transformedConfigName + ".json")
                .map(fileBuffer -> {
                    return new JsonObject(fileBuffer.toString());
                });
    }
}
