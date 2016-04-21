package com.tesco.disco.browse.unit;

import com.tesco.disco.browse.utis.ConfigurationUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by bruno on 21/04/16.
 */
public class ConfigurationLoaderTest {
    private static Vertx vertx;

    @BeforeClass
    public static void setup() {
        vertx = Vertx.vertx();
    }

    @Test
    public void testFetchingConfiguration() {
        JsonObject config = ConfigurationUtils.getConfig(vertx).toBlocking().first();
        Assert.assertNotNull(config);
    }
}