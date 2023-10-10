package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PreDestroy;
import java.io.Serializable;

@Slf4j
@Component
public class KafkaProducerImpl<KEY extends Serializable, VALUE extends SpecificRecordBase> implements KafkaProducer<KEY, VALUE> {
    private final KafkaTemplate<KEY, VALUE> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<KEY, VALUE> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, KEY key, VALUE message, ListenableFutureCallback<SendResult<KEY, VALUE>> callback) {
        log.info("Sending message={} to topic={}", message, topicName);
        try {
            ListenableFuture<SendResult<KEY, VALUE>> kafkaResultFuture =  kafkaTemplate.send(topicName, key, message);
            kafkaResultFuture.addCallback(callback);
        } catch(KafkaException e) {
            log.error("Error on kafka producer with key: {}, message: {}, and exception: {}", key, message, e.getMessage());
            throw new KafkaProducerException("Error on kafka producer");
        }
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer");
            kafkaTemplate.destroy();
        }
    }
}
