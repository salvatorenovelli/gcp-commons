package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GcpCommonsPubSubProperties.class)
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnClass(PubSubTemplate.class)
public class PubSubAutoConfiguration {

    private final GcpCommonsPubSubProperties pubSubProperties;
    private final GcpProjectIdProvider projectIdProvider;
    private final PubSubAdmin pubSubAdmin;
    private final ObjectMapper objectMapper;

    @Bean
    public PubSubMessageConverter pubSubMessageConverter() {
        return new JacksonPubSubMessageConverter(objectMapper);
    }

    @Bean
    public PubSubConnectionVerification getPubSubConnectionVerification() {
        return new PubSubConnectionVerification(projectIdProvider.getProjectId(), pubSubAdmin);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void appStarted() {
        checkTopics();
        checkSubscriptions();
    }

    private void checkSubscriptions() {
        PubSubConnectionVerification pubSubConnectionVerification = getPubSubConnectionVerification();
        pubSubProperties.getSubscriptions().forEach(pubSubConnectionVerification::verifySubscription);
    }

    private void checkTopics() {
        PubSubConnectionVerification pubSubConnectionVerification = getPubSubConnectionVerification();
        List<String> topics = pubSubProperties.getTopics();
        if (topics.size() > 0) {
            topics.forEach(pubSubConnectionVerification::verifyTopic);
        } else {
            pubSubConnectionVerification.verifyConnection();
        }
    }


}
