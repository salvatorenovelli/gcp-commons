package com.myseotoolbox.gcpcommons;

import com.google.api.client.util.ExponentialBackOff;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.subscriber.PubSubSubscriberTemplate;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class PubSubSubscriber<T> {
    private final PubSubSubscriberTemplate template;
    private final PubSubConverter pubSubConverter;
    private final ExponentialBackOff backOff = new ExponentialBackOff();
    private final String projectId;
    private final String topicName;
    private final String subscriptionName;
    private final Class<T> payloadType;


    public void subscribe(Consumer<T> consumer) {
        log.info("Subscribing to {}:{}/{}", projectId, topicName, subscriptionName);

        template.subscribe(subscriptionName, msg -> {
            String msgAsString = msg.toString().replaceAll("\n", "").replaceAll("\\\\\"", "\"");
            log.debug("Event received: {}", msgAsString);
            try {
                T event = pubSubConverter.fromPubSubMessage(msg.getPubsubMessage(), payloadType);
                consumer.accept(event);
                msg.ack();
                log.debug("Processing of event completed: {}", event);
            } catch (Exception e) {
                log.warn("Error while processing event: " + msg.getPubsubMessage().getMessageId() + ": " + msgAsString, e);
                msg.nack();
                executeBackoff();
            }
        });

        log.info("Subscribed!");
    }

    @SneakyThrows
    private void executeBackoff() {
        long delay = backOff.nextBackOffMillis();
        log.warn("Backing off after error with delay of {}ms", delay);
        Thread.sleep(delay);
    }
}
