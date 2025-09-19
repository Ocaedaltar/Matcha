package com.matcha.auth;

import spark.Filter;
import spark.Request;
import spark.Response;

public class RequireVerifiedFilter implements Filter {
  @Override public void handle(Request req, Response res) {
    Boolean verified = req.attribute("verified");
    if (verified == null || !verified) {
      spark.Spark.halt(403, "{\"error\":\"Account not verified yet\"}");
    }
  }
}
