package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.core.subscriber.PubSubSubscriberTemplate;


@Slf4j
public class PubSubSubscriberFactory {
    private final PubSubSubscriberTemplate template;
    private final ObjectMapper objectMapper;
    private final String projectId;

    public PubSubSubscriberFactory(PubSubSubscriberTemplate template, ObjectMapper objectMapper, GcpProjectIdProvider projectIdProvider) {
        this.template = template;
        this.objectMapper = objectMapper;
        this.projectId = projectIdProvider.getProjectId();
    }

    public <T> PubSubSubscriber<T> buildFor(String topicName, String subscriptionName, Class<T> payloadType) {
        PubSubConverter<T> pubSubConverter = new PubSubConverter<>(objectMapper, payloadType);
        return new PubSubSubscriber<T>(template, pubSubConverter, projectId, topicName, subscriptionName);
    }
}
