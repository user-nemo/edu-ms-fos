package com.food.ordering.system.kafka.producer.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.Serializable;

public interface KafkaProducer<KEY extends Serializable, VALUE extends SpecificRecordBase> {
    void send(String topicName, KEY key, VALUE message, ListenableFutureCallback<SendResult<KEY, VALUE>> callback);
}
