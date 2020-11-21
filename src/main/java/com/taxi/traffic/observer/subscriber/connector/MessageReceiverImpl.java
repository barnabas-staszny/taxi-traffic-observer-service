package com.taxi.traffic.observer.subscriber.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.taxi.traffic.observer.aggregator.business.AggregatorService;
import com.taxi.traffic.observer.subscriber.business.TaxiRideService;
import com.taxi.traffic.observer.subscriber.dto.TaxiRideDto;
import com.taxi.traffic.observer.subscriber.model.RideStatus;
import com.taxi.traffic.observer.subscriber.model.TaxiRide;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MessageReceiverImpl implements MessageReceiver {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaxiRideService taxiRideService;

    @Autowired
    private AggregatorService aggregatorService;

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer ackReplyConsumer) {

        TaxiRideDto taxiRideDto = deserializePayload(message.getData());
        if (!isSkippable(taxiRideDto)) {
            TaxiRide taxiRide = taxiRideService.processTaxiRide(taxiRideDto);
            aggregatorService.aggregationConsistencyHandler(taxiRide);
        } else if (log.isTraceEnabled()) {
            log.trace("Skipping message: {} ", message);
        }
        ackReplyConsumer.ack();

    }


    private boolean isSkippable(TaxiRideDto taxiRideDto) {
        return !(RideStatus.DROPOFF.name().equalsIgnoreCase(taxiRideDto.getRideStatus()) ||
                RideStatus.PICKUP.name().equalsIgnoreCase(taxiRideDto.getRideStatus()));
    }

    /**
     * Deserialize JSON messages with a RuntimeException wrapper.
     */
    private TaxiRideDto deserializePayload(ByteString data) {

        try {
            return objectMapper.readValue(data.toStringUtf8(), TaxiRideDto.class);
        } catch (IOException e) {
            log.error("Json deserialization error: {}", e);
            throw new RuntimeException(e);
        }
    }
}
