package com.tesco.hello.utils;


import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by bruno on 21/04/16.
 */
public class ContextDelegator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextDelegator.class);
    private static final Marker MARKER = MarkerFactory.getMarker("COMMONS-CONTEXT");
    private static ContextDelegator INSTANCE = null;

    private ContextDelegator() {
    }

    //Passing both the config and vertx instance separately, because it is quite useful for test contexts
    public static ContextDelegator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ContextDelegator();
        }
        return INSTANCE;
    }

    public <T> T getContext(Vertx vertx) {
        return getContext(vertx, null, null);
    }

    public <T> T getContext(Vertx vertx, JsonObject config) {
        return getContext(vertx, config, null);
    }

    public <T> T getContext(Vertx vertx, JsonObject config, Future future) {
        if (config == null) {
            config = vertx.getOrCreateContext().config();
        }
        String context = config.getString("context");
        if(StringUtils.isBlank(context) || vertx == null) {
            LOGGER.error(MARKER, "Error loading configuration context - unable to start verticle");
            throw new RuntimeException("Error loading context from verticle configuration");
        }
        try {
            if (future == null && Class.<T>forName(context).getAnnotation(Context.class) != null) {
                return (T) Class.<T>forName(context).getConstructor(Vertx.class, JsonObject.class).newInstance(vertx, config);
            } else {
                return (T) Class.<T>forName(context).getConstructor(Vertx.class, JsonObject.class, Future.class).newInstance(vertx, config, future);
            }
        } catch (InstantiationException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(MARKER, "[] Error loading class - unable to start verticle - exiting", e);
            throw new RuntimeException("Error loading context from verticle configuration", e);
        }
    }
}