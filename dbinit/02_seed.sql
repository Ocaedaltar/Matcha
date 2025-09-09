INSERT INTO users (name)
SELECT 'User_' || generate_series(1, 500);


-- TODO: A refaire