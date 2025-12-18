CREATE TYPE continent_enum AS ENUM ('AFRICA', 'EUROPA', 'ASIA', 'AMERICA');
CREATE TYPE player_position_enum AS ENUM ('GK', 'DEF', 'MIDF', 'STR');

CREATE TABLE team (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    continent continent_enum NOT NULL
);

CREATE TABLE player (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    position player_position_enum NOT NULL,
    id_team INT,
    CONSTRAINT fk_team FOREIGN KEY (id_team)
        REFERENCES team(id)
        ON DELETE SET NULL
);


INSERT INTO team (id, name, continent) VALUES
(1, 'Real Madrid', 'EUROPA'),
(2, 'FC Barcelona', 'EUROPA'),
(3, 'Atletico Madrid', 'EUROPA'),
(4, 'Al Ahly', 'AFRICA'),
(5, 'Inter Miami', 'AMERICA'),
(6, 'Bayern Munich', 'EUROPA'),
(7, 'Manchester City', 'EUROPA');

INSERT INTO player (id, name, age, position, team_id) VALUES
(1, 'Lionel Messi', 36, 'STR', 2),
(2, 'Robert Lewandowski', 35, 'STR', 1),
(3, 'Kylian Mbappé', 25, 'STR', 2),
(4, 'Mohamed Salah', 31, 'STR', 3),
(5, 'Gabriel Barbosa', 27, 'STR', 4),
(6, 'Manuel Neuer', 38, 'GK', 6),
(7, 'Kevin De Bruyne', 32, 'MIDF', 7),
(8, 'Virgil van Dijk', 32, 'DEF', 7),
(9, 'Joueur Sans Équipe', 22, 'MIDF', NULL);
