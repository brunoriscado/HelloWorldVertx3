package com.tesco.disco.browse.integration.browse.controller;

import com.tesco.disco.browse.integration.AbstractElasticsearchTestVerticle;
import com.tesco.disco.browse.integration.browse.BrowseTest;
import io.vertx.rxjava.ext.unit.TestContext;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by bruno on 21/04/16.
 */
public class BrowseControllerTest extends AbstractElasticsearchTestVerticle implements BrowseTest {
    @BeforeClass
    public static void setup() {

    }

    @Test
    public void testGenericBrowse(TestContext testContext) {

    }

    @Test
    public void testBrowseWithSuperDeparmentFilter(TestContext testContext) {

    }

    @Test
    public void testBrowseWithDeparmentFilter(TestContext testContext) {

    }

    @Test
    public void testBrowseWithAisleFilter(TestContext testContext) {

    }

    @Test
    public void testBrowseWithShelfFilter(TestContext testContext) {

    }

    @Test
    public void testIncorrectBrowseEndpoint(TestContext testContext) {

    }

    @Test
    public void testEmptyTaxonomyResponse(TestContext testContext) {

    }

    @Test
    public void testNonExistentFilter(TestContext testContext) {

    }
}
