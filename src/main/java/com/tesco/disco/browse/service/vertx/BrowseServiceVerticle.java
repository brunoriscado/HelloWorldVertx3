package com.tesco.disco.browse.service.vertx;

import com.tesco.disco.browse.controller.context.BrowseAPIContext;
import com.tesco.disco.browse.service.context.BrowseServiceContext;
import com.tesco.disco.browse.service.context.BrowseServiceContextImpl;
import com.tesco.search.commons.context.ContextDelegator;
import io.vertx.rxjava.core.AbstractVerticle;

/**
 * Created by bruno on 20/04/16.
 */
public class BrowseServiceVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        BrowseServiceContext context = ContextDelegator.getInstance().getContext(vertx,
                vertx.getOrCreateContext().config());
    }
}
