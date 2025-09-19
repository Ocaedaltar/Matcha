package com.matcha.users.dto;

public class UserDto {
  public long id;
  public String email;
  public String username;
  public String firstName;
  public String lastName;
  public String gender;       // "male" | "female" | "other" | "unspecified"
  public String orientation;  // "hetero" | "homo" | "bi" | "unspecified"
  public boolean isVerified;
  public String createdAt;    // ISO-8601 (OffsetDateTime.toString())
  public String updatedAt;    // ISO-8601
}