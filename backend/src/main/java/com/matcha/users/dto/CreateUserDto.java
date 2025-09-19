package com.matcha.users.dto;

public class CreateUserDto {
  public String email;
  public String username;
  public String password;   // en clair dans la requête ; hashé côté service
  public String firstName;
  public String lastName;
  public String gender;
  public String orientation;
}