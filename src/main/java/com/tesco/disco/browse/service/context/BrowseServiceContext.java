package com.tesco.disco.browse.service.context;

import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchClientFactory;

/**
 * Created by bruno on 21/04/16.
 */
public interface BrowseServiceContext {
    public BrowseService getBrowseService();
    public ElasticSearchClientFactory getElasticSearchClientFactory();
}
