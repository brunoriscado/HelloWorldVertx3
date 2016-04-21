package com.tesco.disco.browse.service.vertx;

import com.tesco.disco.browse.service.BrowseServiceContext;
import com.tesco.disco.browse.service.BrowseServiceContextImpl;
import io.vertx.rxjava.core.AbstractVerticle;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        BrowseServiceContext context = new BrowseServiceContextImpl(vertx);
    }
}
