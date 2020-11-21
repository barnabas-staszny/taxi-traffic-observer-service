package com.taxi.traffic.observer.aggregator.dao.jdbc;

import com.taxi.traffic.observer.aggregator.model.AggregatedRideStat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class AggregatorRepositoryJdbcImpl implements AggregatorRepository {

    @Autowired
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String AGGREGATED_HOURLY_DATA_SELECT = "SELECT " +
            "TO_CHAR(message_timestamp,'YYYY-MM-DD HH24') as hour_of_aggregation, " +
            "SUM(case  " +
            "when ride_status = 'PICKUP' then 1 " +
            "else 0 " +
            "end) as PICKUP_COUNTER, " +
            "SUM(case " +
            "when ride_status = 'DROPOFF' then 1 " +
            "else 0 " +
            "end) as DROPOFF_COUNTER, " +
            "SUM(case " +
            "when ride_status = 'PICKUP' then passenger_count " +
            "else 0 " +
            "end) as PICKUP_PASSENGERS_COUNTER, " +
            "SUM(case " +
            "when ride_status = 'DROPOFF' then passenger_count " +
            "else 0 " +
            "end) as DROPOFF_PASSENGERS_COUNTER " +
            "FROM taxi_ride " +
            "WHERE message_timestamp >= TO_TIMESTAMP(TO_CHAR(:from_local_date,'YYYY-MM-DD ') || (:from_hour::text) ,'YYYY-MM-DD HH24' )" +
            "and message_timestamp <= TO_TIMESTAMP(TO_CHAR(:to_local_date,'YYYY-MM-DD ') || (:to_hour::text) ,'YYYY-MM-DD HH24' )" +
            "group by " +
            "TO_CHAR(message_timestamp,'YYYY-MM-DD HH24')";

    @Override
    public List<AggregatedRideStat> query(LocalDate fromLocalDate, int fromHour, LocalDate toLocalDate, int toHour) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("from_local_date", fromLocalDate);
        params.addValue("from_hour", fromHour);
        params.addValue("to_local_date", toLocalDate);
        params.addValue("to_hour", toHour);
        return namedParameterJdbcTemplate.query(AGGREGATED_HOURLY_DATA_SELECT, params, new AggregatedRideStatRowMapper());
    }

    private class AggregatedRideStatRowMapper implements RowMapper<AggregatedRideStat> {

        @Override
        public AggregatedRideStat mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AggregatedRideStat.builder()
                    .hourOfAggregation(rs.getString("hour_of_aggregation"))
                    .pickupCounter(rs.getLong("PICKUP_COUNTER"))
                    .dropoffCounter(rs.getLong("DROPOFF_COUNTER"))
                    .pickupPassengersCounter(rs.getLong("PICKUP_PASSENGERS_COUNTER"))
                    .dropoffPassengersCounter(rs.getLong("DROPOFF_PASSENGERS_COUNTER"))
                    .build();
        }
    }

}
