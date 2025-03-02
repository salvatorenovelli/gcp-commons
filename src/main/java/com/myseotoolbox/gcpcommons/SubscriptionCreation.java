package com.myseotoolbox.gcpcommons;

import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.pubsub.v1.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
public class SubscriptionCreation {

    private static final int ACK_DEADLINE = 600;
    private final GcpProjectIdProvider projectIdProvider;
    private final PubSubAdmin admin;
    private final String topicName;
    private final String subscriptionName;
    private final GcpCommonsPubSubProperties properties;


    public void createIfNotExist() {
        try {
            createSubscription().get(properties.getCommandTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Exception while connecting to PubSub. This can be caused by bad permissions in credentials. Try to set DEBUG log level to io.grpc & com.google.api.client");
            throw new RuntimeException("Unable to connect to PubSub with timeout of " + properties.getCommandTimeoutSeconds() + " seconds", e);
        }
    }

    private CompletableFuture<Void> createSubscription() {
        return CompletableFuture.runAsync(() -> {
            verifyConfig();
            log.info("Checking if subscription exists...");
            Subscription subscription = admin.getSubscription(subscriptionName);
            log.info("Subscription: {}", (subscription + "").replaceAll("\n", ""));
            if (subscription == null) {
                log.warn("Subscription did not exist. Recreating... '{}/{}' topic:'{}' ack deadline:{}", projectIdProvider.getProjectId(), subscriptionName, topicName, ACK_DEADLINE);
                admin.createSubscription(subscriptionName, topicName, ACK_DEADLINE);
            }
        });
    }

    private void verifyConfig() {
        Assert.noNullElements(new Object[]{projectIdProvider.getProjectId(), subscriptionName, topicName},
                "These properties should be set (projectId, subscriptionName, topicName): " + projectIdProvider.getProjectId() + ", " + subscriptionName + ", " + topicName);
    }
}