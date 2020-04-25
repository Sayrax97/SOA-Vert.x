package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class DataBaseVerticle extends AbstractVerticle {
  private MySQLPool client;
  @Override
  public void start() {
    EventBus eventBus=vertx.eventBus();
    MessageConsumer<JsonObject> consumer=eventBus.consumer("data.base.mysql");
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("localhost")
      .setDatabase("cyclometer")
      .setUser("root")
      .setPassword("");
      PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(5);
      client = MySQLPool.pool(connectOptions, poolOptions);

    consumer.handler(message->{
      if (message.body().getString("method").equals("getUser")){

      }



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
  private JsonObject getUser(int id){
    client.query("SELECT * FROM user");
    return new JsonObject();
  }

  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
