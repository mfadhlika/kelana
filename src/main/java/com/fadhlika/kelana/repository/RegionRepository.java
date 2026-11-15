package com.fadhlika.kelana.repository;

import java.io.InputStream;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.fadhlika.kelana.dto.FeatureCollection;
import com.fadhlika.kelana.model.Region;
import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

@Repository
public class RegionRepository {
    private final JdbcClient jdbcClient;

    private final ObjectMapper mapper;

    private final WKBReader wkbReader = new WKBReader();

    private final RowMapper<Region> rowMapper = (ResultSet rs, int rowNum) -> {
        FeatureCollection geocode = null;
        InputStream geocodeIS = rs.getBinaryStream("geocode");
        if (geocodeIS != null) {
            try {
                geocode = ((RegionRepository) this).mapper.readValue(geocodeIS, FeatureCollection.class);
            } catch (Exception e) {
            }
        }
        byte[] point = rs.getBytes("lonlat");
        Geometry lonlat;
        try {
            lonlat = wkbReader.read(point);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return new Region(rs.getInt("id"), rs.getInt("user_id"), rs.getString("desc"), lonlat,
                rs.getString("beacon_uuid"), rs.getInt("beacon_major"),
                rs.getInt("beacon_minor"), rs.getString("rid"), geocode,
                rs.getObject("created_at", OffsetDateTime.class).toZonedDateTime());
    };

    RegionRepository(JdbcClient jdbcClient, ObjectMapper mapper) {
        this.jdbcClient = jdbcClient;
        this.mapper = mapper;
    }

    public void createRegions(List<Region> regions) throws JsonProcessingException {
        List<String> params = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Region region : regions) {
            params.add("(?, ?, ST_GeogFromText(?), ?, ?, ?, ?, ?::jsonb, ?)");
            values.add(region.getUserId());
            values.add(region.getDesc());
            values.add(region.getLonlat().toText());
            values.add(region.getBeaconUUID());
            values.add(region.getBeaconMajor());
            values.add(region.getBeaconMinor());
            values.add(region.getRid());
            values.add(mapper.writeValueAsString(region.getGeocode()));
            values.add(region.getCreatedAt().toOffsetDateTime());
        }
        jdbcClient
                .sql(String.format(
                        """
                                INSERT INTO region(user_id, "desc", lonlat, beacon_uuid, beacon_major, beacon_minor, rid, geocode, created_at)
                                VALUES %s
                                ON CONFLICT (rid) DO UPDATE SET
                                    "desc" = excluded."desc",
                                    lonlat = excluded.lonlat,
                                    beacon_uuid = excluded.beacon_uuid,
                                    beacon_major = excluded.beacon_major,
                                    beacon_minor = excluded.beacon_minor""",
                        String.join(", ", params)))
                .params(values)
                .update();
    }

    public List<Region> fetchRegions(int userId) {
        return jdbcClient.sql(
                "SELECT id, user_id, \"desc\", ST_AsBinary(lonlat) AS lonlat,  beacon_uuid, beacon_major, beacon_minor, rid, geocode, created_at FROM region WHERE user_id = ?")
                .param(userId).query(rowMapper).list();
    }
}
