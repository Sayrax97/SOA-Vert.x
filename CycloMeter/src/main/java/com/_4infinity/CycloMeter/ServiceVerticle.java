package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class ServiceVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    EventBus eventBus=vertx.eventBus();

    //region GetUser
    router.get("/user/:id").handler(req->{
      int id=Integer.parseInt(req.request().getParam("id"));
      JsonObject message=new JsonObject();
      message.put("id",id);
      if(id<=0)
        req.fail(404,new Throwable());
      eventBus.request("data.base.getUser", message, response -> {
        req.response().putHeader("content-type","application/json").setChunked(true)
          .write(response.result().body().toString()).end();
      });
    });
    //endregion

    //region PostUser
    router.post("/user").handler(req -> {
      JsonObject user=req.getBodyAsJson();
      if(user.getInteger("weight")==null || user.getString("username")==null || user.getString("gender")==null || user.getInteger("age")==null){
        req.response().setStatusCode(400).end("Some user parameters missing");
      }
      if(!user.getString("gender").contains("M") && !user.getString("gender").contains("F")){
        req.response().setStatusCode(400).end("Gender must be M or F");
      }
      eventBus.request("data.base.postUser",user,ar->{
        req.response().setChunked(true).write(ar.result().body().toString()).end();
      });
      //req.response().setChunked(true).write(user.encodePrettily()).end();
    }).failureHandler(failureRoutingContext->{
      int statusCode = failureRoutingContext.statusCode();
      // Status code will be 500 for the RuntimeException or 403 for the other failure
      failureRoutingContext.response().setStatusCode(statusCode).end("Sorry! Not today");
    });
    //endregion

    //region GetSensor
    router.get("/sensor/:id").handler(req->{
      String paramId=req.request().getParam("id");
      int id=Integer.parseInt(paramId);
      JsonObject message= new JsonObject();
      message.put("id",id);
      if(id>0)
        eventBus.request("data.base.getSensor",message,response->{
          req.response().putHeader("content-type","application/json").setChunked(true).
            write(response.result().body().toString()).end();
        });
      else {
        req.fail(400,new Throwable());
      }
    });
    //endregion

    //region postSensor
    router.post("/sensor").handler(req->{
      JsonObject sensor=req.getBodyAsJson();
      if(sensor.getFloat("total_distance_traveled")==null || sensor.getString("started_at")==null || sensor.getInteger("User_id")==null )
        req.fail(400,new Throwable());
      else {
        System.out.println("request");
        eventBus.request("data.base.postSensor", sensor, response -> {
          System.out.println("response");
          req.response().setChunked(true).write(response.result().body().toString()).end();
        });
      }
    });
    //endregion

    //region Server
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
    //endregion

  }

  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
