package com.myseotoolbox.gcpcommons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "com.myseotoolbox.gcpcommons.pubsub")
public class GcpCommonsPubSubProperties {
    private Integer commandTimeoutSeconds = 30;
    private boolean createTopicIfMissing = true;
    private List<String> topics = Collections.emptyList();
    private boolean createSubscriptionIfMissing = true;
    private Map<String, String> subscriptions = Collections.emptyMap();

    public boolean createTopicIfMissing() {
        return createTopicIfMissing;
    }

    public boolean createSubscriptionIfMissing() {
        return createSubscriptionIfMissing;
    }
}