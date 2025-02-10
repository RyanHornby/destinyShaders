-- liquibase formatted sql

-- changeset Ryan_Hornby:1
CREATE TABLE version (version TEXT NOT NULL);
CREATE TABLE shaders (id INTEGER PRIMARY KEY,
                      name TEXT NOT NULL,
                      image_path TEXT NOT NULL,
                      inner_center TEXT NOT NULL,
                      outer_center TEXT NOT NULL,
                      trim_upper TEXT NOT NULL,
                      trim_lower TEXT NOT NULL,
                      left TEXT NOT NULL,
                      right TEXT NOT NULL,
                      up TEXT NOT NULL,
                      down TEXT NOT NULL);