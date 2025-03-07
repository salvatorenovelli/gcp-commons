package com.myseotoolbox.gcpcommons;

import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PubSubSubscriberFactory {
    private final GcpCommonsPubSubProperties properties;
    private final PubSubSubscriberTemplate template;
    private final String projectId;
    private final MessageConverter converter;
    private final PubSubAdmin pubSubAdmin;

    public PubSubSubscriberFactory(GcpCommonsPubSubProperties properties,
                                   PubSubSubscriberTemplate template,
                                   PubSubAdmin pubSubAdmin,
                                   MessageConverter converter,
                                   GcpProjectIdProvider projectIdProvider) {
        this.properties = properties;
        this.template = template;
        this.pubSubAdmin = pubSubAdmin;
        this.converter = converter;
        this.projectId = projectIdProvider.getProjectId();
    }

    public PubSubSubscriber<PubsubMessage> buildRaw(String topicName, String subscriptionName) {
        return new PubSubSubscriber<>(properties, template, pubSubAdmin, m -> m, projectId, topicName, subscriptionName);
    }

    public <T> PubSubSubscriber<T> buildTyped(String topicName, String subscriptionName, Class<T> payloadType) {
        PubSubConverter<T> pubSubConverter = new TypedPubSubConverter<T>(payloadType, converter);
        return new PubSubSubscriber<T>(properties, template, pubSubAdmin, pubSubConverter, projectId, topicName, subscriptionName);
    }
}
