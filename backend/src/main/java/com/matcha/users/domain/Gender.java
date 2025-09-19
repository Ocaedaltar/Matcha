package com.matcha.users.domain;

public enum Gender {
  male, female, other, unspecified;

  public static Gender fromString(String s) {
    if (s == null) return unspecified;
    try { return Gender.valueOf(s.toUpperCase()); }
    catch (IllegalArgumentException e) { return unspecified; }
  }
}
