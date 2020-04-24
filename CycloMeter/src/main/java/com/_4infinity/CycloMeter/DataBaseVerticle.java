package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class DataBaseVerticle extends AbstractVerticle {
  @Override
  public void start() {
    JsonObject food=new JsonObject();
    food.put("1","palacinke");
    food.put("2","pizza");

    EventBus eventBus=vertx.eventBus();
    MessageConsumer<JsonObject> consumer=eventBus.consumer("data.base.mysql");
    consumer.handler(message->{
      if (message.body().getString("method").equals("getFood"))
          message.reply(food);
    });
    vertx.createHttpServer().requestHandler(request ->{
      request.response().end("CycloMeter DataBase Verticle");
    } )
      .listen(1997,ar->{
        if(ar.succeeded()){
          System.out.println("CycloMeter DataBase Verticle running");
        }
        else {
          System.out.println("CycloMeter  DataBase Verticle failed");
        }
      });
  }

  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
