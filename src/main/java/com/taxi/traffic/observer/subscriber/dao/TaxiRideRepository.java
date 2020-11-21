package com.taxi.traffic.observer.subscriber.dao;


import com.taxi.traffic.observer.subscriber.model.TaxiRide;
import com.taxi.traffic.observer.subscriber.model.TaxiRideId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxiRideRepository extends CrudRepository<TaxiRide, TaxiRideId> {

}
