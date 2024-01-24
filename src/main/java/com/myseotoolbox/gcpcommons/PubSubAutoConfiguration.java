package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(GcpCommonsPubSubProperties.class)
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnClass(GcpCommonsPubSubProperties.class)
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
