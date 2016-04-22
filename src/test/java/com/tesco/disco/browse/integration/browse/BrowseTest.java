package com.tesco.disco.browse.integration.browse;

import io.vertx.ext.unit.TestContext;

/**
	* Created by bruno on 22/04/16.
	*/
public interface BrowseTest {
				public void testGenericBrowse(TestContext testContext);
				public void testBrowseWithSuperDeparmentFilter(TestContext testContext);
				public void testBrowseWithDeparmentFilter(TestContext testContext);
				public void testBrowseWithAisleFilter(TestContext testContext);
				public void testBrowseWithShelfFilter(TestContext testContext);
				public void testEmptyTaxonomyResponse(TestContext testContext);
				public void testNonExistentFilter(TestContext testContext);
}
