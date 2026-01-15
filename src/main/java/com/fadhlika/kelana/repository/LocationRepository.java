/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.fadhlika.kelana.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fadhlika.kelana.dto.FeatureCollection;
import com.fadhlika.kelana.model.Location;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.MappedQuerySpec;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author fadhl
 */
@Repository
public class LocationRepository {

    private static final Logger logger = LoggerFactory.getLogger(LocationRepository.class);

    @Autowired
    private JdbcClient jdbcClient;

    private final WKBReader wkbReader = new WKBReader();

    @Autowired
    private ObjectMapper mapper;

    private final RowMapper<Location> locationRowMapper = new RowMapper<Location>() {
        @Override
        public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
            Location location;
            byte[] point = rs.getBytes("geometry");
            try {
                location = new Location(wkbReader.read(point));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            location.setId(rs.getInt("id"));
            location.setDeviceId(rs.getString("device_id"));
            location.setAltitude(rs.getInt("altitude"));
            location.setCourse(rs.getInt("course"));
            location.setSpeed(rs.getDouble("speed"));
            location.setAccuracy(rs.getInt("accuracy"));
            location.setVerticalAccuracy(rs.getInt("vertical_accuracy"));
            location.setBatteryState(rs.getInt("battery_state"));
            location.setBattery(rs.getDouble("battery"));
            location.setSsid(rs.getString("ssid"));
            location.setTimestamp(rs.getObject("timestamp", OffsetDateTime.class).toZonedDateTime());
            location.setCourseAccuracy(rs.getInt("course_accuracy"));
            location.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class).toZonedDateTime());
            location.setRawData(rs.getString("raw_data"));

            Array motions = rs.getArray("motions");
            if (motions != null)
                location.setMotions(Arrays.stream((String[]) motions.getArray()).map(e -> e).toList());

            InputStream geocode = rs.getAsciiStream("geocode");
            if (geocode != null) {
                try {
                    location.setGeocode(mapper.readValue(geocode, FeatureCollection.class));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return location;
        }
    };

    public void createLocation(Location location) throws DataAccessException, JsonProcessingException {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO location(");
        if (location.getId() != 0) {
            sqlBuilder.append("id, ");
        }
        sqlBuilder.append("""
                user_id,
                device_id,
                geometry,
                altitude,
                course,
                course_accuracy,
                speed,
                accuracy,
                vertical_accuracy,
                motions,
                battery_state,
                battery,
                ssid,
                timestamp,
                raw_data,
                created_at,
                import_id,
                geocode) VALUES(""");
        if (location.getId() != 0) {
            sqlBuilder.append("?, ");
        }
        sqlBuilder
                .append("""
                        ?, ?, ST_GeomFromText(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?::jsonb)
                        ON CONFLICT (user_id, device_id, geometry, timestamp, import_id) DO UPDATE SET
                            altitude = excluded.altitude,
                            course = excluded.course,
                            course_accuracy = excluded.course_accuracy,
                            speed = excluded.speed,
                            accuracy = excluded.accuracy,
                            vertical_accuracy = excluded.vertical_accuracy,
                            motions = excluded.motions,
                            battery_state = excluded.battery_state,
                            battery = excluded.battery,
                            ssid = excluded.ssid,
                            raw_data = excluded.raw_data,
                            created_at = excluded.created_at,
                            import_id = excluded.import_id,
                            geocode = excluded.geocode
                        """);

        StatementSpec stmt = jdbcClient.sql(sqlBuilder.toString());
        if (location.getId() != 0) {
            stmt = stmt.param(location.getId());
        }
        stmt.param(location.getUserId())
                .param(location.getDeviceId())
                .param(location.getGeometry().toText())
                .param(location.getAltitude())
                .param(location.getCourse())
                .param(location.getCourseAccuracy())
                .param(location.getSpeed())
                .param(location.getAccuracy())
                .param(location.getVerticalAccuracy())
                .param(location.getMotions())
                .param(location.getBatteryState().value)
                .param(location.getBattery())
                .param(location.getSsid())
                .param(location.getTimestamp().toOffsetDateTime())
                .param(location.getRawData())
                .param(location.getCreatedAt().toOffsetDateTime())
                .param(location.getImportId())
                .param(mapper.writeValueAsString(location.getGeocode()))
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
        return findLocationsStatementSpecBuilder(userId,
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

    public MappedQuerySpec<Location> findLocationsStatementSpecBuilder(
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

        List<Object> args = new ArrayList<Object>();

        id.ifPresent((v) -> {
            where.add("id = ?");
            args.add(v);
        });

        userId.ifPresent((v) -> {
            where.add("user_id = ?");
            args.add(v);
        });

        if (start.isPresent() && end.isPresent()) {
            where.add("timestamp BETWEEN ? AND ?");
            args.add(start.get().toOffsetDateTime());
            args.add(end.get().toOffsetDateTime());
        } else if (start.isPresent()) {
            where.add("timestamp < ?");
            args.add(start.get());
        }

        device.ifPresent((d) -> {
            where.add("device_id = ?");
            args.add(d);
        });

        geocoded.ifPresent((v) -> {
            if (v)
                where.add("geocode != jsonb('null')");
            else
                where.add("geocode = jsonb('null')");
        });

        bounds.ifPresent((b) -> {
            where.add("ST_CoveredBy(geometry, ST_GeomFromText(?))");
            args.add(b);
        });

        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT
                    id,
                    user_id,
                    device_id,
                    ST_AsBinary(geometry) AS geometry,
                    altitude,
                    course,
                    speed,
                    accuracy,
                    vertical_accuracy,
                    motions,
                    battery_state,
                    battery,
                    ssid,
                    raw_data::json AS raw_data,
                    timestamp,
                    created_at,
                    import_id,
                    course_accuracy,
                    geocode::json AS geocode FROM location""");
        if (!where.isEmpty()) {
            sqlBuilder.append(" WHERE ");
            sqlBuilder.append(String.join(" AND ", where));
        }

        sqlBuilder.append(" ORDER BY ");
        sqlBuilder.append(order.orElse("timestamp"));

        desc.ifPresent((v) -> {
            if (v)
                sqlBuilder.append(" DESC");
        });

        limit.ifPresent((l) -> {
            sqlBuilder.append(" LIMIT ?");
            args.add(l);
        });

        offset.ifPresent((o) -> {
            sqlBuilder.append(" OFFSET ?");
            args.add(o);
        });

        return jdbcClient.sql(sqlBuilder.toString()).params(args).query(locationRowMapper);
    }

    public void updateLocationGeocode(int id, FeatureCollection geocode) throws JsonProcessingException {
        jdbcClient.sql("UPDATE location SET geocode = ?::jsonb WHERE id = ?")
                .param(mapper.writeValueAsString(geocode)).param(id).update();
    }
}
