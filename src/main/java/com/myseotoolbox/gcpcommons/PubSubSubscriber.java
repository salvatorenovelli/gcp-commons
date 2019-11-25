package com.myseotoolbox.gcpcommons;

import com.google.api.client.util.ExponentialBackOff;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gcp.pubsub.core.subscriber.PubSubSubscriberTemplate;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class PubSubSubscriber<T> {
    private final PubSubSubscriberTemplate template;
    private final PubSubConverter<T> pubSubConverter;
    private final ExponentialBackOff backOff = new ExponentialBackOff();
    private final String projectId;
    private final String topicName;
    private final String subscriptionName;

    /**
     * Execute back off if exception is thrown
     */
    public void subscribe(Consumer<T> consumer) {
        subscribe(t -> {
            consumer.accept(t);
            return true;
        });
    }

    /**
     * Executes back off if exception is thrown or function return false
     */
    public void subscribe(Function<T, Boolean> function) {
        log.info("Subscribing to {}:{}/{}", projectId, topicName, subscriptionName);
        template.subscribe(subscriptionName, msg -> {
            T event = pubSubConverter.fromPubSubMessage(new PubsubMessage(msg.getPubsubMessage()));
            log.debug("Event received: {}", msgAsString(msg));
            if (runConsumer(function, event)) {
                msg.ack();
            } else {
                msg.nack();
                executeBackoff();
            }
        });
        log.info("Subscribed!");
    }

    private boolean runConsumer(Function<T, Boolean> consumer, T event) {
        try {
            Boolean apply = consumer.apply(event);
            if (apply) log.debug("Processing of event completed: {}", event);
            return apply;
        } catch (Exception e) {
            log.warn("Error while processing event: " + event, e);
            return false;
        }
    }

    private String msgAsString(BasicAcknowledgeablePubsubMessage msg) {
        return msg.toString().replaceAll("\n", "").replaceAll("\\\\\"", "\"");
    }

    @SneakyThrows
    private void executeBackoff() {
        long delay = backOff.nextBackOffMillis();
        log.warn("Backing off after error with delay of {}ms", delay);
        Thread.sleep(delay);
    }
}
