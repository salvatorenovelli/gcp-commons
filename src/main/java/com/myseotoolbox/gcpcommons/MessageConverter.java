package com.myseotoolbox.gcpcommons;

import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConversionException;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class MessageConverter {
    private final ObjectMapper objectMapper;

    public <T> T fromPubSubMessage(PubsubMessage message, Class<T> type) {
        try {
            return objectMapper.readerFor(type).readValue(message.getData());
        } catch (JacksonException ex) {
            throw new PubSubMessageConversionException("JSON deserialization of an object of type " + type.getName() + " failed.", ex);
        }
    }
}
