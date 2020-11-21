package com.taxi.traffic.observer.subscriber.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class TaxiRideId implements Serializable {

    UUID rideId;
    @Enumerated(EnumType.STRING)
    RideStatus rideStatus;
}
