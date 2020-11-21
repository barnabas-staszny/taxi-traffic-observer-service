package com.taxi.traffic.observer.aggregator.dao.jpa;

import com.taxi.traffic.observer.aggregator.model.AggregatedRideStat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregatedRideStatRepository extends CrudRepository<AggregatedRideStat, String> {

}
