/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.fadhlika.kelana.repository;

import com.fadhlika.kelana.dto.Feature;
import com.fadhlika.kelana.model.Location;
import com.fadhlika.kelana.model.Place;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.InputStream;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.MappedQuerySpec;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

/**
 * @author fadhl
 */
@Repository
public class LocationRepository {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(
            LocationRepository.class);

    private final JdbcClient jdbcClient;

    private final WKBReader wkbReader = new WKBReader();

    private final ObjectMapper mapper;

    private final RowMapper<Location> locationRowMapper = new RowMapper<Location>() {
        @Override
        public Location mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            Geometry geom;
            try {
                geom = wkbReader.read(rs.getBytes("location_lonlat"));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Location location = new Location(geom);
            location.setId(rs.getInt("id"));
            location.setDeviceId(rs.getString("device_id"));
            location.setAltitude(rs.getInt("altitude"));
            location.setCourse(rs.getInt("course"));
            location.setSpeed(rs.getDouble("speed"));
            location.setAccuracy(rs.getInt("accuracy"));
            location.setVerticalAccuracy(rs.getInt("vertical_accuracy"));
            location.setBatteryState(rs.getInt("battery_state"));
            location.setBattery(rs.getDouble("battery"));
            location.setPressure(rs.getDouble("pressure"));
            location.setSsid(rs.getString("ssid"));
            location.setTimestamp(
                    rs
                            .getObject("timestamp", OffsetDateTime.class)
                            .toZonedDateTime());
            location.setCourseAccuracy(rs.getInt("course_accuracy"));
            location.setCreatedAt(
                    rs
                            .getObject("created_at", OffsetDateTime.class)
                            .toZonedDateTime());
            location.setRawData(rs.getString("location_raw_data"));

            Array motions = rs.getArray("motions");
            if (motions != null)
                location.setMotions(
                        Arrays.stream((String[]) motions.getArray())
                                .map(e -> e)
                                .toList());

            int placeId = rs.getInt("place_id");
            if (placeId > 0) {
                try {
                    byte[] lonlat = rs.getBytes("place_lonlat");
                    if (lonlat != null) {
                        geom = wkbReader.read(lonlat);
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Feature placeFeature = null;
                InputStream geodata = rs.getAsciiStream("geodata");
                if (geodata != null) {
                    try {
                        placeFeature = mapper.readValue(geodata, Feature.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                Place place = new Place(
                        rs.getInt("place_id"),
                        rs.getString("provider"),
                        geom,
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
                        placeFeature,
                        rs.getObject("created_at", OffsetDateTime.class).toZonedDateTime());

                location.setPlace(place);
            }

            return location;
        }
    };

    LocationRepository(JdbcClient jdbcClient, ObjectMapper mapper) {
        this.jdbcClient = jdbcClient;
        this.mapper = mapper;
    }

    public void createLocation(Location location)
            throws DataAccessException, JsonProcessingException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO location(");
        if (location.getId() != 0) {
            sqlBuilder.append("id, ");
        }
        sqlBuilder.append("""
                user_id,
                device_id,
                lonlat,
                altitude,
                course,
                course_accuracy,
                speed,
                accuracy,
                vertical_accuracy,
                motions,
                battery_state,
                battery,
                pressure,
                ssid,
                timestamp,
                raw_data,
                created_at,
                import_id) VALUES(""");
        if (location.getId() != 0) {
            sqlBuilder.append(":id, ");
        }
        sqlBuilder.append("""
                :user_id, :device_id, ST_GeogFromText(:lonlat), :altitude, :course, :course_accuracy,
                :speed, :accuracy, :vertical_accuracy, :motions::text[], :battery_state, :battery, :pressure,
                :ssid, :timestamp, :raw_data::json, :created_at, :import_id)
                ON CONFLICT (user_id, device_id, lonlat, timestamp) DO UPDATE SET
                            altitude = excluded.altitude,
                            course = excluded.course,
                            course_accuracy = excluded.course_accuracy,
                            speed = excluded.speed,
                            accuracy = excluded.accuracy,
                            vertical_accuracy = excluded.vertical_accuracy,
                            motions = excluded.motions,
                            battery_state = excluded.battery_state,
                            battery = excluded.battery,
                            pressure = excluded.pressure,
                            ssid = excluded.ssid,
                            raw_data = excluded.raw_data,
                            created_at = excluded.created_at,
                            import_id = excluded.import_id
                        """);

        StatementSpec stmt = jdbcClient.sql(sqlBuilder.toString());
        if (location.getId() > 0) {
            stmt = stmt.param("id", location.getId());
        }
        stmt = stmt
                .param("user_id", location.getUserId())
                .param("device_id", location.getDeviceId())
                .param("lonlat", location.getLonlat().toText())
                .param("altitude", location.getAltitude())
                .param("course", location.getCourse())
                .param("course_accuracy", location.getCourseAccuracy())
                .param("speed", location.getSpeed())
                .param("accuracy", location.getAccuracy())
                .param("vertical_accuracy", location.getVerticalAccuracy());

        String motionParam = null;
        if (location.getMotions() != null) {
            motionParam = "{" + String.join(",", location.getMotions()) + "}";
        }

        stmt.param("motions", motionParam)
                .param("battery_state", location.getBatteryState().value)
                .param("battery", location.getBattery())
                .param("pressure", location.getPressure())
                .param("ssid", location.getSsid())
                .param("timestamp", location.getTimestamp().toOffsetDateTime())
                .param("raw_data", location.getRawData())
                .param("created_at", location.getCreatedAt().toOffsetDateTime())
                .param("import_id", location.getImportId())
                .param("geocode", mapper.writeValueAsString(location.getGeocode()))
                .update();
    }

    public List<Location> findLocations(
            Optional<Integer> userId,
            Optional<ZonedDateTime> start,
            Optional<ZonedDateTime> end,
            Optional<String> device,
            Optional<String> order,
            Optional<Boolean> desc,
            Optional<Integer> offset,
            Optional<Integer> limit,
            Optional<Geometry> bounds) {
        return findLocationsStatementSpecBuilder(
                userId,
                start,
                end,
                device,
                order,
                desc,
                offset,
                limit,
                Optional.empty(),
                Optional.empty(),
                bounds).list();
    }

    public Optional<Location> findLocation(
            Optional<Integer> userId,
            Optional<ZonedDateTime> start,
            Optional<ZonedDateTime> end,
            Optional<String> device,
            Optional<String> order,
            Optional<Boolean> desc,
            Optional<Boolean> geocoded) {
        return findLocationsStatementSpecBuilder(
                userId,
                start,
                end,
                device,
                order,
                desc,
                Optional.empty(),
                Optional.of(1),
                Optional.empty(),
                geocoded,
                Optional.empty()).optional();
    }

    public Location findLocation(int id) {
        return findLocationsStatementSpecBuilder(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(1),
                Optional.of(id),
                Optional.empty(),
                Optional.empty()).single();
    }

    private MappedQuerySpec<Location> findLocationsStatementSpecBuilder(
            Optional<Integer> userId,
            Optional<ZonedDateTime> start,
            Optional<ZonedDateTime> end,
            Optional<String> device,
            Optional<String> order,
            Optional<Boolean> desc,
            Optional<Integer> offset,
            Optional<Integer> limit,
            Optional<Integer> id,
            Optional<Boolean> geocoded,
            Optional<Geometry> bounds) {
        List<String> where = new ArrayList<>();

        HashMap<String, Object> args = new HashMap<String, Object>();

        id.ifPresent(v -> {
            where.add("location.id = :id");
            args.put("id", v);
        });

        userId.ifPresent(v -> {
            where.add("location.user_id = :user_id");
            args.put("user_id", v);
        });

        if (start.isPresent() && end.isPresent()) {
            where.add("location.timestamp BETWEEN :start AND :end");
            args.put("start", start.get().toOffsetDateTime());
            args.put("end", end.get().toOffsetDateTime());
        } else if (start.isPresent()) {
            where.add("location.timestamp < :timestamp");
            args.put("timestamp", start.get());
        }

        device.ifPresent(d -> {
            where.add("location.device_id = :device_id");
            args.put("device_id", d);
        });

        geocoded.ifPresent(v -> {
            if (v)
                where.add("location.place_id IS NOT NULL");
            else
                where.add("location.place_id IS NULL");
        });

        bounds.ifPresent(b -> {
            where.add("ST_CoveredBy(lonlat, ST_GeogFromText(:bounds))");
            args.put("bounds", b.toText());
        });

        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT
                    location.id,
                    location.user_id,
                    location.device_id,
                    ST_AsBinary(location.lonlat) AS location_lonlat,
                    location.altitude,
                    location.course,
                    location.speed,
                    location.accuracy,
                    location.vertical_accuracy,
                    location.motions,
                    location.battery_state,
                    location.battery,
                    location.pressure,
                    location.ssid,
                    location.raw_data::json AS location_raw_data,
                    location.timestamp,
                    location.created_at,
                    location.import_id,
                    location.course_accuracy,
                    location.place_id,
                    place.provider,
                    place.type,
                    place.postcode,
                    place.country_code,
                    place.name,
                    place.country,
                    place.city,
                    place.district,
                    place.locality,
                    place.street,
                    place.state,
                    ST_AsBinary(place.lonlat) AS place_lonlat,
                    place.geodata,
                    place.created_at
                FROM location
                LEFT JOIN place ON location.place_id = place.id""");
        if (!where.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            sqlBuilder.append(String.join(" AND ", where));
        }

        sqlBuilder.append(" ORDER BY ");
        sqlBuilder.append(order.orElse("location.timestamp"));

        desc.ifPresent(v -> {
            if (v)
                sqlBuilder.append(" DESC");
        });

        limit.ifPresent(l -> {
            sqlBuilder.append(" LIMIT :limit");
            args.put("limit", l);
        });

        offset.ifPresent(o -> {
            sqlBuilder.append(" OFFSET :offset");
            args.put("offset", o);
        });

        return jdbcClient
                .sql(sqlBuilder.toString())
                .params(args)
                .query(locationRowMapper);
    }

    public void updatePlace(int id, int placeId)
            throws JsonProcessingException {
        jdbcClient
                .sql("UPDATE location SET place_id = ? WHERE id = ?")
                .param(placeId)
                .param(id)
                .update();
    }
}
