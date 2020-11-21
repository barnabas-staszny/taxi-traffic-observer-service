package com.taxi.traffic.observer.aggregator.business;

import com.taxi.traffic.observer.subscriber.model.TaxiRide;

public interface AggregatorService {
    void executeAggregationCycle();

    void aggregationConsistencyHandler(TaxiRide taxiRide);
}
