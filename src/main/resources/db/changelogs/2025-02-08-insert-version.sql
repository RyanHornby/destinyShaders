-- liquibase formatted sql

-- changeset Ryan_Hornby:2 runAlways:true
-- preconditions onFail:CONTINUE
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM version
INSERT INTO version VALUES ("0")
