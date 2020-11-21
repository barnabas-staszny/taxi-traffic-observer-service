package com.taxi.traffic.observer.aggregator.dao.jdbc;

import com.taxi.traffic.observer.aggregator.model.AggregatedRideStat;

import java.time.LocalDate;
import java.util.List;

public interface AggregatorRepository {
    List<AggregatedRideStat> query(LocalDate fromLocalDate, int fromHour, LocalDate toLocalDate, int toHour);
}
