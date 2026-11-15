ALTER TABLE location RENAME COLUMN geometry TO lonlat;
DROP INDEX idx_geometry_timestamp;
CREATE UNIQUE INDEX idx_lonlat_timestamp ON location(user_id, device_id, lonlat, timestamp);

ALTER TABLE region RENAME COLUMN geometry TO lonlat;
ALTER TABLE place RENAME COLUMN geometry TO lonlat;
