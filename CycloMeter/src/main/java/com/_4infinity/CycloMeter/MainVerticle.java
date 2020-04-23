package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import java.util.ArrayList;

public class MainVerticle {

  public static void main(String[] args) {
    Vertx vertx= Vertx.vertx();
    JsonObject objekat1= new JsonObject();
    objekat1.put("id",1);
    objekat1.put("Name","Dusan");
    objekat1.put("Surname","Jankovic");
    JsonObject objekat2= new JsonObject();
    objekat2.put("id",2);
    objekat2.put("Name","Lazar");
    objekat2.put("Surname","Pavlovic");
    JsonArray objekti=new JsonArray();
    objekti.add(objekat1);
    objekti.add(objekat2);

    HttpServer server= vertx.createHttpServer();

    Router router =Router.router(vertx);

    router.get("/example").handler(routingContext -> {
      routingContext.response()
        .putHeader("content-type","application/json")
        .setChunked(true)
        .write(objekti.encodePrettily()).end();
    });
    router.get("/example/:id").handler(routingContext -> {
      int id=Integer.parseInt(routingContext.request().getParam("id"));
      JsonObject result=new JsonObject();
      result.put("Message","Error 404: Not Found");
      for (int i=0; i < objekti.size(); i++) {
        JsonObject temp=objekti.getJsonObject(i);
        if(temp.getInteger("id")==id){
          result=temp;
        }
      }
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
