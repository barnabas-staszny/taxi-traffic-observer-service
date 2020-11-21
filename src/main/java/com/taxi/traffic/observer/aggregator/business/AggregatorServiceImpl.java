package com.taxi.traffic.observer.aggregator.business;

import com.taxi.traffic.observer.aggregator.dao.jdbc.AggregatorRepository;
import com.taxi.traffic.observer.aggregator.dao.jpa.AggregatedRideStatRepository;
import com.taxi.traffic.observer.aggregator.model.AggregatedRideStat;
import com.taxi.traffic.observer.subscriber.model.TaxiRide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
public class AggregatorServiceImpl implements AggregatorService {

    @Value(value = "${taxi-traffic-observer.aggregator.backwardAggregationCycles}")
    private int backwardAggregationCycles;

    @Autowired
    private AggregatorRepository aggregatorRepository;

    @Autowired
    private AggregatedRideStatRepository aggregatedRideStatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void executeAggregationCycle() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant aggregateFrom = now.minus(backwardAggregationCycles, ChronoUnit.HOURS);
        Instant aggregateTo = now.plus(1, ChronoUnit.HOURS);
        executeAggregation(aggregateFrom, aggregateTo);
    }

    /*
        Checks if the taxiRide refers to a timestamp which is no longer aggregated by the scheduler.
        In that case the saved aggregation is outdated so it will be updated.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    @Override
    public void aggregationConsistencyHandler(TaxiRide taxiRide) {
        Instant messageTime = taxiRide.getMessageTimestamp();
        boolean shouldReAggregate = isOutOfScheduledAggregationPeriod(messageTime);

        if (shouldReAggregate) {
            Instant aggregateFrom = messageTime.truncatedTo(ChronoUnit.HOURS);
            Instant aggregateTo = aggregateFrom.plus(1, ChronoUnit.HOURS);
            executeAggregation(aggregateFrom, aggregateTo);
        }

    }

    private boolean isOutOfScheduledAggregationPeriod(Instant messageTime) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant scheduledAggregateFrom = now.minus(backwardAggregationCycles - 1, ChronoUnit.HOURS);
        return messageTime.compareTo(scheduledAggregateFrom) < 0;
    }

    private void executeAggregation(Instant aggregateFrom, Instant aggregateTo) {
        LocalDate fromLocalDate = aggregateFrom.atZone(ZoneOffset.UTC).toLocalDate();
        int fromHour = aggregateFrom.atZone(ZoneOffset.UTC).getHour();
        LocalDate toLocalDate = aggregateTo.atZone(ZoneOffset.UTC).toLocalDate();
        int toHour = aggregateTo.atZone(ZoneOffset.UTC).getHour();

        List<AggregatedRideStat> aggregatedRideStats = aggregatorRepository.query(fromLocalDate, fromHour, toLocalDate, toHour);
        aggregatedRideStats.stream().forEach(
                aggregatedRideStat -> upsertAggregatedRideStat(aggregatedRideStat)
        );
    }

    private void upsertAggregatedRideStat(AggregatedRideStat aggregatedRideStat) {
        Optional<AggregatedRideStat> oAggregatedRideStat = aggregatedRideStatRepository.findById(aggregatedRideStat.getHourOfAggregation());
        if (oAggregatedRideStat.isPresent()) {
            aggregatedRideStat.setCreatedTime(oAggregatedRideStat.get().getCreatedTime());
        }
        aggregatedRideStatRepository.save(aggregatedRideStat);
    }

}
