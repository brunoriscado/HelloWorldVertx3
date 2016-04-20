package com.tesco.disco.browse.utis;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bruno on 20/04/16.
 */
public class ConfigurationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUtils.class);

    public static final String ENV_PROPERTY_NAME = "CONFIG_ENV";
    public static final String ENV_SEPARATOR_CHAR = "-";
    public static final String APPLICATION_CONFIG_NAME = "application";

    public static JsonObject getConfig(Vertx vertx) {
        String environment = System.getenv(ENV_PROPERTY_NAME);

        LOGGER.info("Environment is : " + environment);

        if (StringUtils.isNotBlank(environment)) {
            String transformedConfigName = APPLICATION_CONFIG_NAME + ENV_SEPARATOR_CHAR + environment;
            vertx.fileSystem().exists("config/" + transformedConfigName + ".json",
                    configHandler -> {
                        if (configHandler.succeeded()) {

                        } else {

                        }
                    });
        } else {
            LOGGER.info("Loading base application config.");
            //LOAD - default
        }



        return new JsonObject();
    }
}
