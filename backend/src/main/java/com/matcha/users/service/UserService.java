package com.matcha.users.service;

import com.matcha.core.ExceptionHandlers.BadRequestException;
import com.matcha.core.ExceptionHandlers.NotFoundException;

import com.matcha.users.domain.User;
import com.matcha.users.dto.CreateUserDto;
import com.matcha.users.dto.UpdateUserDto;
import com.matcha.users.mapper.UserMapper;
import com.matcha.users.repo.UserRepository;

import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

public class UserService {
  private final UserRepository repo;

  public UserService(UserRepository repo) { this.repo = repo; }

  private static final Pattern EMAIL_RE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  private static final Pattern PASSWORD_RE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$");

  // --- Helpers validation
  private void validateCreate(CreateUserDto in) {
    if (in == null) throw new BadRequestException("Body required");
    if (in.email == null || !EMAIL_RE.matcher(in.email).matches())
      throw new BadRequestException("Invalid email");
    if (in.username == null || in.username.isBlank())
      throw new BadRequestException("username required");
    if (in.password == null || !PASSWORD_RE.matcher(in.password).matches())
      throw new BadRequestException("Password must be at least 8 chars, contain upper, lower, and special char");
  }

  private void validateUpdate(UpdateUserDto in) {
    if (in == null) throw new BadRequestException("Body required");
    if (in.email != null && !EMAIL_RE.matcher(in.email).matches())
      throw new BadRequestException("Invalid email");
  }


  // ------------------------------------------------------------------------- //
  // -- GET USER BY ID

  public User get(long id) {
    return repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
  }

  // ------------------------------------------------------------------------- //
  // -- USER CREATE

  public User create(CreateUserDto in) {
    validateCreate(in);

    // Unicité app (en plus de la contrainte SQL pour message plus propre)
    if (repo.findByEmailCi(in.email).isPresent())
      throw new BadRequestException("Email already used");
    if (repo.findByUsernameCi(in.username).isPresent())
      throw new BadRequestException("Username already used");

    // Mapper DTO -> Domain (sans passwordHash)
    User u = UserMapper.fromCreateDto(in);

    // Hash côté Java (jBCrypt)
    String hash = BCrypt.hashpw(in.password, BCrypt.gensalt(12)); // cost=12 (équilibre sécu/perf)
    u.setPasswordHash(hash);

    long id = repo.create(u);
    return get(id);
  }

  // ------------------------------------------------------------------------- //
  // -- USER UPDATE

  public User update(long id, UpdateUserDto in) {
    validateUpdate(in);

    User existing = get(id);
    UserMapper.applyUpdate(existing, in);

    if (in.email != null) {
      repo.findByEmailCi(existing.getEmail()).ifPresent(other -> {
        if (other.getId() != id) throw new BadRequestException("Email already used");
      });
    }
    if (in.username != null) {
      repo.findByUsernameCi(existing.getUsername()).ifPresent(other -> {
        if (other.getId() != id) throw new BadRequestException("Username already used");
      });
    }
    boolean ok = repo.updatePartial(
        id,
        existing.getEmail(),
        existing.getUsername(),
        existing.getFirstName(),
        existing.getLastName(),
        existing.getGender(),
        existing.getOrientation()
    );
    if (!ok) throw new NotFoundException("User not found");
    return get(id);
  }

  // ------------------------------------------------------------------------- //
  // --  CHANGE PASSWORD 

  public void changePassword(long id, String oldPassword, String newPassword) {
    if (newPassword == null || !PASSWORD_RE.matcher(newPassword).matches())
      throw new BadRequestException("Password must be at least 8 chars, contain upper, lower, and special char");

    User existing = get(id);
    if (!BCrypt.checkpw(oldPassword, existing.getPasswordHash()))
      throw new BadRequestException("invalid credentials");

    String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
    boolean ok = repo.updatePasswordHash(id, newHash);
    if (!ok) throw new NotFoundException("User not found");
  }

  // ------------------------------------------------------------------------- //
  // --  DELETE USER BY ID

  public void delete(long id) {
    boolean ok = repo.delete(id);
    if (!ok) throw new NotFoundException("User not found");
  }

  // ------------------------------------------------------------------------- //
  // --  LOGIN USER

  public User login(String login, String password) {
    if (login == null || password == null)
      throw new BadRequestException("login & password required");

    var byEmail = repo.findByEmailCi(login);
    var byUsername = repo.findByUsernameCi(login);

    var user = byEmail.orElseGet(() -> byUsername.orElseThrow(() ->
        new BadRequestException("invalid credentials")
    ));

    if (!BCrypt.checkpw(password, user.getPasswordHash())) {
      throw new BadRequestException("invalid credentials");
    }
    return user;
  }

  // ------------------------------------------------------------------------- //
  // --  DELETE USER BY ID
}
