package com.tesco.disco.browse;

/**
 * Created by GB90 on 4/20/2016.
 */
public class ConfigManager {
    private static final  Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    public static final String ENV_PROPERTY_NAME = "CONFIG_ENV";
    public static final String ENV_SEPARATOR_CHAR = "-";
    public static final String APPLICATION_CONFIG_NAME = "application";

    public static JsonObject getConfig() {

        ConfigValue configValue = null;

        ConfigRenderOptions jsonRenderOptions = ConfigRenderOptions.concise();

        Config config = ConfigFactory.load();

        String environment = System.getProperty(ENV_PROPERTY_NAME);

        logger.info("Environment is : " + environment);

        if ((null != environment) && (environment.trim().length() > 0) ) {

            String transformedConfigName = APPLICATION_CONFIG_NAME + ENV_SEPARATOR_CHAR + environment;

            Config envConfig = ConfigFactory.load(transformedConfigName);

            Config mergedConfig = envConfig.root().toConfig()
                    .withFallback(config.root().toConfig());

            configValue = mergedConfig.root();

        } else {

            logger.info("Loading base application config.");
            configValue = config.root();
        }

        String json = configValue.render(jsonRenderOptions);

        return new JsonObject(json);

    }

}
