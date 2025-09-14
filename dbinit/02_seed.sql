-- ==========================================================
-- 02_seed_users.sql  (gendered first names + weighted distro)
-- ----------------------------------------------------------
-- * gender : 60% male / 30% female / 10% other
-- * orientation global : 50% hetero / 15% homo / 15% bi / 20% unspecified
--   (unspecified davantage chez female & other)
-- * first_name pris dans une liste selon le gender :
--     - male   -> fn_male
--     - female -> fn_female
--     - other  -> au hasard fn_male ou fn_female
-- * last_name multi-ethnie
-- * mots de passe hashés (bcrypt) via pgcrypto
-- * rejouable (ON CONFLICT DO NOTHING)
-- ==========================================================

WITH params AS (
  SELECT
    600::int        AS n_total,         -- >= 500
    'Password!123'  AS default_pwd,

    -- Répartition GENDER
    0.60::float8    AS p_male,
    0.30::float8    AS p_female,
    0.10::float8    AS p_other,

    -- Part d'ORIENTATION "unspecified" par GENDER (≈20% global)
    0.0833333333::float8 AS u_male,
    0.30::float8         AS u_female,
    0.60::float8         AS u_other,

    -- Répartition interne (hors "unspecified") = 50/15/15
    0.625::float8  AS w_hetero_nonunspec,  -- 50 / 80
    0.1875::float8 AS w_homo_nonunspec,    -- 15 / 80
    0.1875::float8 AS w_bi_nonunspec       -- 15 / 80
),

-- ================== Prénoms par genre =====================
fn_male AS (
  SELECT ARRAY[
    'Alexandre','Amir','Andrei','Arjun','Bilal','Carlos','Daniel','Diego','Dimitri','Elias',
    'Emil','Enzo','Filip','George','Hugo','Ignacio','Isaac','Ivan','Jamal','Javier',
    'Jonas','João','Jordan','José','Julien','Kenji','Kofi','Liam','Luc','Lucas',
    'Luís','Marco','Mateusz','Mateo','Mehdi','Mohamed','Nikola','Omar','Pavel','Pedro',
    'Quentin','Radu','Rahim','Ricardo','Rohan','Saeed','Satoshi','Stefan','Sven','Tariq',
    'Théo','Victor','Vlad','Wei','Youssef','Yusuf','Zinedine','Samir','Nabil','Gonzalo',
    'Sergei','Aziz','Ibrahim','Marius','Hassan','Khalid','Reza','Joon','Seojun','Takeshi'
  ] AS a
),
fn_female AS (
  SELECT ARRAY[
    'Alice','Amina','Ana','Aya','Beatriz','Camila','Carla','Chloé','Daria','Elena',
    'Elif','Emma','Fatima','Farah','Gabriela','Giulia','Hana','Inès','Ivana','Jana',
    'Julia','Karima','Katya','Lara','Leïla','Linh','Lucia','Maria','Maya','Mina',
    'Nadia','Naïma','Naomi','Nia','Noor','Nour','Paula','Priya','Rania','Rosa',
    'Salma','Sara','Sofia','Sunita','Tania','Valentina','Vera','Yara','Yasmine','Yuna',
    'Zahra','Anya','Lea','Noa','Amélie','Haruka','Aisha','Khadija','Minji','Meera',
    'Sanaa','Zoe','Katerina','Ewa','Nuria','Anya','Maya','Priyanka','Yara','Zoe'
  ] AS a
),

-- ================== Noms de famille =======================
ln AS (
  SELECT ARRAY[
    'Durand','Martin','Bernard','Petit','Robert','Dubois','Leroy','Moreau','Simon','Laurent',
    'Lefebvre','Roux','David','Bertrand','Bonnet','Dupont','Lambert','Rousseau','Vincent','García',
    'Martínez','Rodríguez','López','Fernández','González','Pérez','Sánchez','Romero','Torres','Álvarez',
    'Herrera','Silva','Oliveira','Pereira','Souza','Costa','Santos','Rocha','Almeida','Carvalho',
    'Rodrigues','Sousa','Rossi','Russo','Ferrari','Esposito','Romano','Conti','Bruno','Gallo',
    'Greco','De Luca','Müller','Schmidt','Schneider','Fischer','Weber','Becker','Hoffmann','Wagner',
    'Keller','Braun','Smith','Johnson','Williams','Brown','Jones','Miller','Davis','Wilson',
    'Anderson','Taylor','Thomas','Moore','Jackson','Murphy','O''Connor','O''Neill','Campbell','MacLeod',
    'de Vries','Jansen','Bakker','Visser','Johansson','Andersson','Karlsson','Nilsson','Eriksson','Hansen',
    'Johansen','Nielsen','Jensen','Korhonen','Virtanen','Nowak','Kowalski','Wiśniewski','Zieliński','Novák',
    'Svoboda','Kováč','Nagy','Kovács','Ivanov','Petrov','Smirnov','Kuznetsov','Novikov','Shevchenko',
    'Bondarenko','Petrović','Jovanović','Kovačević','Papadopoulos','Nikolaidis','Georgiou','Demir','Şahin',
    'Kaya','Haddad','Rahimi','Mansour','Al-Sayed','Al-Hassan','Benali','Benkacem','El Amrani',
    'Bouzid','Cohen','Levi','Mizrahi','Patel','Singh','Sharma','Gupta','Iyer','Reddy',
    'Nair','Khan','Wang','Li','Zhang','Liu','Chen','Yang','Zhao','Huang',
    'Zhou','Wu','Sato','Suzuki','Takahashi','Tanaka','Watanabe','Ito','Yamamoto','Kobayashi',
    'Kim','Lee','Park','Choi','Jung','Kang','Nguyen','Tran','Le','Pham',
    'Hoang','Okafor','Okoye','Adeyemi','Balogun','Mensah','Boateng','Bekele','Otieno','Njoroge',
    'Diop','Ndiaye','Kouassi','Traoré','Keita','Diarra'
  ] AS a
),

