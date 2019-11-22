package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.core.subscriber.PubSubSubscriberTemplate;


@Slf4j
public class PubSubSubscriberFactory {
    private final PubSubSubscriberTemplate template;
    private final PubSubConverter pubSubConverter;
    private final String projectId;

    public PubSubSubscriberFactory(PubSubSubscriberTemplate template, ObjectMapper objectMapper, GcpProjectIdProvider projectIdProvider) {
        this.template = template;
        this.projectId = projectIdProvider.getProjectId();
        this.pubSubConverter = new PubSubConverter(objectMapper);
    }

    public <T> PubSubSubscriber<T> buildFor(String topicName, String subscriptionName, Class<T> payloadType) {
        return new PubSubSubscriber<T>(template, pubSubConverter, projectId, topicName, subscriptionName, payloadType);
    }
}
