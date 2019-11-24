package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConversionException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
class PubSubConverter<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> payloadType;

    public T fromPubSubMessage(PubsubMessage message) {
        try {
            return objectMapper.readerFor(payloadType).readValue(message.getData().toByteArray());
        } catch (IOException ex) {
            throw new PubSubMessageConversionException("JSON deserialization of an object of type " + payloadType.getName() + " failed.", ex);
        }
    }
}