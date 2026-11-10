ALTER TABLE location ADD COLUMN place_id BIGINT;
ALTER TABLE location ADD CONSTRAINT idx_location_place_id FOREIGN KEY(place_id) REFERENCES "place"(id);
