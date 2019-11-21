package com.myseotoolbox.gcpcommons;

import com.google.pubsub.v1.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.PubSubAdmin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
public class PubSubConnectionVerification {

    public static final int DEFAULT_TIMEOUT = 10;
    private final GcpProjectIdProvider projectIdProvider;
    private final PubSubAdmin pubSubAdmin;
    private final String topicName;

    public void verifyPubSubConnection() {
        try {
            log.info("Verifying pubsub connection. ProjectId: {}", projectIdProvider.getProjectId());
            Topic topic = CompletableFuture.supplyAsync(() -> pubSubAdmin.getTopic(topicName)).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            log.info("Successfully verified connection to pubsub. Topic: {}", topic.getName());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Exception while connecting to PubSub. This can be caused by bad permissions in credentials. Try to set DEBUG log level to io.grpc & com.google.api.client");
            throw new RuntimeException("Unable to connect to PubSub with timeout of " + DEFAULT_TIMEOUT + " seconds", e);
        }
    }
}