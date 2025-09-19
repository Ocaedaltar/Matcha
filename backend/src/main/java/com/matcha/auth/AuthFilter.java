package com.matcha.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import spark.Filter;
import spark.Request;
import spark.Response;

public class AuthFilter implements Filter {
  private final JwtUtil jwt;

  public AuthFilter(JwtUtil jwt) { this.jwt = jwt; }

  @Override public void handle(Request req, Response res) {
    String h = req.headers("Authorization");
    if (h == null || !h.startsWith("Bearer ")) {
      halt401("Missing or invalid Authorization header");
    }
    String token = h.substring("Bearer ".length()).trim();
    try {
      DecodedJWT j = jwt.verify(token);
      long userId = Long.parseLong(j.getSubject());
      req.attribute("userId", userId);
      req.attribute("username", j.getClaim("username").asString());
      req.attribute("verified", j.getClaim("verified").asBoolean());
      req.attribute("role", j.getClaim("role").asString());
    } catch (Exception e) {
      halt401("Invalid token");
    }
  }

  private void halt401(String msg) {
    spark.Spark.halt(401, "{\"error\":\"" + msg + "\"}");
  }
}
