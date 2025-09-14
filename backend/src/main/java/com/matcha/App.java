package com.matcha;

import static spark.Spark.*;

public class App {
  private static String env(String k, String def) {
    String v = System.getenv(k);
    return (v == null || v.isBlank()) ? def : v;
  }

  public static void main(String[] args) {
    port(Integer.parseInt(env("BACKEND_PORT", "8080")));

    // (option) CORS très simple pour ton front Angular
    before((req, res) -> {
      res.header("Access-Control-Allow-Origin", "*");
      res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
      res.header("Access-Control-Allow-Headers", "Content-Type");
    });
    options("/*", (req,res) -> "OK");

    get("/health", (req, res) -> "OK");
    get("/hello",  (req, res) -> "Hello Matcha!");
  }
}
