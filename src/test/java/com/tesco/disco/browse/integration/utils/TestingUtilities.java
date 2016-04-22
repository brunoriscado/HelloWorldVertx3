package com.tesco.disco.browse.integration.utils;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Christo on 06/05/15.
 */
public class TestingUtilities {

    // TODO don't generate these here - they're baked into scm. decouple into a separate test loader if required
    public void createDocumentFilesTest() throws IOException, InterruptedException, ExecutionException {
        createDocumentFiles();
    }

    public static void createDocumentFiles() throws IOException, InterruptedException, ExecutionException {

        final Logger logger = LoggerFactory.getLogger(Utils.class);

        JsonObject data = Utils.getJsonFile("src/test/resources/ghs.products.json");
        Integer total = data.getJsonObject("hits").getJsonArray("hits").size();
        for (int i=0; i < total; i++) {
            JsonObject document = data.getJsonObject("hits").getJsonArray("hits").getJsonObject(i);
            String fileName = "src/test/resources/ghs.products/" + document.getString("_id") + ".json";

            logger.debug("Filename: " + fileName);

            writeFile(fileName, document.getJsonObject("_source").encodePrettily());
            //bulkRequest.add(new IndexRequest("ghs.products", "product", document.getString("_id")).source(document.getObject("_source")));
        }

        //getClient().bulk(bulkRequest).get();
        logger.debug("Created data files");
    }

    public static void writeFile(String filename, String content) {

        File file = new File(filename);

        // if file doesnt exists, then create it
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String buildQuery(String name) {
        try {
            String query = Utils.getJsonFile("src/test/resources/taxonomyTemplate/" + name + ".json").encodePrettily();
            String template = fetchQueryTemplate(name);
            template = StringEscapeUtils.escapeJava(template);
            return query.replace("{{template}}", template);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fetchQueryTemplate(String name) {
        try {
            return Utils.getFileContents("src/test/resources/taxonomyTemplate/" + name + ".json.template");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
