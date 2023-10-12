package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class OrderKafkaMessageHelper {
    public <MODEL> ListenableFutureCallback<SendResult<String, MODEL>> getKafkaCallback(
        String responseTopicName,
        MODEL requestAvroModel,
        String orderId
    ) {
        return new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error(
                    "Error while sending RequestAvroModel message {} to topic {}",
                    requestAvroModel.toString(),
                    responseTopicName,
                    ex
                );
            }

            @Override
            public void onSuccess(SendResult<String, MODEL> result) {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info(
                    "Received successful response from kafka for order if: {}, topic: {}, partition: {}, offset: {}, timestamp: {}",
                    orderId,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp()
                );
            }
        };
    }

}