-- =============== Génération des N lignes ===================
seed AS (
  SELECT
    gs.i,
    random() AS r_g,    -- pour choisir le gender
    random() AS r_o,    -- pour choisir l'orientation conditionnée
    random() AS r_fn,   -- pour choisir la liste de prénoms si gender=other
    (random() < 0.70) AS is_verified
  FROM params p
  CROSS JOIN LATERAL generate_series(1, p.n_total) AS gs(i)
),

-- =============== Tirage du GENDER (60/30/10) ===============
gendered AS (
  SELECT
    s.*,
    CASE
      WHEN r_g <  (SELECT p_male   FROM params) THEN 'male'::gender_t
      WHEN r_g < ((SELECT p_male   FROM params) + (SELECT p_female FROM params))
               THEN 'female'::gender_t
      ELSE 'other'::gender_t
    END AS gender_val
  FROM seed s
),

-- ============ 1) TIRAGE DU FIRST_NAME (par ligne) ============
named_first AS (
  SELECT
    g.*,
    pick_fn.first_name
  FROM gendered g
  -- Prend l'array selon le gender puis pioche 1 prénom au hasard
  CROSS JOIN LATERAL (
    SELECT val AS first_name
    FROM unnest(
      CASE g.gender_val
        WHEN 'male'::gender_t   THEN (SELECT a FROM fn_male)
        WHEN 'female'::gender_t THEN (SELECT a FROM fn_female)
        ELSE (SELECT (SELECT a FROM fn_male) || (SELECT a FROM fn_female))
      END
    ) AS val
    ORDER BY gen_random_uuid()
    LIMIT 1
  ) AS pick_fn
),

-- ============ 2) TIRAGE DU LAST_NAME (par ligne) =============
named_last AS (
  SELECT
    f.*,
    pick_ln.last_name
  FROM named_first f
  -- Pioche 1 nom de famille au hasard dans la liste globale
  CROSS JOIN LATERAL (
    SELECT val AS last_name
    FROM unnest((SELECT a FROM ln)) AS val
    ORDER BY gen_random_uuid(), f.i
    LIMIT 1
  ) AS pick_ln
),

-- ====== Orientation conditionnée au GENDER (≈50/15/15/20) ===
oriented AS (
  SELECT
    n.*,
    CASE gender_val
      WHEN 'male'::gender_t THEN
        CASE
          WHEN r_o < (SELECT u_male FROM params) THEN 'unspecified'::orientation_t
          WHEN r_o < (SELECT u_male + (1 - u_male) * w_hetero_nonunspec FROM params) THEN 'hetero'::orientation_t
          WHEN r_o < (SELECT u_male + (1 - u_male) * (w_hetero_nonunspec + w_homo_nonunspec) FROM params) THEN 'homo'::orientation_t
          ELSE 'bi'::orientation_t
        END
      WHEN 'female'::gender_t THEN
        CASE
          WHEN r_o < (SELECT u_female FROM params) THEN 'unspecified'::orientation_t
          WHEN r_o < (SELECT u_female + (1 - u_female) * w_hetero_nonunspec FROM params) THEN 'hetero'::orientation_t
          WHEN r_o < (SELECT u_female + (1 - u_female) * (w_hetero_nonunspec + w_homo_nonunspec) FROM params) THEN 'homo'::orientation_t
          ELSE 'bi'::orientation_t
        END
      WHEN 'other'::gender_t THEN
        CASE
          WHEN r_o < (SELECT u_other FROM params) THEN 'unspecified'::orientation_t
          WHEN r_o < (SELECT u_other + (1 - u_other) * w_hetero_nonunspec FROM params) THEN 'hetero'::orientation_t
          WHEN r_o < (SELECT u_other + (1 - u_other) * (w_hetero_nonunspec + w_homo_nonunspec) FROM params) THEN 'homo'::orientation_t
          ELSE 'bi'::orientation_t
        END
      ELSE 'unspecified'::orientation_t
    END AS orientation_val
  FROM named_last n
)

-- ===================== INSERT FINAL ========================
INSERT INTO users (
  email, username, password_hash,
  first_name, last_name,
  gender, orientation, is_verified
)
SELECT
  LOWER(
    regexp_replace(first_name, '[^A-Za-zÀ-ÿ]', '', 'g')
    || '.' ||
    regexp_replace(last_name,  '[^A-Za-zÀ-ÿ]', '', 'g')
  ) || lpad(i::text, 3, '0') || '@example.com'                                  AS email,

  LOWER(
    substr(regexp_replace(first_name, '[^A-Za-zÀ-ÿ]', '', 'g'), 1, 1)
    || regexp_replace(last_name,  '[^A-Za-zÀ-ÿ]', '', 'g')
  ) || lpad(i::text, 3, '0')                                                    AS username,

  crypt((SELECT default_pwd FROM params), gen_salt('bf'))                       AS password_hash,

  first_name, last_name,
  gender_val, orientation_val, is_verified

FROM oriented
ON CONFLICT (email) DO NOTHING;
