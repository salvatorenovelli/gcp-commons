package com.myseotoolbox.gcpcommons;

import com.google.api.client.util.ExponentialBackOff;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.myseotoolbox.gcpcommons.LogUtils.shortToString;

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
                log.info("Processing of event {} was unsuccessful sending NACK", shortToString(event.toString()));
                msg.nack();
                executeBackoff();
            }
        });
        log.info("Subscribed!");
    }

    private boolean runConsumer(Function<T, Boolean> consumer, T event) {
        try {
            Boolean apply = consumer.apply(event);
            if (apply) log.debug("Processing of event completed: {}", shortToString(event.toString()));
            return apply;
        } catch (Exception e) {
            log.warn("Error while processing event: " + shortToString(event.toString()), e);
            return false;
        }
    }


    private String msgAsString(BasicAcknowledgeablePubsubMessage msg) {
        return shortToString(msg.toString().replaceAll("\n", "").replaceAll("\\\\\"", "\""));
    }

    @SneakyThrows
    private void executeBackoff() {
        long delay = backOff.nextBackOffMillis();
        log.warn("Backing off after error with delay of {}ms", delay);
        Thread.sleep(delay);
    }
}
