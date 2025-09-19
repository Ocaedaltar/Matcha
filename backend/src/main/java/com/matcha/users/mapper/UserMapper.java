package com.matcha.users.mapper;

import com.matcha.users.domain.Gender;
import com.matcha.users.domain.Orientation;
import com.matcha.users.domain.User;

import com.matcha.users.dto.CreateUserDto;
import com.matcha.users.dto.UpdateUserDto;
import com.matcha.users.dto.UserDto;

public class UserMapper {

  // Domain -> DTO (sortie)
  public static UserDto toDto(User u) {
    UserDto dto = new UserDto();
    dto.id = u.getId();
    dto.email = u.getEmail();
    dto.username = u.getUsername();
    dto.firstName = u.getFirstName();
    dto.lastName = u.getLastName();
    dto.gender = u.getGender() == null ? null : u.getGender().name();
    dto.orientation = u.getOrientation() == null ? null : u.getOrientation().name();
    dto.isVerified = u.isVerified();
    dto.createdAt = u.getCreatedAt() == null ? null : u.getCreatedAt().toString();
    dto.updatedAt = u.getUpdatedAt() == null ? null : u.getUpdatedAt().toString();
    return dto;
  }

  // Create DTO -> Domain
  // ⚠️ Ne renseigne PAS le passwordHash ici. Le service doit le calculer ensuite.
  public static User fromCreateDto(CreateUserDto in) {
    User u = new User();
    u.setEmail(in.email);
    u.setUsername(in.username);
    u.setFirstName(in.firstName);
    u.setLastName(in.lastName);
    u.setGender(Gender.fromString(in.gender));
    u.setOrientation(Orientation.fromString(in.orientation));
    return u;
  }

  // Update DTO -> applique dans le domain (mutations partielles)
  public static void applyUpdate(User u, UpdateUserDto in) {
    if (in.email != null) u.setEmail(in.email);
    if (in.username != null) u.setUsername(in.username);
    if (in.firstName != null) u.setFirstName(in.firstName);
    if (in.lastName != null) u.setLastName(in.lastName);
    if (in.gender != null) u.setGender(Gender.fromString(in.gender));
    if (in.orientation != null) u.setOrientation(Orientation.fromString(in.orientation));
  }
}
