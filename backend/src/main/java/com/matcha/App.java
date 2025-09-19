package com.matcha;

import static spark.Spark.*;

import com.matcha.auth.AuthController;
import com.matcha.auth.JwtUtil;
import com.matcha.config.Database;
import com.matcha.core.ExceptionHandlers;
import com.matcha.core.JsonTransformer;
import com.matcha.core.RequestLogFilter;
import com.matcha.users.api.UserController;
import org.jdbi.v3.core.Jdbi;

public class App {
  public static void main(String[] args) {
    port(8080);
    JsonTransformer json = new JsonTransformer();
    Jdbi jdbi = Database.connectFromEnv();

    // Filters
    before(new RequestLogFilter());

    // JWT util depuis env
    String secret = System.getenv("JWT_SECRET");
    long ttlMin = Long.parseLong(System.getenv().getOrDefault("JWT_TTL_MINUTES", "60"));
    JwtUtil jwt = new JwtUtil(secret == null ? "dev-secret" : secret, ttlMin);

    // Health
    get("/health", (req, res) -> "OK");

    new AuthController(jdbi, json, jwt).registerRoutes();
    new UserController(jdbi, json).registerRoutes();

    // Exceptions -> JSON propres
    ExceptionHandlers.register();

    awaitInitialization();
    System.out.println("Matcha API started on :8080");
  }
}
