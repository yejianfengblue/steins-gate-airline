package com.yejianfengblue.sga.fltsch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    /**
     * {@link KafkaAutoConfiguration#properties}
     */
    private final KafkaProperties properties;

    /**
     * A {@link JsonSerializer} using the {@link ObjectMapper} configured in the Spring context.
     * This JsonSerializer is supposed to be used to construct a {@link ProducerFactory}.
     * Using the same ObjectMapper makes Kafka JSON serialization consistent with web JSON serialization.
     * @param objectMapper  the ObjectMapper configured in the Spring context.
     */
    @Bean
    public JsonSerializer jsonSerializer(ObjectMapper objectMapper) {

        Map<String, Object> jsonSerializerProp = new HashMap<>();
        JsonSerializer jsonSerializer = new JsonSerializer(objectMapper);
        Map<String, Object> stringObjectMap = properties.buildProducerProperties();
        jsonSerializer.configure(properties.buildProducerProperties(), false);

        return jsonSerializer;
    }

    /**
     * A {@link ProducerFactory} configured with {@link KafkaProperties}, default key serializer,
     * and a value serializer {@link JsonSerializer} configured in the Spring context.
     * @param jsonSerializer  {@link #jsonSerializer(ObjectMapper)}
     */
    @Bean
    public ProducerFactory<?, ?> kafkaProducerFactory(JsonSerializer jsonSerializer) {

        DefaultKafkaProducerFactory<?, ?> kafkaProducerFactory = new DefaultKafkaProducerFactory<>(
                this.properties.buildProducerProperties(),
                () -> null,
                () -> jsonSerializer);
        String transactionIdPrefix = this.properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            kafkaProducerFactory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return kafkaProducerFactory;
    }
}
