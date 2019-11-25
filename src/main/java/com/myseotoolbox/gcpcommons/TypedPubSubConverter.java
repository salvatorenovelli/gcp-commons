package com.myseotoolbox.gcpcommons;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TypedPubSubConverter<T> implements PubSubConverter<T> {
    private final Class<T> payloadType;
    private final MessageConverter converter;

    public T fromPubSubMessage(PubsubMessage message) {
        return converter.fromPubSubMessage(message, payloadType);
    }
}