package com.myseotoolbox.gcpcommons;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "com.myseotoolbox.gcpcommons.pubsub")
public class GcpCommonsPubSubProperties {
    private final List<String> topics;
    private final Map<String,String> subscriptions;
}