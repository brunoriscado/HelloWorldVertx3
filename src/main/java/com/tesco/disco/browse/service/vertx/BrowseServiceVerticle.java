package com.tesco.disco.browse.service.vertx;

import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.disco.browse.utils.ContextDelegator;
import io.vertx.rxjava.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseServiceVerticle.class);
    private static final Marker MARKER = MarkerFactory.getMarker("VERTICLE");
    @Override
    public void start() throws Exception {
        LOGGER.info(MARKER, "Starting browse service verticle");
        BrowseServiceContext context = ContextDelegator.getInstance().getContext(vertx,
                vertx.getOrCreateContext().config());
    }
}
