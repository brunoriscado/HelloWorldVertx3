package com.tesco.disco.browse.service;

import com.tesco.disco.browse.service.BrowseService;
import com.tesco.disco.browse.service.elasticsearch.ElasticSearchManager;

/**
 * Created by bruno on 21/04/16.
 */
public interface BrowseServiceContext {
    public BrowseService getBrowseService();
    public ElasticSearchManager getElasticSearchManager();
    public void setElasticSearchManager(ElasticSearchManager elasticSearchManager);
}
