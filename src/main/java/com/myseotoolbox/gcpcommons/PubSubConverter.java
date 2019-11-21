package com.myseotoolbox.gcpcommons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConversionException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PubSubConverter {
    private final ObjectMapper objectMapper;

    public <T> T fromPubSubMessage(PubsubMessage message, Class<T> payloadType) {
        try {
            return objectMapper.readerFor(payloadType).readValue(message.getData().toByteArray());
        } catch (IOException ex) {
            throw new PubSubMessageConversionException("JSON deserialization of an object of type " + payloadType.getName() + " failed.", ex);
        }
    }
}