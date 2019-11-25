package com.myseotoolbox.gcpcommons;

import com.google.protobuf.Timestamp;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Data
public class PubsubMessage {
    private final LocalDateTime publishTime;

    private final String messageId;

    private final byte[] data;

    public PubsubMessage(com.google.pubsub.v1.PubsubMessage message) {
        this.messageId = message.getMessageId();
        this.publishTime = getDateTime(message.getPublishTime());
        this.data = message.getData().toByteArray();
    }

    private LocalDateTime getDateTime(Timestamp ts) {
        Instant instant = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    public String toString() {
        return "PubsubMessage{" +
                "publishTime=" + publishTime +
                ", messageId='" + messageId + '\'' +
                ", data=" + new String(data) +
                '}';
    }
}
