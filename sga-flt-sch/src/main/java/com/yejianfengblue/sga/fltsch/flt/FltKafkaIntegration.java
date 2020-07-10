package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.fltsch.flt.Flt.FltEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@RepositoryEventHandler
@ConditionalOnProperty(name = "app.flt.kafka.enabled", havingValue = "true")
@Component
@Slf4j
@RequiredArgsConstructor
public class FltKafkaIntegration {

    @NonNull
    private final KafkaTemplate kafkaTemplate;

    private static final String TOPIC = "flt";

    @NonNull
    private final ObjectMapper objectMapper;

    @HandleAfterCreate
    public void handleFltCreate(Flt flt) {

        FltEvent fltEvent = FltEvent.of(flt, FltEvent.Type.CREATE);
        Message<?> message = MessageBuilder.withPayload(fltEvent)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        kafkaTemplate.send(message);
        log.debug("Send to Kafka: {}", message);
    }

    @HandleAfterSave
    public void handleFltSave(Flt flt) {

        FltEvent fltEvent = FltEvent.of(flt, FltEvent.Type.UPDATE);
        Message<?> message = MessageBuilder.withPayload(fltEvent)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        kafkaTemplate.send(message);
        log.debug("Send to Kafka: {}", message);
    }

}
