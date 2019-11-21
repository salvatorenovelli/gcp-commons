package com.myseotoolbox.gcpcommons;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@EnableConfigurationProperties(PubSubProperties.class)
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.gcp.pubsub.enabled", matchIfMissing = true)
@ConditionalOnClass(PubSubTemplate.class)
public class PubSubAutoConfiguration {

    @EventListener(ApplicationStartedEvent.class)
    public void appStarted() {
        System.out.println("SALVE");
    }


}
