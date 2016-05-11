package com.tesco.disco.browse.controller.context;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.service.BrowseService;
import io.vertx.rxjava.ext.web.Router;

/**
 * Created by bruno on 21/04/16.
 */
public interface BrowseAPIContext {
    public BrowseService getBrowseService();
    public BrowseController getBrowseController();
    public Router getRouter();
    public void setRouter(Router router);
}
