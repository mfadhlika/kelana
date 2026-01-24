CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE "user"(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    "password" TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE "import"
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    INTEGER  NOT NULL,
    source     TEXT     NOT NULL,
    filename   TEXT     NOT NULL,
    path       TEXT,
    content    BYTEA,
    checksum   TEXT     NOT NULL UNIQUE,
    done       BOOLEAN  NOT NULL DEFAULT false,
    count      INTEGER,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES "user"(id)
);

CREATE TABLE "location"(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER NOT NULL,
    device_id TEXT,
    geometry GEOMETRY NOT NULL,
    altitude INTEGER,
    course INTEGER,
    speed FLOAT,
    accuracy INTEGER,
    vertical_accuracy INTEGER,
    battery_state INTEGER,
    battery FLOAT,
    ssid TEXT,
    timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, 
    import_id INTEGER REFERENCES "import"(id), 
    course_accuracy INTEGER, 
    motions TEXT[], 
    raw_data JSONB, 
    geocode JSONB,
    FOREIGN KEY(user_id) REFERENCES "user"(id)
);

CREATE UNIQUE INDEX idx_geometry_timestamp ON location(user_id, device_id, geometry, timestamp, import_id);

CREATE TABLE "export"
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    INTEGER  NOT NULL,
    filename   TEXT     NOT NULL,
    start_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    end_at     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    content    BYTEA,
    done       BOOLEAN  NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES "user"(id)
);

CREATE UNIQUE INDEX idx_user_id_filename ON export(user_id, filename);

CREATE TABLE "integration"
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER  NOT NULL UNIQUE,
    owntracks_username TEXT,
    owntracks_password TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, 
    overland_api_key TEXT,
    FOREIGN KEY(user_id) REFERENCES "user"(id)
);

CREATE TABLE region (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INTEGER NOT NULL,
    "desc" TEXT,
    geometry GEOMETRY NOT NULL,
    beacon_uuid UUID,
    beacon_major INTEGER,
    beacon_minor INTEGER,
    rid TEXT UNIQUE,
    geocode JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_unique_beacon ON region(beacon_uuid, beacon_major, beacon_minor);

CREATE TABLE "trip"
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    INTEGER  NOT NULL,
    title      TEXT,
    start_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    end_at     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP, 
    uuid       UUID UNIQUE NOT NULL,
    is_public BOOLEAN DEFAULT false,
    FOREIGN KEY(user_id) REFERENCES "user"(id)
);

CREATE TABLE place(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    provider TEXT NOT NULL,
    type TEXT,
    postcode TEXT,
    country_code TEXT,
    name TEXT,
    country TEXT,
    city TEXT,
    district TEXT,
    locality TEXT,
    street TEXT,
    state TEXT,
    geometry GEOMETRY,
    geodata JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_unique_place ON place(type, postcode, country_code, name, country, city, district, locality, street, state);

CREATE TABLE owntracks_mqtt_message(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    topic TEXT NOT NULL,
    payload JSONB,
    status TEXT DEFAULT 'RECEIVED',
    reason TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
