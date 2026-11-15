DROP INDEX idx_unique_place;
CREATE UNIQUE INDEX idx_unique_place_osm_id ON place((geodata->'properties'->'osm_id'::TEXT));
