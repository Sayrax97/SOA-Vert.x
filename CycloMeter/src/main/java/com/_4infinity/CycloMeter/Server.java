package com._4infinity.CycloMeter;

import io.vertx.core.Vertx;

public class Server {
  public static void main(String[] args) {
    Vertx vertx=Vertx.vertx();
    vertx.deployVerticle(new DataBaseVerticle());
    vertx.deployVerticle(new ServiceVerticle());
  }
}
