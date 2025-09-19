package com.matcha.core;

import spark.Filter;
import spark.Request;
import spark.Response;

public class RequestLogFilter implements Filter {
  @Override public void handle(Request req, Response res) {
    if (!req.pathInfo().equals("/health"))
      System.out.printf("%s %s%n", req.requestMethod(), req.pathInfo());
  }
}
