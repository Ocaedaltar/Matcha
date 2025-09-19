package com.matcha.core;

import static spark.Spark.exception;

import java.util.Map;

public class ExceptionHandlers {
  public static void register() {
    exception(NotFoundException.class, (e, req, res) -> {
      res.type("application/json");
      res.status(404);
      res.body(JsonUtil.obj(Map.of("error", e.getMessage())));
    });

    exception(BadRequestException.class, (e, req, res) -> {
      res.type("application/json");
      res.status(400);
      res.body(JsonUtil.obj(Map.of("error", e.getMessage())));
    });

    exception(Exception.class, (e, req, res) -> {
      e.printStackTrace();
      res.type("application/json");
      res.status(500);
      res.body(JsonUtil.obj(Map.of("error", "Internal Server Error")));
    });
  }

  // petites classes utilitaires + exceptions
  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) { super(msg); }
  }
  public static class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) { super(msg); }
  }
  private static class JsonUtil {
    private static final com.google.gson.Gson G = new com.google.gson.Gson();
    static String obj(Object o) { return G.toJson(o); }
  }
}
