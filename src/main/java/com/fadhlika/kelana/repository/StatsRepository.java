package com.fadhlika.kelana.repository;

import com.fadhlika.kelana.dto.Stats;

import java.sql.Array;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class StatsRepository {

    private final JdbcClient jdbClient;

    private final RowMapper<Stats> rowMapper = (ResultSet rs, int rowNum) -> {

        OffsetDateTime lastPointTimestampOS = rs.getObject("last_point_timestamp", OffsetDateTime.class);
        ZonedDateTime lastPointTimestamp = null;
        if (lastPointTimestampOS != null) {
            lastPointTimestamp = lastPointTimestampOS.toZonedDateTime();
        }

        Array arr = rs.getArray("visited_cities");
        String[] objects = (String[]) arr.getArray();

        List<String> visitedCities = new ArrayList<String>();
        for (String el : objects) {
            visitedCities.add((el));
        }

        arr = rs.getArray("visited_countries");
        objects = (String[]) arr.getArray();

        List<String> visitedCountries = new ArrayList<String>();
        for (String el : objects) {
            visitedCountries.add((el));
        }

        return new Stats(
                rs.getInt("total_points"),
                rs.getInt("total_reverse_geocoded_points"),
                rs.getInt("total_visited_cities"),
                rs.getInt("total_visited_countries"),
                visitedCities,
                visitedCountries,
                lastPointTimestamp);
    };

    StatsRepository(JdbcClient jdbClient) {
        this.jdbClient = jdbClient;
    }

    public Stats getStats(int userId) {
        return jdbClient
                .sql("""
                        WITH user_location AS (
                        	SELECT
                                geocode,
                                geocode->'features'->0->'properties'->>'city' AS city,
                                geocode->'features'->0->'properties'->>'country' AS country,
                                "timestamp"
                            FROM location WHERE user_id = ?
                        ),
                        cities AS (
                        	SELECT
                        		city,
                        		ROW_NUMBER() OVER (ORDER BY "timestamp") - ROW_NUMBER() OVER (PARTITION BY city ORDER BY "timestamp") AS grp,
                        		"timestamp"
                        	FROM user_location
                        	WHERE city IS NOT NULL
                        	ORDER BY "timestamp"
                        ),
                        filtered_cities AS (
                        	SELECT
                        		COUNT(city),
                        		city,
                        		MAX("timestamp")-MIN("timestamp") AS duration
                        	FROM cities
                        	GROUP by city, grp
                        	HAVING
                        		COUNT(city) > 1 AND
                        		MAX("timestamp")-MIN("timestamp") > INTERVAL '60 minutes'
                        	ORDER BY MIN("timestamp")
                        )
                        SELECT
                        	(SELECT COUNT(1) FROM user_location) AS total_points,
                        	(SELECT COUNT(1) FROM user_location WHERE geocode IS NOT NULL) AS total_reverse_geocoded_points,
                        	(SELECT ARRAY_AGG(DISTINCT city) FROM filtered_cities) AS visited_cities,
                        	(SELECT ARRAY_AGG(DISTINCT country) FROM user_location) AS visited_countries,
                        	(SELECT COUNT(DISTINCT city) FROM filtered_cities) AS total_visited_cities,
                        	(SELECT COUNT(DISTINCT country) FROM user_location) AS total_visited_countries,
                        	(SELECT MAX("timestamp") FROM user_location) AS last_point_timestamp;""")
                .param(userId)
                .query(rowMapper)
                .single();
    }
}
