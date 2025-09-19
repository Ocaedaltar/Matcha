package com.matcha.users.repo;

import com.matcha.users.domain.Gender;
import com.matcha.users.domain.Orientation;
import com.matcha.users.domain.User;
import org.jdbi.v3.core.Jdbi;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class UserRepository {
  private final Jdbi jdbi;
  public UserRepository(Jdbi jdbi) { this.jdbi = jdbi; }

  // --- Mapping ResultSet -> Domain
  private static User map(java.sql.ResultSet rs) throws java.sql.SQLException {
    User u = new User();
    u.setId(rs.getLong("id"));
    u.setEmail(rs.getString("email"));
    u.setUsername(rs.getString("username"));
    u.setPasswordHash(rs.getString("password_hash"));
    u.setFirstName(rs.getString("first_name"));
    u.setLastName(rs.getString("last_name"));
    u.setGender(Gender.fromString(rs.getString("gender")));
    u.setOrientation(Orientation.fromString(rs.getString("orientation")));
    u.setVerified(rs.getBoolean("is_verified"));

    // created_at / updated_at (TIMESTAMPTZ -> OffsetDateTime)
    OffsetDateTime created = rs.getObject("created_at", OffsetDateTime.class);
    OffsetDateTime updated = rs.getObject("updated_at", OffsetDateTime.class);
    u.setCreatedAt(created);
    u.setUpdatedAt(updated);
    return u;
  }

  // --- READs
  public List<User> findAll() {
    String sql = """
        SELECT id,email,username,password_hash,first_name,last_name,
               gender,orientation,is_verified,created_at,updated_at
        FROM users ORDER BY id
        """;
    return jdbi.withHandle(h -> h.createQuery(sql).map((rs,ctx) -> map(rs)).list());
  }

  public Optional<User> findById(long id) {
    String sql = """
        SELECT id,email,username,password_hash,first_name,last_name,
               gender,orientation,is_verified,created_at,updated_at
        FROM users WHERE id = :id
        """;
    return jdbi.withHandle(h ->
        h.createQuery(sql).bind("id", id).map((rs,ctx) -> map(rs)).findOne()
    );
  }

  public Optional<User> findByEmailCi(String email) {
    String sql = """
        SELECT id,email,username,password_hash,first_name,last_name,
               gender,orientation,is_verified,created_at,updated_at
        FROM users WHERE LOWER(email) = LOWER(:email)
        """;
    return jdbi.withHandle(h ->
        h.createQuery(sql).bind("email", email).map((rs,ctx) -> map(rs)).findOne()
    );
  }

  public Optional<User> findByUsernameCi(String username) {
    String sql = """
        SELECT id,email,username,password_hash,first_name,last_name,
               gender,orientation,is_verified,created_at,updated_at
        FROM users WHERE LOWER(username) = LOWER(:username)
        """;
    return jdbi.withHandle(h ->
        h.createQuery(sql).bind("username", username).map((rs,ctx) -> map(rs)).findOne()
    );
  }

  // --- CREATE
  // Attend un passwordHash déjà calculé côté Service (jBCrypt)
  public long create(User u) {
    String sql = """
        INSERT INTO users(email, username, password_hash, first_name, last_name, gender, orientation)
        VALUES (:email, :username, :passwordHash, :firstName, :lastName, :gender, :orientation)
        RETURNING id
        """;
    return jdbi.withHandle(h ->
        h.createQuery(sql)
          .bind("email", u.getEmail())
          .bind("username", u.getUsername())
          .bind("passwordHash", u.getPasswordHash())
          .bind("firstName", u.getFirstName())
          .bind("lastName", u.getLastName())
          .bind("gender", u.getGender() == null ? "unspecified" : u.getGender().name())
          .bind("orientation", u.getOrientation() == null ? "unspecified" : u.getOrientation().name())
          .mapTo(Long.class)
          .one()
    );
  }

  // --- UPDATE (partiel)
  // Met uniquement ce qui est non-null (construit au Service)
  public boolean updatePartial(long id, String email, String username,
                               String firstName, String lastName,
                               Gender gender, Orientation orientation) {
    StringBuilder sb = new StringBuilder("UPDATE users SET ");
    boolean first = true;
    if (email != null)     { sb.append("email = :email"); first = false; }
    if (username != null)  { if (!first) sb.append(", "); sb.append("username = :username"); first = false; }
    if (firstName != null) { if (!first) sb.append(", "); sb.append("first_name = :firstName"); first = false; }
    if (lastName != null)  { if (!first) sb.append(", "); sb.append("last_name = :lastName"); first = false; }
    if (gender != null)    { if (!first) sb.append(", "); sb.append("gender = :gender"); first = false; }
    if (orientation != null){ if (!first) sb.append(", "); sb.append("orientation = :orientation"); first = false; }

    sb.append(" WHERE id = :id");
    String sql = sb.toString();

    if (first) return true; // rien à mettre à jour

    int changed = jdbi.withHandle(h ->
        h.createUpdate(sql)
          .bind("email", email)
          .bind("username", username)
          .bind("firstName", firstName)
          .bind("lastName", lastName)
          .bind("gender", gender == null ? null : gender.name())
          .bind("orientation", orientation == null ? null : orientation.name())
          .bind("id", id)
          .execute()
    );
    return changed > 0;
  }

  // --- UPDATE mot de passe (route dédiée si besoin)
  public boolean updatePasswordHash(long id, String newHash) {
    String sql = "UPDATE users SET password_hash = :ph WHERE id = :id";
    int changed = jdbi.withHandle(h ->
        h.createUpdate(sql).bind("ph", newHash).bind("id", id).execute()
    );
    return changed > 0;
  }

  // --- DELETE
  public boolean delete(long id) {
    String sql = "DELETE FROM users WHERE id = :id";
    int changed = jdbi.withHandle(h -> h.createUpdate(sql).bind("id", id).execute());
    return changed > 0;
  }
}
