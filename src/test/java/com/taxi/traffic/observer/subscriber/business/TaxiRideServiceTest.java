package com.taxi.traffic.observer.subscriber.business;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.traffic.observer.subscriber.dao.TaxiRideRepository;
import com.taxi.traffic.observer.subscriber.dto.TaxiRideDto;
import com.taxi.traffic.observer.subscriber.model.RideStatus;
import com.taxi.traffic.observer.subscriber.model.TaxiRide;
import com.taxi.traffic.observer.subscriber.model.TaxiRideId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TaxiRideServiceImpl.class})
@ActiveProfiles("test")
public class TaxiRideServiceTest {

    @Autowired
    private TaxiRideService taxiRideService;

    @MockBean
    private TaxiRideRepository taxiRideRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void testTaxiRideServiceResultEqualsToInputDto() throws Exception {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2019-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"pickup\",\"passenger_count\":1}";

        TaxiRideDto taxiRideDto = OBJECT_MAPPER.readValue(testData, TaxiRideDto.class);

        TaxiRide taxiRide = taxiRideService.processTaxiRide(taxiRideDto);

        Assert.assertNotNull(taxiRide);
        Assert.assertEquals(taxiRideDto.getPassengerCount(), taxiRide.getPassengerCount());
        Assert.assertEquals(RideStatus.PICKUP, taxiRide.getTaxiRideId().getRideStatus());
        Assert.assertEquals("2bbbeae2-cd84-4177-9479-3a8df1ee6d58", taxiRide.getTaxiRideId().getRideId().toString());
        Instant expectedTimestamp = taxiRideDto.getTimestamp().toInstant();
        Instant savedTimestamp = taxiRide.getMessageTimestamp();
        Assert.assertEquals(expectedTimestamp.atZone(ZoneId.systemDefault()).getYear(), savedTimestamp.atZone(ZoneId.systemDefault()).getYear());
        Assert.assertEquals(expectedTimestamp.atZone(ZoneId.systemDefault()).getMonthValue(), savedTimestamp.atZone(ZoneId.systemDefault()).getMonthValue());
        Assert.assertEquals(expectedTimestamp.atZone(ZoneId.systemDefault()).getDayOfMonth(), savedTimestamp.atZone(ZoneId.systemDefault()).getDayOfMonth());
        Assert.assertEquals(expectedTimestamp.atZone(ZoneId.systemDefault()).getHour(), savedTimestamp.atZone(ZoneId.systemDefault()).getHour());

    }


    @Test
    public void testTaxiRideSave() throws Exception {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2019-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"pickup\",\"passenger_count\":1}";

        TaxiRideDto taxiRideDto = OBJECT_MAPPER.readValue(testData, TaxiRideDto.class);

        Mockito.when(taxiRideRepository.findById(any(TaxiRideId.class))).thenReturn(Optional.empty());

        TaxiRide taxiRide = taxiRideService.processTaxiRide(taxiRideDto);

        Assert.assertNotNull(taxiRide);
        Assert.assertNotNull(taxiRide.getTaxiRideId());
        Assert.assertNotNull(taxiRide.getTaxiRideId().getRideId());
        Assert.assertNotNull(taxiRide.getTaxiRideId().getRideStatus());
        Assert.assertNotNull(taxiRide.getPassengerCount());
        Assert.assertNotNull(taxiRide.getMessageTimestamp());

        Mockito.verify(taxiRideRepository, Mockito.times(1)).findById(any(TaxiRideId.class));
        Mockito.verify(taxiRideRepository, Mockito.times(1)).save(any(TaxiRide.class));
    }

    @Test
    public void testTaxiRideUpdate() throws Exception {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2019-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"pickup\",\"passenger_count\":1}";

        TaxiRideDto taxiRideDto = OBJECT_MAPPER.readValue(testData, TaxiRideDto.class);
        Instant oldCreatedTime = Instant.now().minus(5, ChronoUnit.HOURS);
        TaxiRide oldTaxiRide = TaxiRide.builder()
                .taxiRideId(TaxiRideId.builder().rideId(UUID.fromString("2bbbeae2-cd84-4177-9479-3a8df1ee6d58")).rideStatus(RideStatus.PICKUP).build())
                .messageTimestamp(taxiRideDto.getTimestamp().toInstant().minus(10, ChronoUnit.HOURS))
                .passengerCount(taxiRideDto.getPassengerCount() + 10)
                .build();
        oldTaxiRide.setCreatedTime(oldCreatedTime);

        Mockito.when(taxiRideRepository.findById(any(TaxiRideId.class))).thenReturn(Optional.of(oldTaxiRide));


        TaxiRide taxiRide = taxiRideService.processTaxiRide(taxiRideDto);

        Assert.assertNotNull(taxiRide);
        Assert.assertNotNull(taxiRide.getTaxiRideId());
        Assert.assertEquals(taxiRide.getTaxiRideId().getRideId(), UUID.fromString("2bbbeae2-cd84-4177-9479-3a8df1ee6d58"));
        Assert.assertEquals(taxiRide.getTaxiRideId().getRideStatus(), RideStatus.PICKUP);
        Assert.assertEquals(taxiRide.getPassengerCount(), 1);
        Assert.assertEquals(taxiRide.getMessageTimestamp(), taxiRideDto.getTimestamp().toInstant());
        Assert.assertEquals(taxiRide.getCreatedTime(), oldCreatedTime);

        Mockito.verify(taxiRideRepository, Mockito.times(1)).findById(any(TaxiRideId.class));
        Mockito.verify(taxiRideRepository, Mockito.times(1)).save(any(TaxiRide.class));
    }


}
