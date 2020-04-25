package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class ServiceVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    EventBus eventBus=vertx.eventBus();

    router.get("/user/:id").handler(req->{
      int id=Integer.parseInt(req.request().getParam("id"));
      JsonObject message=new JsonObject();
      message.put("id",id);
      if(id<=0)
        req.fail(404,new Throwable());
      eventBus.request("data.base.getUser", message, response -> {
        req.response().putHeader("content-type","application/json")
          .setChunked(true)
          .write(response.result().body().toString()).end();
      });
    });

    /*router.post("/user").handler(event -> {

    });*/

    router.route().handler(request -> {
      request.response().end("CycloMeter Service API");
    });
    server.requestHandler(router).listen(1998, ar -> {
      if (ar.succeeded()) {
        System.out.println("CycloMeter Service running");
      } else {
        System.out.println("CycloMeter Service failed");
      }
    });

  }

  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
