package com.taxi.traffic.observer.subscriber.business;


import com.taxi.traffic.observer.subscriber.dao.TaxiRideRepository;
import com.taxi.traffic.observer.subscriber.dto.TaxiRideDto;
import com.taxi.traffic.observer.subscriber.model.RideStatus;
import com.taxi.traffic.observer.subscriber.model.TaxiRide;
import com.taxi.traffic.observer.subscriber.model.TaxiRideId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class TaxiRideServiceImpl implements TaxiRideService {

    @Autowired
    private TaxiRideRepository taxiRideRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public TaxiRide processTaxiRide(TaxiRideDto taxiRideDto) {

        Optional<TaxiRide> oTaxiRide = taxiRideRepository.findById(getTaxiRideId(taxiRideDto));
        TaxiRide taxiRide = oTaxiRide.map(oldTaxiRide -> updateTaxiRideModel(taxiRideDto, oldTaxiRide))
                .orElseGet(() -> mapToModel(taxiRideDto));

        taxiRideRepository.save(taxiRide);
        log.debug("Saving taxi ride: {} ", taxiRide);
        return taxiRide;
    }

    private TaxiRide mapToModel(TaxiRideDto taxiRideDto) {
        TaxiRide taxiRide = TaxiRide.builder()
                .taxiRideId(getTaxiRideId(taxiRideDto))
                .build();
        return updateTaxiRideModel(taxiRideDto, taxiRide);
    }

    private TaxiRide updateTaxiRideModel(TaxiRideDto taxiRideDto, TaxiRide taxiRide) {
        taxiRide.setPassengerCount(taxiRideDto.getPassengerCount());
        taxiRide.setMessageTimestamp(taxiRideDto.getTimestamp().toInstant());
        return taxiRide;
    }

    private TaxiRideId getTaxiRideId(TaxiRideDto taxiRideDto) {
        TaxiRideId taxiRideId;
        taxiRideId = TaxiRideId.builder()
                .rideId(UUID.fromString(taxiRideDto.getRideId()))
                .rideStatus(RideStatus.fromString(taxiRideDto.getRideStatus()))
                .build();
        return taxiRideId;
    }
}
