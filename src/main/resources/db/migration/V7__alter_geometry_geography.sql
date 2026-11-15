ALTER TABLE location ALTER COLUMN lonlat TYPE Geography(POINT, 4326) USING ST_Transform(ST_SetSRID(ST_Force2D(lonlat), 4326), 4326)::Geography(POINT);
ALTER TABLE region ALTER COLUMN lonlat TYPE Geography(POLYGON, 4326) USING ST_Transform(ST_SetSRID(ST_Force2D(lonlat), 4326), 4326)::Geography(POLYGON);
ALTER TABLE place ALTER COLUMN lonlat TYPE Geography(POINT, 4326) USING ST_Transform(ST_SetSRID(ST_Force2D(lonlat), 4326), 4326)::Geography(POINT);
