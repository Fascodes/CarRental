CREATE TABLE storage(
    "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "car_id" bigint NOT NULL,
    "storage_key" varchar NOT NULL,
    "original_filename" varchar,
    "content_type" varchar NOT NULL,
    "bucket" varchar NOT NULL,
    "size" bigint NOT NULL,
    "uploaded_at" timestamp DEFAULT now()
);

ALTER TABLE "storage" ADD FOREIGN KEY ("car_id") REFERENCES "cars" ("id");