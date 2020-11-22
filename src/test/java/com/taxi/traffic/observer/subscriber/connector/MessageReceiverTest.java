package com.taxi.traffic.observer.subscriber.connector;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.taxi.traffic.observer.aggregator.business.AggregatorService;
import com.taxi.traffic.observer.subscriber.business.TaxiRideService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MessageReceiverImpl.class, ObjectMapper.class})
@SpyBean(ObjectMapper.class)
@ActiveProfiles("test")
public class MessageReceiverTest {

    @Autowired
    MessageReceiver messageReceiver;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaxiRideService taxiRideService;

    @MockBean
    private AggregatorService aggregatorService;


    @Test
    public void testOnParseExceptionItShouldNotAckTheMessage() throws JsonProcessingException {

        AckReplyConsumer mockAckReplyConsumer = mock(AckReplyConsumer.class);
        PubsubMessage mockPubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom("Cannot parsing this message", Charset.defaultCharset())).build();

        Exception ex = getException(() -> messageReceiver.receiveMessage(mockPubsubMessage, mockAckReplyConsumer));
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getCause() instanceof JsonParseException);
        Mockito.verify(taxiRideService, Mockito.times(0)).storeTaxiRide(any());
        Mockito.verify(aggregatorService, Mockito.times(0)).aggregationConsistencyHandler(any());
        Mockito.verify(mockAckReplyConsumer, Mockito.times(0)).ack();

    }

    @Test
    public void testOnSuccessItShouldAckTheMessage() throws JsonProcessingException {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2019-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"enroute\",\"passenger_count\":1}";

        AckReplyConsumer mockAckReplyConsumer = mock(AckReplyConsumer.class);
        PubsubMessage mockPubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(testData, Charset.defaultCharset())).build();

        Exception ex = getException(() -> messageReceiver.receiveMessage(mockPubsubMessage, mockAckReplyConsumer));
        Assert.assertNull(ex);
        Mockito.verify(mockAckReplyConsumer, Mockito.times(1)).ack();
        Mockito.verify(taxiRideService, Mockito.times(0)).storeTaxiRide(any());
        Mockito.verify(aggregatorService, Mockito.times(0)).aggregationConsistencyHandler(any());

    }

    @Test
    public void testOnSuccessItShouldAckAndSkipEnrouteMessage() throws JsonProcessingException {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2019-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"enroute\",\"passenger_count\":1}";

        AckReplyConsumer mockAckReplyConsumer = mock(AckReplyConsumer.class);
        PubsubMessage mockPubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(testData, Charset.defaultCharset())).build();

        Exception ex = getException(() -> messageReceiver.receiveMessage(mockPubsubMessage, mockAckReplyConsumer));
        Assert.assertNull(ex);
        Mockito.verify(mockAckReplyConsumer, Mockito.times(1)).ack();
        Mockito.verify(taxiRideService, Mockito.times(0)).storeTaxiRide(any());
        Mockito.verify(aggregatorService, Mockito.times(0)).aggregationConsistencyHandler(any());

    }

    @Test
    public void testOnSuccessItShouldAckAndHandleDropOffMessage() throws JsonProcessingException {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2010-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"dropoff\",\"passenger_count\":1}";

        AckReplyConsumer mockAckReplyConsumer = mock(AckReplyConsumer.class);
        PubsubMessage mockPubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(testData, Charset.defaultCharset())).build();

        Exception ex = getException(() -> messageReceiver.receiveMessage(mockPubsubMessage, mockAckReplyConsumer));
        Assert.assertNull(ex);
        Mockito.verify(mockAckReplyConsumer, Mockito.times(1)).ack();
        Mockito.verify(taxiRideService, Mockito.times(1)).storeTaxiRide(any());
        Mockito.verify(aggregatorService, Mockito.times(1)).aggregationConsistencyHandler(any());

    }

    @Test
    public void testOnSuccessItShouldAckAndHandlePickUpMessage() throws JsonProcessingException {

        String testData = "{\"ride_id\":\"2bbbeae2-cd84-4177-9479-3a8df1ee6d58\",\"point_idx\":2060,\"latitude\":40.72430000000001," +
                "\"longitude\":-74.13295000000001,\"timestamp\":\"2010-10-17T17:07:42.72289-04:00\",\"meter_reading\":84.16801," +
                "\"meter_increment\":0.040858258,\"ride_status\":\"pickup\",\"passenger_count\":1}";

        AckReplyConsumer mockAckReplyConsumer = mock(AckReplyConsumer.class);
        PubsubMessage mockPubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(testData, Charset.defaultCharset())).build();

        Exception ex = getException(() -> messageReceiver.receiveMessage(mockPubsubMessage, mockAckReplyConsumer));
        Assert.assertNull(ex);
        Mockito.verify(mockAckReplyConsumer, Mockito.times(1)).ack();
        Mockito.verify(taxiRideService, Mockito.times(1)).storeTaxiRide(any());
        Mockito.verify(aggregatorService, Mockito.times(1)).aggregationConsistencyHandler(any());

    }

    private Exception getException(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            return e;
        }
        return null;
    }
}
