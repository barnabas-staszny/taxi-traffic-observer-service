package com.taxi.traffic.observer.aggregator.model;

import com.taxi.traffic.observer.common.model.AbstractAuditedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AggregatedRideStat extends AbstractAuditedEntity {

    @Id
    String hourOfAggregation;
    long pickupCounter;
    long dropoffCounter;
    long pickupPassengersCounter;
    long dropoffPassengersCounter;


}
