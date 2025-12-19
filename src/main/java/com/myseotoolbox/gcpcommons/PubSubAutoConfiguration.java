package com.myseotoolbox.gcpcommons;

import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.PubSubAdmin;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConversionException;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(GcpCommonsPubSubProperties.class)
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnClass(GcpCommonsPubSubProperties.class)
public class PubSubAutoConfiguration {

    @Bean
    public MessageConverter getMessageConverter(ObjectMapper mapper) {
        return new MessageConverter(mapper);
    }

    @Bean
    public PubSubMessageConverter pubSubMessageConverter(ObjectMapper objectMapper) {
        return new NewJacksonDatabinder(objectMapper);
    }

    @Bean
    public PubSubConnectionVerification getPubSubConnectionVerification(GcpCommonsPubSubProperties properties, GcpProjectIdProvider projectIdProvider, PubSubAdmin pubSubAdmin) {
        return new PubSubConnectionVerification(projectIdProvider.getProjectId(), pubSubAdmin, properties);
    }

    @Bean
    public PubSubSubscriberFactory getPubSubSubscriberFactory(GcpCommonsPubSubProperties properties, PubSubSubscriberTemplate template, PubSubAdmin pubSubAdmin, MessageConverter converter, GcpProjectIdProvider provider) {
        return new PubSubSubscriberFactory(properties, template, pubSubAdmin, converter, provider);
    }


}

class NewJacksonDatabinder implements PubSubMessageConverter {

    private final ObjectMapper objectMapper;

    public NewJacksonDatabinder(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "A valid ObjectMapper is required.");
        this.objectMapper = objectMapper;
    }

    @Override
    public com.google.pubsub.v1.PubsubMessage toPubSubMessage(Object payload, Map<String, String> headers) {
        try {
            return byteStringToPubSubMessage(
                    ByteString.copyFrom(this.objectMapper.writeValueAsBytes(payload)), headers);
        } catch (JacksonException ex) {
            throw new PubSubMessageConversionException(
                    "JSON serialization of an object of type " + payload.getClass().getName() + " failed.",
                    ex);
        }
    }

    @Override
    public <T> T fromPubSubMessage(PubsubMessage message, Class<T> payloadType) {
        try {
            return this.objectMapper.readerFor(payloadType).readValue(message.getData().toByteArray());
        } catch (JacksonException ex) {
            throw new PubSubMessageConversionException(
                    "JSON deserialization of an object of type " + payloadType.getName() + " failed.", ex);
        }
    }
}