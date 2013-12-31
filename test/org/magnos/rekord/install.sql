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
