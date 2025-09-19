package com.matcha.users.api;

import static spark.Spark.*;

import com.matcha.core.JsonTransformer;
import com.matcha.users.dto.CreateUserDto;
import com.matcha.users.dto.UpdateUserDto;
import com.matcha.users.mapper.UserMapper;
import com.matcha.users.repo.UserRepository;
import com.matcha.users.service.UserService;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;

public class UserController {
  private final UserService service;
  private final JsonTransformer json;

  public UserController(Jdbi jdbi, JsonTransformer json) {
    this.service = new UserService(new UserRepository(jdbi));
    this.json = json;
  }

  public void registerRoutes() {
    path("/users", () -> {

      get("/:id", (req, res) -> {
        res.type("application/json");
        long id = Long.parseLong(req.params("id"));
        return UserMapper.toDto(service.get(id));
      }, json);

      post("", (req, res) -> {
        res.type("application/json");
        CreateUserDto body = json.fromJson(req.body(), CreateUserDto.class);
        var created = service.create(body);
        res.status(201);
        return UserMapper.toDto(created);
      }, json);

      put("/:id", (req, res) -> {
        res.type("application/json");
        long id = Long.parseLong(req.params("id"));
        UpdateUserDto body = json.fromJson(req.body(), UpdateUserDto.class);
        var updated = service.update(id, body);
        return UserMapper.toDto(updated);
      }, json);

      delete("/:id", (req, res) -> {
        long id = Long.parseLong(req.params("id"));
        service.delete(id);
        res.type("application/json");
        return Map.of("deleted", id);
      }, json);

      // (Optionnel) changement de mot de passe
      // post("/:id/password", (req, res) -> { ... });
    });
  }
}
