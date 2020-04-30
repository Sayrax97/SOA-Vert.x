package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class ServiceVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.PUT)
      .allowedMethod(HttpMethod.OPTIONS)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Content-Type"));
    router.route().handler(BodyHandler.create());
    EventBus eventBus=vertx.eventBus();

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
        JsonObject message=(JsonObject) ar.result().body();
        if(message.getInteger("statusCode")==400)
          req.fail(400);
        else if(message.getInteger("statusCode")==500)
          req.fail(500);
        else if(message.getInteger("statusCode")==200)
        req.response()
          .setChunked(true).write(message.encodePrettily())
          .end();
      });
      //req.response().setChunked(true).write(user.encodePrettily()).end();
    }).failureHandler(failureRoutingContext->{
      int statusCode = failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("User already exists");
      else
        failureRoutingContext.response().setStatusCode(statusCode).end("Creating user failed");
    })   ;
    //endregion

    //region GetUser
    router.get("/user/:id").handler(req->{
      int id=Integer.parseInt(req.request().getParam("id"));
      JsonObject message=new JsonObject();
      message.put("id",id);
      if(id<=0) {
        req.fail(400,new Throwable());
      }
      eventBus.request("data.base.getUser", message, response -> {
        JsonObject msg=(JsonObject) response.result().body();
        if(msg.getInteger("statusCode")==404)
          req.fail(404);
        else if(msg.getInteger("statusCode")==400)
          req.fail(400);
        else if(msg.getInteger("statusCode")==200)
          req.response().putHeader("content-type","application/json")
            .setChunked(true).write(msg.encodePrettily())
            .end();
      });
    }).failureHandler(failureRoutingContext -> {
      int statusCode = failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid id");
      else if(statusCode==404)
        failureRoutingContext.response().setStatusCode(statusCode).end("No user with such id found");

    });
    //endregion

    //region GetUserByUsername
    router.get("/user/login/:username").handler(req->{
      String username=req.request().getParam("username");
      JsonObject message=new JsonObject();
      message.put("username",username);
      if(username.equals("")) {
        req.fail(400,new Throwable());
      }
      eventBus.request("data.base.getUserByUsername", message, response -> {
        JsonObject msg=(JsonObject) response.result().body();
        if(msg.getInteger("statusCode")==404)
          req.fail(404);
        else if(msg.getInteger("statusCode")==400)
          req.fail(400);
        else if(msg.getInteger("statusCode")==200)
          req.response().putHeader("content-type","application/json")
            .setChunked(true).write(msg.encodePrettily())
            .end();
      });
    }).failureHandler(failureRoutingContext -> {
      int statusCode = failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid username");
      else if(statusCode==404)
        failureRoutingContext.response().setStatusCode(statusCode).end("No user with such username found");

    });
    //endregion


    //region PostSensorData
    router.post("/sensor/data").handler(req->{
      //(speed,incline,terain_type,heart_rate,senzor_id,time_stemp,distance_traveled)
      JsonObject data=req.getBodyAsJson();
      if(data.getFloat("speed")==null || data.getBoolean("incline")==null || data.getString("terrain_type")==null ||
         data.getInteger("heart_rate")==null || data.getInteger("senzor_id")==null || data.getInteger("distance_traveled")==null){
        req.response().setStatusCode(400).end("Some sensor data parameters missing");
      }
      else{
      eventBus.request("data.base.postSensorData",data,response->{
        JsonObject msg=(JsonObject) response.result().body();
        System.out.println(msg.encodePrettily());
        if(msg.getInteger("statusCode")==400)
          req.fail(400);
        else {
          req.response().setChunked(true).write(msg.encodePrettily()).end();
        }
      });
      }
    }).failureHandler(failureRoutingContext -> {
      int statusCode = failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, missing data");
    });
    //endregion

    //region GetSensorDataAll
    router.get("/sensor/data/all/:id").handler(req->{
      int id=Integer.parseInt(req.request().getParam("id"));
      JsonObject message=new JsonObject();
      if(id<=0){
        req.fail(400);
      }
      message.put("id",id);
      eventBus.request("data.base.GetSensorDataAll",message,response->{
        JsonObject msg=(JsonObject) response.result().body();
        if(msg.getInteger("statusCode")==400)
          req.fail(400);
        else if(msg.getInteger("statusCode")==404)
          req.fail(404);
        else
        req.response().putHeader("content-type","application/json").
          setChunked(true).write(response.result().body().toString()).end();
      });
    }).failureHandler(failureRoutingContext -> {
      int statusCode=failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid id");
      else
        failureRoutingContext.response().setStatusCode(statusCode).end("No senzor data with that id found");
    });
    //endregion

    //region GetAllSensor
    router.get("/sensor/all/:id").handler(req->{
      int id=Integer.parseInt(req.request().getParam("id"));
      JsonObject message=new JsonObject();
      if(id<=0){
        req.fail(400);
      }
      message.put("id",id);
      eventBus.request("data.base.GetSensorAll",message,response->{
        JsonObject msg=(JsonObject) response.result().body();
        System.out.println("+");
        if(msg.getInteger("statusCode")==400)
          req.fail(400);
        else if(msg.getInteger("statusCode")==404)
          req.fail(404);
        else
          req.response().putHeader("content-type","application/json").
            setChunked(true).write(response.result().body().toString()).end();
      });
    }).failureHandler(failureRoutingContext -> {
      int statusCode=failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid id");
      else
        failureRoutingContext.response().setStatusCode(statusCode).end("No senzor data with that id found");
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
          JsonObject msg=(JsonObject) response.result().body();
          if(msg.getInteger("statusCode")==400)
            req.fail(400);
          else if(msg.getInteger("statusCode")==404)
            req.fail(404);
          else {
            req.response().putHeader("content-type", "application/json").setChunked(true).
              write(response.result().body().toString()).end();
          }
        });
      else {
        req.fail(400,new Throwable());
      }
    }).failureHandler(failureRoutingContext -> {
      int statusCode=failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid id");
      else
        failureRoutingContext.response().setStatusCode(statusCode).end("No senzor with that id found");
    });
    //endregion

    //region postSensor
    router.post("/sensor").handler(req->{
      JsonObject sensor=req.getBodyAsJson();
      System.out.println(sensor.encodePrettily());
      if(sensor.getInteger("user_id")==null )
        req.fail(400,new Throwable());
      else {
        eventBus.request("data.base.postSensor", sensor, response -> {
          JsonObject msg=(JsonObject) response.result().body();
          if (msg.getInteger("statusCode") == 400)
            req.fail(400);
          else {
            req.response().putHeader("content-type", "application/json").setChunked(true).
              write(response.result().body().toString()).end();
          }
        });
      }
      }).failureHandler(failureRoutingContext -> {
      int statusCode=failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid user id");
    });
    //endregion

    //region getMET
    router.get("/MET/:speed").handler(req->{
      Double speed=Double.parseDouble( req.request().getParam("speed"));
      JsonObject message= new JsonObject();
      message.put("speed",speed);
      if(speed>=0)
        eventBus.request("data.base.getMET",message,response->{
          JsonObject msg=(JsonObject) response.result().body();
          if(msg.getInteger("statusCode")==400)
            req.fail(400);
          else if(msg.getInteger("statusCode")==404)
            req.fail(404);
          else
          req.response().putHeader("content-type","application/json").setChunked(true).
            write(response.result().body().toString()).end();
        });
      else {
        req.fail(400,new Throwable());
      }
    }).failureHandler(failureRoutingContext -> {
      int statusCode=failureRoutingContext.statusCode();
      if(statusCode==400)
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid speed");
      else{
        failureRoutingContext.response().setStatusCode(statusCode).end("No such MET found");
      }
    });;
    //endregion

    //region PutSensor
    router.put("/sensor").handler(req->{
      JsonObject sensor=req.getBodyAsJson();
      if(sensor.getInteger("senzor_id")==null )
        req.fail(400,new Throwable());
      else {
        eventBus.request("data.base.putSensor", sensor, response -> {
          JsonObject msg=(JsonObject) response.result().body();
          if(msg.getInteger("statusCode")==400)
            req.fail(400);
          else
          req.response().setChunked(true).write(response.result().body().toString()).end();
        });
      }
    }).failureHandler(failureRoutingContext -> {
      int statusCode=failureRoutingContext.statusCode();
      if(statusCode==400){
        failureRoutingContext.response().setStatusCode(statusCode).end("Bad Request, invalid id");
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
