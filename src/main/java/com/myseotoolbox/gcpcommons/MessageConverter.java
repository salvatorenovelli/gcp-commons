package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConversionException;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class MessageConverter {
    private final ObjectMapper objectMapper;

    public <T> T fromPubSubMessage(PubsubMessage message, Class<T> type) {
        try {
            return objectMapper.readerFor(type).readValue(message.getData());
        } catch (IOException ex) {
            throw new PubSubMessageConversionException("JSON deserialization of an object of type " + type.getName() + " failed.", ex);
        }
    }
}
