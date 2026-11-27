package com.myseotoolbox.gcpcommons;

import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.pubsub.v1.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.myseotoolbox.gcpcommons.LogUtils.shortToString;

@Slf4j
@RequiredArgsConstructor
public class PubSubSubscriber<T> {
    private final GcpCommonsPubSubProperties properties;
    private final PubSubSubscriberTemplate template;
    private final PubSubAdmin pubSubAdmin;
    private final PubSubConverter<T> pubSubConverter;
    private final String projectId;
    private final String topicName;
    private final String subscriptionName;
    private final ExponentialBackOff backOff;
    private final Sleeper sleeper;

    /**
     * Execute back off if exception is thrown
     */
    public void subscribe(Consumer<T> consumer) throws SubscriptionNonExistentException {
        subscribe(t -> {
            consumer.accept(t);
            return true;
        });
    }

    /**
     * Executes back off if exception is thrown or function return false
     */
    public void subscribe(Function<T, Boolean> function) throws SubscriptionNonExistentException {

        verifySubscription();

        log.info("Subscribing to {}:{}/{}", projectId, topicName, subscriptionName);
        template.subscribe(subscriptionName, msg -> {
            T event = pubSubConverter.fromPubSubMessage(new PubsubMessage(msg.getPubsubMessage()));
            log.debug("Event received: {}", msgAsString(msg));
            if (runConsumer(function, event)) {
                msg.ack();
                restartBackoff();
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

    private void verifySubscription() throws SubscriptionNonExistentException {
        Subscription subscription = runWithTimeout(() -> pubSubAdmin.getSubscription(subscriptionName));
        if (subscription == null) {
            log.error("Subscription does not exist '{}'", subscriptionName);
            throw new SubscriptionNonExistentException(subscriptionName);
        }
    }

    private String msgAsString(BasicAcknowledgeablePubsubMessage msg) {
        return shortToString(msg.toString().replaceAll("\n", "").replaceAll("\\\\\"", "\""));
    }

    @SneakyThrows
    private void executeBackoff() {
        long delay = nextBackoffMillis();
        log.warn("Backing off after error with delay of {}ms", delay);
        sleeper.sleep(delay);
    }

    /**
     * Synchronized because ExponentialBackOff is not thread-safe and Pub/Sub delivers on several threads.
     */
    private synchronized void restartBackoff() {
        backOff.reset();
    }

    private synchronized long nextBackoffMillis() throws IOException {
        long delay = backOff.nextBackOffMillis();
        boolean backOffHasPassedItsTimeLimit = delay == BackOff.STOP;
        if (backOffHasPassedItsTimeLimit) return backOff.getMaxIntervalMillis();
        return delay;
    }

    private <T> T runWithTimeout(Supplier<T> supplier) {
        try {
            return CompletableFuture.supplyAsync(supplier).get(properties.getCommandTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Exception while executing pubsub command. This can be caused by bad permissions in credentials. " +
                    "Try to set DEBUG log level to io.grpc & com.google.api.client");
            throw new RuntimeException("Unable to execute PubSub command. (timeout " + properties.getCommandTimeoutSeconds() + " seconds)", e);
        }
    }
}
