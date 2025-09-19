package com.matcha.users.domain;

import java.time.OffsetDateTime;

public class User {
  private long id;
  private String email;
  private String username;
  private String passwordHash;     // ⚠️ jamais exposé en DTO
  private String firstName;
  private String lastName;
  private Gender gender;           // mappe gender_t
  private Orientation orientation; // mappe orientation_t
  private boolean verified;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  // Getters
  public long getId() { return id; }
  public String getEmail() { return email; }
  public String getUsername() { return username; }
  public String getPasswordHash() { return passwordHash; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public Gender getGender() { return gender; }
  public Orientation getOrientation() { return orientation; }
  public boolean isVerified() { return verified; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  // Setters
  public void setId(long id) { this.id = id; }
  public void setEmail(String email) { this.email = email; }
  public void setUsername(String username) { this.username = username; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public void setGender(Gender gender) { this.gender = gender; }
  public void setOrientation(Orientation orientation) { this.orientation = orientation; }
  public void setVerified(boolean verified) { this.verified = verified; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
