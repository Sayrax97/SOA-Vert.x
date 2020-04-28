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

    //region GetUserByUsername
    router.get("/user/username/:username").handler(req->{
      String username=req.request().getParam("username");
      JsonObject message=new JsonObject();
      message.put("username",username);
      if(username=="")
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

    //region PostSensorData
    router.post("/sensor/data").handler(req->{
      //(speed,incline,terain_type,heart_rate,senzor_id,time_stemp,distance_traveled)
      JsonObject data=req.getBodyAsJson();
      if(data.getFloat("speed")==null || data.getInteger("incline")==null || data.getString("terain_type")==null ||
         data.getInteger("heart_rate")==null || data.getInteger("senzor_id")==null || data.getInteger("distance_traveled")==null){
        req.response().setStatusCode(400).end("Some sensor data parameters missing");
      }
      else
      eventBus.request("data.base.postSensorData",data,response->{
        req.response().end(response.result().body().toString());
      });
    });
    //endregion

    //region GetSensorDataAll
    router.get("/sensor/data/all/:id").handler(req->{
      int id=Integer.parseInt(req.request().getParam("id"));
      JsonObject message=new JsonObject();
      message.put("id",id);
      eventBus.request("data.base.GetSensorDataAll",message,response->{
        req.response().putHeader("content-type","application/json").setChunked(true).write(response.result().body().toString()).end();
      });
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
      if(sensor.getInteger("user_id")==null )
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

    //region getMET
    router.get("/MET/:speed").handler(req->{
      Double speed=Double.parseDouble( req.request().getParam("speed"));
      JsonObject message= new JsonObject();
      message.put("speed",speed);
      if(speed>=0)
        eventBus.request("data.base.getMET",message,response->{
          req.response().putHeader("content-type","application/json").setChunked(true).
            write(response.result().body().toString()).end();
        });
      else {
        req.fail(400,new Throwable());
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
