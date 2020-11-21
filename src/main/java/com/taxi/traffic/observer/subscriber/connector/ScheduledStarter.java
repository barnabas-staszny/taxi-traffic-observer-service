package com.taxi.traffic.observer.subscriber.connector;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledStarter {

    private final SubscriberProperties properties;
    private final MessageReceiver messageReceiver;

    private Subscriber subscriber;

    /*
        This solution meant to reconnect on network failure.
     */
    @Scheduled(fixedDelay = 5000)
    public void scheduleSubscriber() {

        boolean isNewSubscriber = initiateSubscriberOnStop();

        if (isNewSubscriber) {
            try {
                subscriber.startAsync().awaitRunning();
                log.info("Subscriber started up: {} ", subscriber.getSubscriptionNameString());
            } catch (Exception e) {
                log.error("Subscriber error: {} ", e);
                subscriber.stopAsync();
            }
        }
    }

    private boolean initiateSubscriberOnStop() {
        boolean subscriberRecreated = false;
        if (subscriber == null || !subscriber.isRunning()) {
            initiateSubscriber();
            subscriberRecreated = true;
        }
        return subscriberRecreated;
    }

    private void initiateSubscriber() {
        CredentialsProvider credentialsProvider = null;
        try {
            credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(new File(properties.getCredentialsPath()))));
        } catch (IOException e) {
            log.error("Unable to create Subscriber: {}", e);
            throw new RuntimeException(e);
        }

        ExecutorProvider executorProvider =
                InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(properties.getExecutorThreadCount()).build();

        subscriber = Subscriber
                .newBuilder(ProjectSubscriptionName.of(properties.getProjectId(), properties.getSubscriptionId()), messageReceiver)
                .setParallelPullCount(properties.getParallelPullCount())
                .setExecutorProvider(executorProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
        log.info("Subscriber created for {}", subscriber.getSubscriptionNameString());
    }
}
