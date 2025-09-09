package com.matcha.config;

import org.jdbi.v3.core.Jdbi;

public class Database {
    private static Jdbi jdbi;

    public static Jdbi getJdbi() {
        if (jdbi == null) {
            jdbi = Jdbi.create(
                "jdbc:postgresql://db:5432/" +
                System.getenv("DB_NAME"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD")
            );
        }
        return jdbi;
    }
}