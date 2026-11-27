package com.myseotoolbox.gcpcommons;

import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.pubsub.v1.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PubSubSubscriberTest {

    private static final String SUBSCRIPTION = "a-service.a-topic";
    private static final int SHORTEST_WAIT_MILLIS = 100;
    private static final int LONGEST_WAIT_MILLIS = 1_000;
    private static final int BACK_OFF_TIME_LIMIT_MILLIS = 10_000;
    private static final RuntimeException HANDLER_ERROR = new RuntimeException("the handler failed");

    @Mock PubSubSubscriberTemplate template;
    @Mock PubSubAdmin pubSubAdmin;
    @Mock Sleeper sleeper;
    @Mock BasicAcknowledgeablePubsubMessage message;
    @Mock Consumer<String> handler;

    private final AtomicLong nanosSinceTheSubscriberStarted = new AtomicLong();
    private PubSubSubscriber<String> sut;

    @BeforeEach
    void setUp() {
        when(pubSubAdmin.getSubscription(SUBSCRIPTION)).thenReturn(Subscription.getDefaultInstance());
        when(message.getPubsubMessage()).thenReturn(com.google.pubsub.v1.PubsubMessage.getDefaultInstance());
        ExponentialBackOff waitsDoublingFromShortestToLongest = new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(SHORTEST_WAIT_MILLIS)
                .setMultiplier(2)
                .setRandomizationFactor(0)
                .setMaxIntervalMillis(LONGEST_WAIT_MILLIS)
                .setMaxElapsedTimeMillis(BACK_OFF_TIME_LIMIT_MILLIS)
                .setNanoClock(nanosSinceTheSubscriberStarted::get)
                .build();
        sut = new PubSubSubscriber<>(new GcpCommonsPubSubProperties(), template, pubSubAdmin, pubSubMessage -> "an event",
                "a-project", "a-topic", SUBSCRIPTION, waitsDoublingFromShortestToLongest, sleeper);
    }

    @Test
    void shouldAcknowledgeAMessageThatTheHandlerAccepts() throws Exception {
        sut.subscribe(handler);

        deliver(message);

        verify(handler).accept("an event");
        verify(message).ack();
        verify(message, never()).nack();
        verify(sleeper, never()).sleep(anyLong());
    }

    @Test
    void shouldRefuseAMessageWhenTheHandlerThrows() throws Exception {
        doThrow(HANDLER_ERROR).when(handler).accept(any());
        sut.subscribe(handler);

        deliver(message);

        verify(message).nack();
        verify(message, never()).ack();
    }

    @Test
    void shouldRefuseAMessageWhenTheHandlerReturnsFalse() throws Exception {
        Function<String, Boolean> handlerThatReturnsFalse = event -> false;
        sut.subscribe(handlerThatReturnsFalse);

        deliver(message);

        verify(message).nack();
        verify(message, never()).ack();
    }

    @Test
    void shouldWaitAfterItRefusedAMessage() throws Exception {
        doThrow(HANDLER_ERROR).when(handler).accept(any());
        sut.subscribe(handler);

        deliver(message);

        verify(sleeper).sleep(SHORTEST_WAIT_MILLIS);
    }

    @Test
    void shouldWaitLongerAfterEachRefusalInARow() throws Exception {
        doThrow(HANDLER_ERROR).when(handler).accept(any());
        sut.subscribe(handler);

        deliver(message);
        deliver(message);
        deliver(message);

        InOrder waits = inOrder(sleeper);
        waits.verify(sleeper).sleep(100);
        waits.verify(sleeper).sleep(200);
        waits.verify(sleeper).sleep(400);
    }

    @Test
    void shouldWaitTheShortestTimeAgainAfterAMessageThatTheHandlerAccepts() throws Exception {
        doThrow(HANDLER_ERROR).doThrow(HANDLER_ERROR).doNothing().doThrow(HANDLER_ERROR).when(handler).accept(any());
        sut.subscribe(handler);

        deliver(message);
        deliver(message);
        deliver(message);
        deliver(message);

        InOrder waits = inOrder(sleeper);
        waits.verify(sleeper).sleep(100);
        waits.verify(sleeper).sleep(200);
        waits.verify(sleeper).sleep(100);
    }

    @Test
    void shouldWaitTheLongestTimeWhenTheBackOffHasPassedItsTimeLimit() throws Exception {
        doThrow(HANDLER_ERROR).when(handler).accept(any());
        sut.subscribe(handler);
        nanosSinceTheSubscriberStarted.set(TimeUnit.MILLISECONDS.toNanos(BACK_OFF_TIME_LIMIT_MILLIS + 1));

        deliver(message);

        verify(sleeper).sleep(LONGEST_WAIT_MILLIS);
    }

    private void deliver(BasicAcknowledgeablePubsubMessage message) {
        ArgumentCaptor<Consumer<BasicAcknowledgeablePubsubMessage>> subscribed = ArgumentCaptor.captor();
        verify(template).subscribe(eq(SUBSCRIPTION), subscribed.capture());
        subscribed.getValue().accept(message);
    }
}
