package com.taxi.traffic.observer.web;

import com.taxi.traffic.observer.aggregator.dao.jpa.AggregatedRideStatRepository;
import com.taxi.traffic.observer.aggregator.model.AggregatedRideStat;
import com.taxi.traffic.observer.subscriber.dao.TaxiRideRepository;
import com.taxi.traffic.observer.subscriber.model.TaxiRide;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
public class ObserverController {

    @Autowired
    TaxiRideRepository taxiRideRepository;

    @Autowired
    AggregatedRideStatRepository aggregatedRideStatRepository;


    @GetMapping("/taxirides")
    public ResponseEntity<List<TaxiRide>> listTaxiRides() {
        log.debug("GET request received on /taxirides path");
        List<TaxiRide> taxiRides = StreamSupport.stream(taxiRideRepository.findAll().spliterator(), false).collect(Collectors.toList());
        log.debug("Retrieving taxiRides: {}", taxiRides);
        return new ResponseEntity<List<TaxiRide>>(taxiRides, HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<AggregatedRideStat>> listAggregatedRideStats() {
        log.debug("GET request received on /taxirides path");
        List<AggregatedRideStat> aggregatedRideStats = StreamSupport.stream(aggregatedRideStatRepository.findAll().spliterator(), false).collect(Collectors.toList());
        log.debug("Retrieving aggregatedRideStat: {}", aggregatedRideStats);
        return new ResponseEntity<List<AggregatedRideStat>>(aggregatedRideStats, HttpStatus.OK);
    }

}
