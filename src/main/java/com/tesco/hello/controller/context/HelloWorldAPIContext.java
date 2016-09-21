package com.tesco.hello.controller.context;

import com.tesco.hello.controller.HelloWorldController;
import com.tesco.hello.service.rxjava.HelloWorldService;
import io.vertx.rxjava.ext.web.Router;

/**
 * Created by bruno on 21/04/16.
 */
public interface HelloWorldAPIContext {
    public HelloWorldService getHelloWorldService();
    public HelloWorldController getHelloWorldController();
    public Router getRouter();
    public void setRouter(Router router);
}
