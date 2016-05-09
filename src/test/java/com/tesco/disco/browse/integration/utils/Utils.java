package com.tesco.disco.browse.integration.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;

/**
 * Created by bruno on 20/04/16.
 */
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private Vertx vertx;

    public Utils(Vertx vertx) {
        vertx = vertx;
    }

    public static String getFileContents(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
    }

    public static JsonObject getJsonFile(String fileName) throws IOException {
        return new JsonObject(getFileContents(fileName));
    }

    public static void touch(String fileName) {
        File file = Paths.get(fileName).toFile();
        long timestamp = new Timestamp(new java.util.Date().getTime()).getTime();
        try {
            if (!file.exists())
                new FileOutputStream(file).close();
            file.setLastModified(timestamp);
        }
        catch (IOException e) {
            LOGGER.error("Error occured touching the files: {}", e);
        }
    }

    public static String[] getFiles(String path) {
        File file = new File(path);
        String[] files = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        return files;
    }

    public Observable<String> getFilesRx(String path) {
        return vertx.fileSystem().readDirObservable(path)
                .flatMap(files -> {
                    return Observable.from(files);
                })
                .filter(file-> {
                    return new File(file).isFile();
                });
    }
}
