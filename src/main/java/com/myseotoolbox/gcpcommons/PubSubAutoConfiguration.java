package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gcp.autoconfigure.core.GcpContextAutoConfiguration;
import org.springframework.cloud.gcp.core.GcpProjectIdProvider;
import org.springframework.cloud.gcp.pubsub.PubSubAdmin;
import org.springframework.cloud.gcp.pubsub.core.subscriber.PubSubSubscriberTemplate;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GcpCommonsPubSubProperties.class)
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnClass(GcpCommonsPubSubProperties.class)
@Profile("!test")
public class PubSubAutoConfiguration {
    private final GcpCommonsPubSubProperties properties;

    @Bean
    public MessageConverter getMessageConverter(ObjectMapper mapper) {
        return new MessageConverter(mapper);
    }

    @Bean
    public PubSubMessageConverter pubSubMessageConverter(ObjectMapper objectMapper) {
        return new JacksonPubSubMessageConverter(objectMapper);
    }

    @Bean
    public PubSubConnectionVerification getPubSubConnectionVerification(GcpProjectIdProvider projectIdProvider, PubSubAdmin pubSubAdmin) {
        return new PubSubConnectionVerification(projectIdProvider.getProjectId(), pubSubAdmin, properties);
    }

    @Bean
    public PubSubSubscriberFactory getPubSubSubscriberFactory(PubSubSubscriberTemplate template, MessageConverter converter, GcpProjectIdProvider provider) {
        return new PubSubSubscriberFactory(template, converter, provider);
    }


}
