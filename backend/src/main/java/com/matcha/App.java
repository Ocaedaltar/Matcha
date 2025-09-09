package com.matcha;

import spark.Spark;

public class App {
  public static void main(String[] args) {
    Spark.port(8080);
    Spark.get("/health", (req, res) -> "OK");
    Spark.get("/hello",  (req, res) -> "Hello Matcha!");
  }
}