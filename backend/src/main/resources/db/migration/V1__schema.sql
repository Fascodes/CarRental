CREATE TYPE "reservations_status" AS ENUM (
  'PENDING',
  'RENTER_CONFIRMED',
  'CONFIRMED',
  'ACTIVE',
  'CANCELLED',
  'COMPLETED'
);

CREATE TYPE "listings_status" AS ENUM (
  'ACTIVE',
  'INACTIVE'
);

CREATE TYPE "user_role" AS ENUM (
  'USER',
  'ADMIN'
);

CREATE TYPE "gearbox_type" AS ENUM (
  'AUTOMATIC',
  'MANUAL'
);

CREATE TABLE "reservations" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "listing_id" bigint NOT NULL,
  "owner_id" bigint NOT NULL,
  "renter_id" bigint NOT NULL,
  "status" reservations_status,
  "date_start" timestamp,
  "date_end" timestamp,
  "created_at" timestamp
);

CREATE TABLE "listings" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "car_id" bigint NOT NULL,
  "user_id" bigint NOT NULL,
  "price" integer,
  "status" listings_status,
  "title" varchar,
  "localization" varchar,
  "body" text,
  "created_at" timestamp,
  "last_active" timestamp
);

CREATE TABLE "users" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "username" varchar,
  "email" varchar UNIQUE,
  "password" varchar,
  "role" user_role,
  "last_online" timestamp,
  "created_at" timestamp
);

CREATE TABLE "cars" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "owner_id" bigint,
  "brand" varchar,
  "model" varchar,
  "model_year" integer,
  "vin" varchar(17),
  "seat_number" integer,
  "gearbox" gearbox_type,
  "horse_power" integer,
  "avg_liters_per_hundred_km" numeric(5,2),
  "addtional_information" text
);

CREATE TABLE "notifications" (
  "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  "user_email" varchar NOT NULL,
  "message" varchar NOT NULL,
  "reservation_id" bigint REFERENCES reservations(id),
  "is_read" boolean DEFAULT false,
  "created_at" timestamp DEFAULT now()
);

COMMENT ON COLUMN "listings"."body" IS 'Content of the listing';

COMMENT ON COLUMN "cars"."addtional_information" IS 'Car information';

ALTER TABLE "reservations" ADD FOREIGN KEY ("listing_id") REFERENCES "listings" ("id");

ALTER TABLE "reservations" ADD FOREIGN KEY ("owner_id") REFERENCES "users" ("id");

ALTER TABLE "reservations" ADD FOREIGN KEY ("renter_id") REFERENCES "users" ("id");

ALTER TABLE "listings" ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");

ALTER TABLE "listings" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "cars" ADD FOREIGN KEY ("owner_id") REFERENCES "users" ("id");
