package com.myseotoolbox.gcpcommons;


public interface PubSubConverter<T> {
    T fromPubSubMessage(PubsubMessage message);
}
