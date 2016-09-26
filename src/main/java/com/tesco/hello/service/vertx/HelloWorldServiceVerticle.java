package com.tesco.hello.service.vertx;

import com.tesco.hello.service.context.HelloWorldServiceContext;
import com.tesco.hello.utils.ContextDelegator;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Created by bruno on 20/04/16.
 */
public class HelloWorldServiceVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldServiceVerticle.class);
    private static final Marker MARKER = MarkerFactory.getMarker("VERTICLE");
    @Override
    public void start() throws Exception {
        LOGGER.info(MARKER, "Starting test service verticle");
        HelloWorldServiceContext context = ContextDelegator.getInstance().getContext(vertx,
                vertx.getOrCreateContext().config());
    }
}
