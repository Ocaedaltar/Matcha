package com.matcha.auth;

import static spark.Spark.*;

import com.matcha.core.JsonTransformer;

import com.matcha.users.dto.CreateUserDto;
import com.matcha.users.dto.LoginDto;
import com.matcha.users.mapper.UserMapper;
import com.matcha.users.repo.UserRepository;
import com.matcha.users.service.UserService;

import org.jdbi.v3.core.Jdbi;

import java.util.Map;

public class AuthController {
  private final UserService service;
  private final JsonTransformer json;
  private final JwtUtil jwt;

  public AuthController(Jdbi jdbi, JsonTransformer json, JwtUtil jwt) {
    this.service = new UserService(new UserRepository(jdbi));
    this.json = json;
    this.jwt = jwt;
  }

  public void registerRoutes() {
    path("/auth", () -> {

      post("/signup", (req, res) -> {
        CreateUserDto in = json.fromJson(req.body(), CreateUserDto.class);
        var created = service.create(in); // hash + checks inside
        // ici: soit tu envoies un mail de vérif et tu NE donnes pas de token,
        // soit tu donnes un token "non-verified" (claim verified=false) :
        String token = jwt.createToken(created.getId(), created.getUsername(), created.isVerified(), "user");
        res.status(201);
        res.type("application/json");
        return Map.of(
            "user", UserMapper.toDto(created),
            "token", token
        );
      }, json);

      post("/login", (req, res) -> {
        LoginDto in = json.fromJson(req.body(), LoginDto.class);
        var user = service.login(in.login, in.password); // à ajouter (ci-dessous)
        String token = jwt.createToken(user.getId(), user.getUsername(), user.isVerified(), "user");
        res.type("application/json");
        return Map.of("token", token, "user", UserMapper.toDto(user));
      }, json);

    });
  }
}
