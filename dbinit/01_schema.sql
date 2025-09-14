-- ==========================================================
-- 01_schema.sql
-- ----------------------------------------------------------
-- Schéma minimal pour l'authentification :
--   users(email, username, password_hash, first_name, last_name,
--         gender, orientation, is_verified)
-- + quelques champs utilitaires (created_at, updated_at).
--
-- Ce fichier est exécuté automatiquement PAR POSTGRES
-- à la première initialisation du volume, si monté dans
-- /docker-entrypoint-initdb.d (via docker-compose).
-- ==========================================================

-- Activer l'extension pgcrypto pour disposer de :
--   - gen_salt('bf') : génère un sel bcrypt
--   - crypt(texte, sel) : calcule le hash (bcrypt ici)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- On crée 2 types ENUM pour restreindre les valeurs possibles
-- (c'est propre et évite les fautes de frappe).
-- DO $$ ... $$ permet d'exécuter un bloc PL/pgSQL.
DO $$
BEGIN
  -- Type ENUM pour le genre
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender_t') THEN
    CREATE TYPE gender_t AS ENUM ('male','female','other','unspecified');
  END IF;

  -- Type ENUM pour l'orientation
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'orientation_t') THEN
    CREATE TYPE orientation_t AS ENUM ('hetero','homo','bi','unspecified');
  END IF;
END$$;

-- ==========================================================
-- Table users : uniquement les colonnes nécessaires à l'auth
-- + created_at / updated_at (très utile en pratique).
-- IMPORTANT : on ne stocke PAS le mot de passe en clair.
-- On stocke "password_hash" (bcrypt).
-- ==========================================================
CREATE TABLE IF NOT EXISTS users (
  id            BIGSERIAL PRIMARY KEY,   -- identifiant auto-incrémenté 64 bits
  email         TEXT NOT NULL UNIQUE,    -- e-mail unique
  username      TEXT NOT NULL UNIQUE,    -- pseudo unique
  password_hash TEXT NOT NULL,           -- hash bcrypt (via crypt() + gen_salt('bf'))
  first_name    TEXT,                    -- prénom (optionnel)
  last_name     TEXT,                    -- nom (optionnel)
  gender        gender_t      NOT NULL DEFAULT 'unspecified',   -- valeur par défaut
  orientation   orientation_t NOT NULL DEFAULT 'unspecified',   -- valeur par défaut
  is_verified   BOOLEAN       NOT NULL DEFAULT FALSE,            -- compte vérifié ?
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),            -- date de création
  updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()             -- maj auto par trigger
);

-- Index "fonctionnels" pour accélérer les recherches insensibles à la casse.
-- (Tu peux ensuite écrire WHERE LOWER(email) = LOWER($1)
--  et Postgres pourra utiliser ce type d'index.)
CREATE INDEX IF NOT EXISTS idx_users_email_ci    ON users (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_users_username_ci ON users (LOWER(username));

-- ==========================================================
-- Trigger pour tenir à jour updated_at à chaque UPDATE.
-- ==========================================================
CREATE OR REPLACE FUNCTION set_users_updated_at() RETURNS trigger AS $fn$
BEGIN
  NEW.updated_at := NOW();  -- met à jour le timestamp
  RETURN NEW;
END;
$fn$ LANGUAGE plpgsql;

-- On (re)crée proprement le trigger (drop si existait)
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_users_updated_at();

-- Fin du schéma