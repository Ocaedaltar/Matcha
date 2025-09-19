package com.matcha.config;

import org.jdbi.v3.core.Jdbi;

public class Database {
  public static Jdbi connectFromEnv() {
    String url  = getenvOrDefault("DB_URL",  "jdbc:postgresql://db:5432/ginger");
    String user = getenvOrDefault("DB_USER", "ftsguf");
    String pass = getenvOrDefault("DB_PASS", "SuperMDP1!");
    return Jdbi.create(url, user, pass);
  }
  private static String getenvOrDefault(String k, String d) {
    String v = System.getenv(k);
    return (v == null || v.isBlank()) ? d : v;
  }
}
