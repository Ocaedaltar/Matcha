package com.matcha.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
  private Db() {}

  static {
    try {
      // Charge le driver explicitement (utile si l’auto-loading ne se fait pas)
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("PostgreSQL JDBC driver not found", e);
    }
  }

  private static String env(String k, String def) {
    String v = System.getenv(k);
    return (v == null || v.isBlank()) ? def : v;
  }

  private static final String URL = String.format(
      "jdbc:postgresql://%s:%s/%s",
      env("DB_HOST", "nom du service docker"),     // <— nom du service docker
      env("DB_PORT", "port du service docker"),
      env("DB_NAME", "nom de la base de donnee")
  );
  private static final String USER = env("DB_USER", "nom de l'utilisateur admin");
  private static final String PASS = env("DB_PASSWORD", "mot de pass de l'utilisateur admin!");

  public static Connection connect() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASS);
  }
}