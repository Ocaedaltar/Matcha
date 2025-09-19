package com.matcha.users.dto;

public class UpdateUserDto {
  public String email;        // optionnel
  public String username;     // optionnel
  public String firstName;    // optionnel
  public String lastName;     // optionnel
  public String gender;       // optionnel
  public String orientation;  // optionnel
  // Pas de password -> route dédiée
}