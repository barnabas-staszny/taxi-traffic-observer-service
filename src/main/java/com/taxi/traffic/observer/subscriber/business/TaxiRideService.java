package com.taxi.traffic.observer.subscriber.business;


import com.taxi.traffic.observer.subscriber.dto.TaxiRideDto;
import com.taxi.traffic.observer.subscriber.model.TaxiRide;

public interface TaxiRideService {
    TaxiRide storeTaxiRide(TaxiRideDto taxiRideDto);
}
