package com.matcha.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.util.Date;

public class JwtUtil {
  private final Algorithm algo;
  private final long ttlSeconds;

  public JwtUtil(String secret, long ttlMinutes) {
    this.algo = Algorithm.HMAC256(secret);
    this.ttlSeconds = ttlMinutes * 60;
  }

  public String createToken(long userId, String username, boolean isVerified, String role) {
    Instant now = Instant.now();
    return JWT.create()
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
        .withSubject(String.valueOf(userId))
        .withClaim("username", username)
        .withClaim("verified", isVerified)
        .withClaim("role", role == null ? "user" : role)
        .sign(algo);
  }

  public DecodedJWT verify(String token) {
    return JWT.require(algo).build().verify(token);
  }
}
