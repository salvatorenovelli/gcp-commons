package com.myseotoolbox.gcpcommons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "com.myseotoolbox.gcpcommons.pubsub")
public class GcpCommonsPubSubProperties {
    private boolean createTopicIfMissing = true;
    private final List<String> topics;
    private boolean createSubscriptionIfMissing = true;
    private final Map<String, String> subscriptions;

    public boolean createTopicIfMissing() {
        return createTopicIfMissing;
    }

    public boolean createSubscriptionIfMissing() {
        return createSubscriptionIfMissing;
    }

    public Map<String, String> getSubscriptions() {
        return subscriptions;
    }
}