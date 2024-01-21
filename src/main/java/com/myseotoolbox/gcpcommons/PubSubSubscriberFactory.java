package com.myseotoolbox.gcpcommons;

import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PubSubSubscriberFactory {
    private final PubSubSubscriberTemplate template;
    private final String projectId;
    private final MessageConverter converter;

    public PubSubSubscriberFactory(PubSubSubscriberTemplate template, MessageConverter converter, GcpProjectIdProvider projectIdProvider) {
        this.template = template;
        this.converter = converter;
        this.projectId = projectIdProvider.getProjectId();

    }

    public PubSubSubscriber<PubsubMessage> buildRaw(String topicName, String subscriptionName) {
        return new PubSubSubscriber<>(template, m -> m, projectId, topicName, subscriptionName);
    }

    public <T> PubSubSubscriber<T> buildTyped(String topicName, String subscriptionName, Class<T> payloadType) {
        PubSubConverter<T> pubSubConverter = new TypedPubSubConverter<T>(payloadType, converter);
        return new PubSubSubscriber<T>(template, pubSubConverter, projectId, topicName, subscriptionName);
    }
}
