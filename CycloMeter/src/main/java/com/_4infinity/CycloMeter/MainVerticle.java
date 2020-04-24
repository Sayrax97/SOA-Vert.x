package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.ArrayList;

public class MainVerticle {

  public static void main(String[] args) {
    Vertx vertx= Vertx.vertx();
    EventBus eb=vertx.eventBus();

    HttpServer server= vertx.createHttpServer();

    Router router =Router.router(vertx);

    router.get("/example").handler(routingContext -> {
      routingContext.response()
        .putHeader("content-type","application/json")
        .setChunked(true)
        .write("yay").end();
    });
    router.get("/example/:id").handler(routingContext -> {
      int id=Integer.parseInt(routingContext.request().getParam("id"));
      JsonObject result=new JsonObject();
      result.put("Message","Error 404: Not Found");
        routingContext.fail(418,new Throwable());
      routingContext.response()
        .putHeader("content-type","application/json")
        .setChunked(true)
        .write(result.encodePrettily()).end();
    });

    router.route().handler(routingContext->{
      routingContext.response()
        .putHeader("content-type","text/plain").end("Hello from the other side");
    });



    server.requestHandler(router).listen(5555);
  }


}
