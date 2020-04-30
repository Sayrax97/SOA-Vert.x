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
import io.vertx.sqlclient.impl.RowStreamImpl;
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
      client.preparedQuery("SELECT * FROM users WHERE id=?").execute(Tuple.of(message.body().getInteger("id")),event -> {
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
    MessageConsumer<JsonObject> consumerGetUserByUsername = eventBus.consumer("data.base.getUserByUsername");
    consumerGetUserByUsername.handler(message -> {
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
                    JsonObject msg=new JsonObject();
                    msg.put("statusCode",200);
                    msg.put("message","User Added");
                    message.reply(msg);
                  }
                  else{
                    JsonObject msg=new JsonObject();
                    msg.put("statusCode",500);
                    msg.put("message","Error");
                    message.reply(msg);
                  }
                });
            }
            else {
              JsonObject msg=new JsonObject();
              msg.put("statusCode",400);
              msg.put("message","User already exists");
              message.reply(msg);
            }
          }
          else {
            JsonObject msg=new JsonObject();
            msg.put("statusCode",500);
            msg.put("message","Query error");
            message.reply(msg);
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
                sensor.put("status_voznje",res.getInteger(3));
                sensor.put("User_id",res.getInteger(4));
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
      JsonObject msg=new JsonObject();
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
                   //message.reply("greska u trazenju senzora");
                 } else {
                   for (Row res : result) {
                     id=res.getInteger(0);
                   }
                   client.preparedQuery("INSERT INTO senzor_data (speed,incline,terrain_type,heart_rate,senzor_id,time_stemp,distance_traveled) VALUES(?,?,?,?,?,?,?)")
                     .execute(Tuple.of(0,0,"flat",0,id,time,0),event2 -> {
                       if(!event2.succeeded()){
                         //message.reply("neuspesno unet senzor data");
                       }
                       msg.put("statusCode",200);
                       message.reply(msg);
                     });
                 }
               }
               else{
                 //message.reply("greska u queriju");
               }
             }
           );

         }
         else{
           msg.put("statusCode",400);
           message.reply(msg);
         }

       });

    });
    //endregion

    //region PostSensorData
    MessageConsumer<JsonObject> consumerPostSensorData=eventBus.consumer("data.base.postSensorData");
    consumerPostSensorData.handler(message->{
      LocalTime time=LocalTime.now();
      JsonObject data= message.body();
      JsonObject msg=new JsonObject();
      int i=data.getBoolean("incline")?1:0;
      System.out.println(data.encodePrettily());
      client.preparedQuery("INSERT INTO senzor_data (speed,incline,terrain_type,heart_rate,senzor_id,time_stemp,distance_traveled) VALUES(?,?,?,?,?,?,?)")
        .execute(Tuple.of(data.getFloat("speed"),
          i,
          data.getString("terrain_type"),
          data.getInteger("heart_rate"),
          data.getInteger("senzor_id"),
          time,
          data.getInteger("distance_traveled")), result -> {
          if(result.succeeded()){
               client.preparedQuery("SELECT total_distance_traveled FROM senzor WHERE id=?")
              .execute(Tuple.of( data.getInteger("senzor_id")),event -> {
                double totalDistance=0;
                if(event.succeeded()){
                  RowSet<Row> res=event.result();
                  if(res.size()>0){
                    for (Row r:res) {
                      totalDistance=r.getDouble(0);
                    }
                    totalDistance+=data.getFloat("distance_traveled");
                    client.preparedQuery("UPDATE senzor SET total_distance_traveled=?")
                      .execute(Tuple.of(totalDistance),event1 -> {
                        if(event1.succeeded())
                          msg.put("statusCode",200);
                        else{
                          msg.put("statusCode",400);
                        }
                        message.reply(msg);
                      });
                  }
                  //message.reply("Nije pronaso ni jedan senzor");
                }
                //message.reply("Greska u selectu");

              });
          }
          else {
            msg.put("statusCode",400);
            message.reply(msg);
          }

        });
      });


    //endregion

    //region GetAllSensorData
    MessageConsumer<JsonObject> consumerGetSensorDataAll=eventBus.consumer("data.base.GetSensorDataAll");
    consumerGetSensorDataAll.handler(message->{
      JsonArray data=new JsonArray();
      JsonObject object=new JsonObject();
      int id=message.body().getInteger("id");
      client.preparedQuery("SELECT * FROM senzor_data WHERE senzor_id=? ORDER BY id ASC").execute(Tuple.of(id),result->{
        if(result.succeeded()){
          RowSet<Row> res=result.result();
          if(res.size()>0) {
            for (Row r : res) {
              JsonObject sensorData = new JsonObject();
              sensorData.put("speed", r.getDouble("speed"));
              sensorData.put("incline", r.getInteger("incline"));
              sensorData.put("terrain_type", r.getString("terrain_type"));
              sensorData.put("heart_rate", r.getInteger("heart_rate"));
              sensorData.put("senzor_id", r.getInteger("senzor_id"));
              LocalTime time = r.getLocalDateTime("time_stemp").toLocalTime();
              sensorData.put("time_stemp", time.toString());
              sensorData.put("distance_traveled", r.getDouble("distance_traveled"));
              data.add(sensorData);
            }
            object.put("statusCode", 200);
          }
          else{
            object.put("statusCode",404);
          }
        }
        else {
          object.put("statusCode",400);
        }
        object.put("result",data);
        message.reply(object);
      });

    });
    //endregion

    //region GetAllSensor
    MessageConsumer<JsonObject> consumerGetSensorAll=eventBus.consumer("data.base.GetSensorAll");
    consumerGetSensorAll.handler(message->{
      JsonArray data=new JsonArray();
      JsonObject object=new JsonObject();
      int id=message.body().getInteger("id");
      client.preparedQuery("SELECT * FROM senzor WHERE user_id=?").execute(Tuple.of(id),result->{
        if(result.succeeded()){
          RowSet<Row> res=result.result();
          if(res.size()>0) {
            for (Row r : res) {
              JsonObject sensor = new JsonObject();
              sensor.put("id",r.getInteger(0));
              sensor.put("total_distance_traveled",r.getDouble(1));
              LocalDateTime time=r.getLocalDateTime(2);
              sensor.put("started_at", time.toString());
              sensor.put("status_voznje",r.getInteger(3));
              sensor.put("User_id",r.getInteger(4));
              data.add(sensor);
            }
            object.put("statusCode", 200);
          }
          else{
            object.put("statusCode",404);
          }
        }
        else {
          object.put("statusCode",400);
        }
        object.put("result",data);
        message.reply(object);
      });

    });
    //endregion

    //region getMET
    MessageConsumer<JsonObject> consumerMET=eventBus.consumer("data.base.getMET");
     consumerMET.handler(message -> {
       JsonObject sensor=new JsonObject();
       double speed=message.body().getDouble("speed");
       client.preparedQuery("SELECT MET FROM activity WHERE speed<? ORDER BY MET DESC LIMIT 1")
         .execute(Tuple.of(speed),event -> {
           if(event.succeeded()) {
             RowSet<Row> result = event.result();
             if (result.size() != 0){
               for (Row res:result) {
                 sensor.put("MET",res.getDouble("MET"));
               }
               sensor.put("statusCode",200);
             }
             else{
               sensor.put("statusCode",404);
             }
           }
           else{
             sensor.put("statusCode",400);
           }
           message.reply(sensor);
         });

     });
    //endregion

    //region putSensor
    MessageConsumer<JsonObject> consumerPutSenzor=eventBus.consumer("data.base.putSensor");
    consumerPutSenzor.handler(message-> {
      JsonObject msg=new JsonObject();
      client.preparedQuery("UPDATE senzor SET status_voznje=1 WHERE id=?")
        .execute(Tuple.of(message.body().getInteger("senzor_id"))
          ,event -> {
            if(event.succeeded())
              msg.put("statusCode",200);
            else{
              msg.put("statusCode",400);
            }
            message.reply(msg);
          });

    });
    //endregion

  }
  @Override
  public void stop() throws Exception {
    super.stop();
  }
}
