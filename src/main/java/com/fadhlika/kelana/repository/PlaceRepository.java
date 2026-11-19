package com.fadhlika.kelana.repository;

import com.fadhlika.kelana.dto.Feature;
import com.fadhlika.kelana.model.Place;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
public class PlaceRepository {

    public final JdbcClient jdbcClient;

    private final ObjectMapper mapper;

    private final WKBReader wkbReader = new WKBReader();

    private RowMapper<Place> rowMapper = (ResultSet rs, int rowNum) -> {
        byte[] geogBytes = rs.getBytes("lonlat");
        Geometry geog = null;
        if (geogBytes != null) {
            try {
                geog = wkbReader.read(geogBytes);
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        Feature geodata = null;
        InputStream geodataIS = rs.getAsciiStream("geodata");
        if (geodataIS != null) {
            try {
                geodata = ((PlaceRepository) this).mapper.readValue(
                        geodataIS,
                        Feature.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new Place(
                rs.getInt("id"),
                rs.getString("provider"),
                geog,
                rs.getString("type"),
                rs.getString("postcode"),
                rs.getString("country_code"),
                rs.getString("name"),
                rs.getString("country"),
                rs.getString("city"),
                rs.getString("district"),
                rs.getString("locality"),
                rs.getString("street"),
                rs.getString("state"),
                geodata,
                rs.getObject("created_at", OffsetDateTime.class).toZonedDateTime());
    };

    PlaceRepository(JdbcClient jdbcClient, ObjectMapper mapper) {
        this.jdbcClient = jdbcClient;
        this.mapper = mapper;
    }

    public Place createPlace(Place place) throws JsonProcessingException {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient
                .sql("""
                        INSERT INTO place(
                            provider,
                            lonlat,
                            type,
                            postcode,
                            country_code,
                            name,
                            country,
                            city,
                            district,
                            locality,
                            street,
                            state,
                            geodata,
                            created_at
                        ) VALUES (?, ST_GeogFromText(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::json, ?)
                        ON CONFLICT ((geodata->'properties'->'osm_id'::TEXT)) DO NOTHING RETURNING id""")
                .param(place.provider())
                .param(place.lonlat().toText())
                .param(place.type())
                .param(place.postcode())
                .param(place.countryCode())
                .param(place.name())
                .param(place.country())
                .param(place.city())
                .param(place.district())
                .param(place.locality())
                .param(place.street())
                .param(place.state())
                .param(mapper.writeValueAsString(place.geodata()))
                .param(place.createdAt().toOffsetDateTime())
                .update(keyHolder);

        return fetchPlace(keyHolder.getKeyAs(Integer.class)).orElseThrow();
    }

    public Optional<Place> fetchPlace(Geometry lonlat) {
        return jdbcClient
                .sql("""
                        SELECT
                            id,
                            provider,
                            type,
                            postcode,
                            country_code,
                            name,
                            country,
                            city,
                            district,
                            locality,
                            street,
                            state,
                            ST_AsBinary(lonlat) AS lonlat,
                            geodata,
                            created_at
                        FROM place WHERE ST_AsText(lonlat) =  ?""")
                .param(lonlat.toText())
                .query(rowMapper)
                .optional();
    }

    public Optional<Place> fetchPlace(Integer id) {
        return jdbcClient
                .sql("""
                        SELECT
                            id,
                            provider,
                            type,
                            postcode,
                            country_code,
                            name,
                            country,
                            city,
                            district,
                            locality,
                            street,
                            state,
                            ST_AsBinary(lonlat) AS lonlat,
                            geodata,
                            created_at
                        FROM place WHERE id =  ?""")
                .param(id)
                .query(rowMapper)
                .optional();
    }

    public List<Place> fetchPlaces(
            Optional<String> city,
            Optional<String> country,
            Optional<Integer> limit,
            Optional<Integer> offset) {
        List<String> where = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        city.ifPresent(v -> {
            where.add("city = ?");
            args.add(v);
        });

        country.ifPresent(v -> {
            where.add("country = ?");
            args.add(v);
        });

        StringBuilder sb = new StringBuilder(
                """
                        SELECT
                            id,
                            provider,
                            type,
                            postcode,
                            country_code,
                            name,
                            country,
                            city,
                            district,
                            locality,
                            street,
                            state,
                            ST_AsBinary(lonlat) AS lonlat,
                            geodata,
                            created_at
                        FROM place""");

        if (!where.isEmpty()) {
            sb.append(" WHERE ");
            sb.append(String.join(" AND ", where));
        }

        limit.ifPresent(v -> {
            sb.append(" LIMIT ?");
            args.add(v);
        });

        offset.ifPresent(v -> {
            sb.append(" OFFSET ?");
            args.add(v);
        });

        String sql = sb.toString();
        return jdbcClient.sql(sql).params(args).query(rowMapper).list();
    }

    public List<Place> fetchPlaces(Double lon, Double lat, Double rad) {
        return jdbcClient
                .sql(
                        """
                                SELECT
                                    id,
                                    provider,
                                    type,
                                    postcode,
                                    country_code,
                                    name,
                                    country,
                                    city,
                                    district,
                                    locality,
                                    street,
                                    state,
                                    ST_AsBinary(lonlat) AS lonlat,
                                    geodata,
                                    created_at
                                FROM place WHERE ST_DWithin(lonlat, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)""")
                .param(lon)
                .param(lat)
                .param(rad)
                .query(rowMapper)
                .list();
    }

    public List<Place> fetchPlaces(Geometry bounds) {
        return jdbcClient
                .sql("""
                        SELECT
                            id,
                            provider,
                            type,
                            postcode,
                            country_code,
                            name,
                            country,
                            city,
                            district,
                            locality,
                            street,
                            state,
                            ST_AsBinary(lonlat) AS lonlat,
                            geodata,
                            created_at
                        FROM place
                        WHERE ST_DWithin(lonlat, ST_GeogFromText(?))""")
                .param(bounds.toText())
                .query(rowMapper)
                .list();
    }
}
