package com._4infinity.CycloMeter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import javafx.scene.control.DatePicker;

import javax.swing.*;
import java.sql.Date;
import java.time.LocalDate;

public class DataBaseVerticle extends AbstractVerticle {
  private MySQLPool client;
  @Override
  public void start() {
    //region Init
    EventBus eventBus = vertx.eventBus();
    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3308)
      .setHost("localhost")
      .setDatabase("cyclometer")
      .setUser("root")
      .setPassword("");
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);
    client = MySQLPool.pool(vertx, connectOptions, poolOptions);
    //endregion

    //region getUser
    MessageConsumer<JsonObject> consumer = eventBus.consumer("data.base.getUser");
    JsonObject msg = new JsonObject();
    consumer.handler(message -> {
      client.query("SELECT * FROM users WHERE id=" + message.body().getInteger("id")).execute(event -> {
        if (event.succeeded()) {
          RowSet<Row> result = event.result();
          if (result.size() == 0) {
            msg.put("statusCode", 404);
          } else {
            for (Row res : result) {
              msg.put("statusCode", 200);
              msg.put("id", res.getInteger(0));
              msg.put("username", res.getString(1));
              msg.put("gender", res.getString(2));
              msg.put("weight", res.getInteger(3));
              msg.put("age", res.getInteger(4));
            }
          }
        } else {
          msg.put("statusCode", 400);
        }
        message.reply(msg);
      });
    });
    //endregion

    //region Server
    vertx.createHttpServer().requestHandler(request -> {
      request.response().end("CycloMeter DataBase Verticle");
    })
      .listen(1997, ar -> {
        if (ar.succeeded()) {
          System.out.println("CycloMeter DataBase Verticle running");
        } else {
          System.out.println("CycloMeter  DataBase Verticle failed");
        }
      });
    //endregion

    //region postUser
    MessageConsumer<JsonObject> consumer1=eventBus.consumer("data.base.postUser");
    consumer1.handler(message -> {
      System.out.println("uso sam u handler consmer");
      client.preparedQuery("INSERT INTO users (username, gender,weight,age) VALUES(?,?,?,?)").
        execute(Tuple.of(message.body().getString("username"),
          (message.body().getString("gender")),
          (message.body().getInteger("weight")),
          (message.body().getInteger("age"))),event -> {
            if(event.succeeded()){
              message.reply("Uspesno odradjeno");
              System.out.println("stiglo u uspesno");
            }
            else{
              message.reply("Neuspesno odradjeno");
              System.out.println("stiglo u neuspesno");
            }
          });
    });
    //endregion

    //region GetSensor
    MessageConsumer<JsonObject> consumerGetSensor=eventBus.consumer("data.base.getSensor");
    consumerGetSensor.handler(message->{
      int id=message.body().getInteger("id");
      //get data from database
      JsonObject sensor=new JsonObject();
      sensor.put("id",id);
      sensor.put("total_distance_traveled",24);
      sensor.put("started_at", "19:55");
      sensor.put("User_id",1);
      message.reply(sensor);

    });
    //endregion

    //region PostSensor
    MessageConsumer<JsonObject> consumerPostSensor=eventBus.consumer("data.base.postSensor");
    consumerPostSensor.handler(message->{
      JsonObject sensor=message.body();
      //send data to database
      //if success
      message.reply("Sensor Added to Database");
      //else
    });
    //endregion

  }
  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
