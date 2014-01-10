SET SESSION AUTHORIZATION "rekord_user";

CREATE TABLE "commentable" (
    "id" BIGSERIAL,
    "count" INT NOT NULL DEFAULT 0,
    "created_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY ("id")
);

CREATE TABLE "user" (
    "id" BIGSERIAL,
    "name" TEXT NOT NULL,
    "state" CHARACTER(1),
    "commentable_id" BIGINT NOT NULL,
    "created_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY ("id"),
    FOREIGN KEY ("commentable_id") REFERENCES "commentable"("id") ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE "user_history" (
    "id" BIGINT NOT NULL,
    "name" TEXT NOT NULL,
    "user_history_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "comment" (
    "id" BIGSERIAL,
    "commentable_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "text" TEXT NOT NULL,
    "created_timestamp" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY ("id"),
    FOREIGN KEY ("commentable_id") REFERENCES "commentable"("id") ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY ("user_id") REFERENCES "user"("id") ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE "crazy" 
(
    "id_0" BIGSERIAL,
    "id_1" BIGSERIAL,
    "diameter" REAL NOT NULL,
    "updates" INT NOT NULL DEFAULT 0,
    "last_modified_tms" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY ("id_0", "id_1")
);

CREATE FUNCTION crazy_update() RETURNS trigger AS $crazy_update$
    BEGIN
        NEW.updates = OLD.updates + 1;
        RETURN NEW;
    END;
$crazy_update$ LANGUAGE plpgsql;

CREATE TRIGGER crazy_update BEFORE UPDATE ON crazy
    FOR EACH ROW EXECUTE PROCEDURE crazy_update();
    
    