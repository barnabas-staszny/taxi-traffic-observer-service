package com.taxi.traffic.observer.aggregator.business;


import com.google.common.collect.ImmutableList;
import com.taxi.traffic.observer.aggregator.dao.jdbc.AggregatorRepository;
import com.taxi.traffic.observer.aggregator.dao.jpa.AggregatedRideStatRepository;
import com.taxi.traffic.observer.aggregator.model.AggregatedRideStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AggregatorServiceImpl.class})
@ActiveProfiles("test")
public class AggregatorServiceTest {

    @Autowired
    AggregatorServiceImpl aggregatorService;

    @MockBean
    private AggregatorRepository aggregatorRepository;

    @MockBean
    private AggregatedRideStatRepository aggregatedRideStatRepository;

    @Test
    public void testAggregationSavesFoundAggregatedRideStats() throws Exception {

        Mockito.when(aggregatorRepository.query(any(LocalDate.class), anyInt(), any(LocalDate.class), anyInt()))
                .thenReturn(ImmutableList.of(getAggregatedRideStat("2020-11-21 04"), getAggregatedRideStat("2020-11-21 04")));

        aggregatorService.executeAggregationCycle();

        Mockito.verify(aggregatedRideStatRepository, Mockito.times(2)).findById(anyString());
        Mockito.verify(aggregatedRideStatRepository, Mockito.times(2)).save(any(AggregatedRideStat.class));

    }

    @Test
    public void testAggregationUsesTheOldCreatedTime() throws Exception {

        String hourOfAggregation1 = "2020-11-21 04";
        String hourOfAggregation2 = "2020-11-21 05";
        AggregatedRideStat aggregatedRideStat1New = Mockito.spy(getAggregatedRideStat(hourOfAggregation1));
        AggregatedRideStat aggregatedRideStat1Old = getAggregatedRideStat(hourOfAggregation1);
        Instant oldCreatedDate = Instant.now().minus(5, ChronoUnit.HOURS);
        aggregatedRideStat1Old.setCreatedTime(oldCreatedDate);
        AggregatedRideStat aggregatedRideStat2 = Mockito.spy(getAggregatedRideStat(hourOfAggregation2));
        Mockito.when(aggregatorRepository.query(any(LocalDate.class), anyInt(), any(LocalDate.class), anyInt())).thenReturn(ImmutableList.of(aggregatedRideStat1New, aggregatedRideStat2));
        Mockito.when(aggregatedRideStatRepository.findById(hourOfAggregation1)).thenReturn(Optional.of(aggregatedRideStat1Old));

        aggregatorService.executeAggregationCycle();

        Mockito.verify(aggregatedRideStatRepository, Mockito.times(2)).findById(anyString());
        Mockito.verify(aggregatedRideStatRepository, Mockito.times(2)).save(any(AggregatedRideStat.class));
        Mockito.verify(aggregatedRideStat1New, Mockito.times(1)).setCreatedTime(any(Instant.class));
        Mockito.verify(aggregatedRideStat2, Mockito.times(0)).setCreatedTime(any(Instant.class));

    }

    private AggregatedRideStat getAggregatedRideStat(String hourOfAggregation) {

        return new AggregatedRideStat(hourOfAggregation, 1, 2, 3, 4);
    }


}
