package com.myseotoolbox.gcpcommons;

import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
class PubSubConnectionVerification {

    public static final int DEFAULT_TIMEOUT = 10;
    private static final int DEFAULT_ACK_DEADLINE_SECONDS = 600;
    public static final String FAKE_TOPIC_NAME = "nonexistent";
    private final String projectId;
    private final PubSubAdmin pubSubAdmin;
    private final GcpCommonsPubSubProperties properties;

    @EventListener(ApplicationStartedEvent.class)
    public void appStarted() {
        checkTopics();
        checkSubscriptions();
    }

    private void checkSubscriptions() {
        properties.getSubscriptions().forEach(this::verifySubscription);
    }

    private void checkTopics() {
        List<String> topics = properties.getTopics();
        if (topics.size() > 0) {
            topics.forEach(this::verifyTopic);
        } else {
            verifyConnection();
        }
    }

    private void verifyTopic(String topicName) {
        log.info("Verifying topic {}. ProjectId: {}", topicName, projectId);
        Topic topic = runWithTimeout(() -> pubSubAdmin.getTopic(topicName));
        Assert.notNull(topic, String.format("Topic '%s' not found in project '%s'", topicName, projectId));
        log.info("Successfully verified topic name '{}': {}", topicName, topic);
    }

    private void verifySubscription(String topicName, String subscriptionName) {
        runWithTimeout(() -> {
            log.info("Checking if subscription {} exists...", subscriptionName);
            Subscription subscription = pubSubAdmin.getSubscription(subscriptionName);
            log.info("Subscription: {}", (subscription + "").replaceAll("\n", ""));
            if (subscription == null) {
                log.warn("Subscription did not exist. Recreating... '{}/{}' topic:'{}' ack deadline:{}",
                        projectId, subscriptionName, topicName, DEFAULT_ACK_DEADLINE_SECONDS);
                pubSubAdmin.createSubscription(subscriptionName, topicName, DEFAULT_ACK_DEADLINE_SECONDS);
            }
            return null;
        });

    }

    void verifyConnection() {
        log.info("Checking connection to pub/sub using '{}' topic", FAKE_TOPIC_NAME);
        runWithTimeout(() -> pubSubAdmin.getTopic(FAKE_TOPIC_NAME));
    }

    private <T> T runWithTimeout(Supplier<T> supplier) {
        try {
            return CompletableFuture.supplyAsync(supplier).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Exception while executing pubsub command. This can be caused by bad permissions in credentials. Try to set DEBUG log level to io.grpc & com.google.api.client");
            throw new RuntimeException("Unable to execute PubSub command. (timeout " + DEFAULT_TIMEOUT + " seconds)", e);
        }
    }
}