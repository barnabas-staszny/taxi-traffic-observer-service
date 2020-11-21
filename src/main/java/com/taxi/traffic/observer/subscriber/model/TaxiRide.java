package com.taxi.traffic.observer.subscriber.model;


import com.taxi.traffic.observer.common.model.AbstractAuditedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.time.Instant;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TaxiRide extends AbstractAuditedEntity {

    @EmbeddedId
    TaxiRideId taxiRideId;
    Instant messageTimestamp;
    int passengerCount;

}
