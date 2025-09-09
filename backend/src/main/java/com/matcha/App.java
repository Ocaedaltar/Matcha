package com.matcha;
import org.jdbi.v3.core.Jdbi;

import spark.Spark;

class User {
    int id;
    String name;
    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

public class App {
  public static void main(String[] args) {

    // Connexion à PostgreSQL
    Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/ginger", "ftsguf", "SuperMDP1!");


    Spark.port(8080);
    Spark.get("/health", (req, res) -> "OK");
    Spark.get("/hello",  (req, res) -> "Hello Matcha!");

    // Exemple route GET
    Spark.get("/users", (req, res) -> {
        res.type("application/json");
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT id, name FROM users")
                .map((rs, ctx) -> rs.getInt("id") + " - " + rs.getString("name"))
                .list()
        ).toString();
    });

    // Exemple route POST
    Spark.post("/users", (req, res) -> {
        String name = req.queryParams("name");
        jdbi.useHandle(handle ->
            handle.createUpdate("INSERT INTO users (name) VALUES (:name)")
                    .bind("name", name)
                    .execute()
        );
        res.status(201);
        return "User ajouté : " + name;
    });
  }
}

/*
 *  curl -X GET http://localhost:8080/users
 *  curl -X POST http://localhost:8080/users \
        -H "Content-Type: application/json" \
        -d '{"name": "Maxime"}'
 * 
 * 
 * 
 */