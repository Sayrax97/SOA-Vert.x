package com._4infinity.CycloMeter;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import javafx.scene.control.DatePicker;

import javax.swing.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

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
    consumer.handler(message -> {
      client.query("SELECT * FROM users WHERE id=" + message.body().getInteger("id")).execute(event -> {
        JsonObject msg = new JsonObject();
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

    //region getUserByUsername
    MessageConsumer<JsonObject> consumerGetUser = eventBus.consumer("data.base.getUser");
    consumer.handler(message -> {
      client.preparedQuery("SELECT * FROM users WHERE username=?")
        .execute(Tuple.of(message.body().getString("username")),event -> {
        JsonObject msg = new JsonObject();
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
      client.preparedQuery("SELECT * FROM users WHERE username=?")
        .execute(Tuple.of(message.body().getString("username")),event1 -> {
          if(event1.succeeded()){
            RowSet<Row> result=event1.result();
            if(result.size()==0){
              client.preparedQuery("INSERT INTO users (username, gender,weight,age) VALUES(?,?,?,?)")
                .execute(Tuple.of(message.body().getString("username"),
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
            }
            else {
              message.reply("User vec postoji");
            }
          }
          else {
            message.reply("Greska u queriju");
          }
        });

    });
    //endregion

    //region GetSensor
    MessageConsumer<JsonObject> consumerGetSensor=eventBus.consumer("data.base.getSensor");
    consumerGetSensor.handler(message->{
      client.preparedQuery("SELECT * FROM senzor WHERE id=?")
        .execute(Tuple.of(message.body().getInteger("id")),event -> {
          JsonObject sensor=new JsonObject();
          if(event.succeeded()){
            RowSet<Row> result = event.result();
            if(result.size()>0){
              for (Row res:result) {
                sensor.put("id",res.getInteger(0));
                sensor.put("total_distance_traveled",res.getDouble(1));
                LocalDateTime time=res.getLocalDateTime(2);
                System.out.println(time);
                sensor.put("started_at", time.toString());
                sensor.put("User_id",res.getInteger(3));
              }
            }
            else{
              sensor.put("statusCode", 404);
            }
          }
          else{
            sensor.put("statusCode", 400);
          }
          message.reply(sensor);
        });


    });
    //endregion

    //region PostSensor
    MessageConsumer<JsonObject> consumerPostSensor=eventBus.consumer("data.base.postSensor");
    consumerPostSensor.handler(message->{
      //JsonObject sensor=message.body();
      LocalTime time=LocalTime.now();
      client.preparedQuery("INSERT INTO senzor (total_distance_traveled,started_at,status_voznje,user_id) VALUES(?,?,?,?)")
       .execute(Tuple.of(
         0,
         time,
         0,
         message.body().getInteger("user_id")
         ),
         event -> {
         if(event.succeeded()){
           client.preparedQuery("SELECT id FROM senzor WHERE user_id=? AND status_voznje=?").execute(
             Tuple.of(message.body().getInteger("user_id"),0),event1 -> {
               int id=0;
               if (event1.succeeded()) {
                 RowSet<Row> result = event1.result();
                 if (result.size() == 0) {
                   message.reply("greska u trazenju senzora");
                 } else {
                   for (Row res : result) {
                     id=res.getInteger(0);
                   }
                   client.preparedQuery("INSERT INTO senzor_data (speed,incline,terain_type,heart_rate,senzor_id,time_stemp,distance_traveled) VALUES(?,?,?,?,?,?,?)")
                     .execute(Tuple.of(0,0,"flat",0,id,time,0),event2 -> {
                       if(!event2.succeeded()){
                         message.reply("neuspesno unet senzor data");
                       }
                     });
                 }
               }
               else{
                 message.reply("greska u queriju");
               }
             }
           );
           message.reply("Sve je dobro");
         }
         else{
           message.reply("Neuspesno odradjeno");
           System.out.println(event.cause());
         }
       });
    });
    //endregion

    //region PostSensorData
    MessageConsumer<JsonObject> consumerPostSensorData=eventBus.consumer("data.base.postSensorData");
    consumerPostSensorData.handler(message->{
      LocalTime time=LocalTime.now();
      JsonObject data= message.body();
      client.preparedQuery("INSERT INTO senzor_data (speed,incline,terain_type,heart_rate,senzor_id,time_stemp,distance_traveled) VALUES(?,?,?,?,?,?,?)")
        .execute(Tuple.of(data.getFloat("speed"),
          data.getInteger("incline"),
          data.getString("terain_type"),
          data.getInteger("heart_rate"),
          data.getInteger("senzor_id"),
          time,
          data.getInteger("distance_traveled")), result -> {
          if(result.succeeded()){
            message.reply("Sensor Data added to Database");
          }
          else {
            message.reply("Error adding sensor data");
          }
        });
      message.reply("Sensor Data added to Database");
    });


    //endregion

    //region GetAllSensorData
    MessageConsumer<JsonObject> consumerGetSensorDataAll=eventBus.consumer("data.base.GetSensorDataAll");
    consumerGetSensorDataAll.handler(message->{
      int id=message.body().getInteger("id");
      client.preparedQuery("SELECT * FROM senzor_data WHERE senzor_id=?").execute(Tuple.of(id),result->{
        if(result.succeeded()){
          JsonArray data=new JsonArray();
          for (Row res:result.result()) {
            JsonObject sensorData=new JsonObject();
            sensorData.put("speed",res.getDouble("speed"));
            sensorData.put("incline",res.getInteger("incline"));
            sensorData.put("terain_type",res.getString("terain_type"));
            sensorData.put("heart_rate",res.getInteger("heart_rate"));
            sensorData.put("senzor_id",res.getInteger("senzor_id"));
            LocalTime time=res.getLocalDateTime("time_stemp").toLocalTime();
            sensorData.put("time_stemp",time.toString());
            sensorData.put("distance_traveled",res.getDouble("distance_traveled"));
            data.add(sensorData);
          }
          message.reply(data);
        }
        else {
          message.reply("Error occured while fethcing data");
        }
      });
    });
    //endregion

    //region getMET
    MessageConsumer<JsonObject> consumerMET=eventBus.consumer("data.base.getMET");
     consumerMET.handler(message -> {
       double speed=message.body().getDouble("speed");
       client.preparedQuery("SELECT MET FROM activity WHERE speed<? ORDER BY MET DESC LIMIT 1")
         .execute(Tuple.of(speed),event -> {
           JsonObject sensor=new JsonObject();
           if(event.succeeded()) {
             RowSet<Row> result = event.result();
             if (result.size() != 0){
               for (Row res:result) {
                 sensor.put("MET",res.getDouble("MET"));
               }
               message.reply(sensor);
             }
             else{
               message.reply("Nije pronadjen ni jedan rezultat");
             }
           }
           else{
             message.reply("greska u upitu");
           }
         });

     });
    //endregion


  }
  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
