package com.matcha.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {
  private final Gson gson = new GsonBuilder()
      .serializeNulls()
      .setPrettyPrinting()
      .create();

  @Override public String render(Object model) {
    return gson.toJson(model);
  }

  public <T> T fromJson(String body, Class<T> clazz) {
    return gson.fromJson(body, clazz);
  }
}