package com.tesco.disco.browse.api;

import com.tesco.disco.browse.controller.BrowseController;
import com.tesco.disco.browse.service.BrowseService;

/**
 * Created by bruno on 21/04/16.
 */
public interface BrowseAPIContext {
    public BrowseService getBrowseService();
    public BrowseController getBrowseController();
}
