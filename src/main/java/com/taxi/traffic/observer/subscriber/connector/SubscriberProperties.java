package com.taxi.traffic.observer.subscriber.connector;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "taxi-traffic-observer.subscriber")
public class SubscriberProperties {

    String credentialsPath;
    String projectId;
    String subscriptionId;
    int executorThreadCount;
    int parallelPullCount;
}
