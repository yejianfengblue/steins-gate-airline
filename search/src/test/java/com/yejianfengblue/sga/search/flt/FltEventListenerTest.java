package com.yejianfengblue.sga.search.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.AbstractJavaTypeMapper;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.consumer.value-deserializer = org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.type.mapping = fltEvent:com.yejianfengblue.sga.search.flt.FltEvent",
        "spring.kafka.consumer.auto-offset-reset = earliest",
        "spring.kafka.consumer.bootstrap-servers = PLAINTEXT://localhost:9093"
})
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9093"
        })
@Slf4j
public class FltEventListenerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    FltEventHandler fltEventHandler;

    @Test
    @SneakyThrows
    void fltEventListenerTest() {

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(this.embeddedKafka);
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<String, String>(producerProps);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);

        ObjectNode fltEventJson = objectMapper.createObjectNode();
        fltEventJson.put("id", UUID.randomUUID().toString());
        fltEventJson.put("timestamp", "2020-01-01T01:00:00Z");
        fltEventJson.put("type", "CREATE");
        ObjectNode fltJson = objectMapper.createObjectNode();
        fltJson.put("carrier", "SG");
        fltJson.put("fltNum", "520");
        fltJson.put("serviceType", "PAX");
        fltJson.put("fltDate", "2020-01-01");
        fltJson.put("fltDow", 3);
        fltJson.put("createdBy", "chris");
        fltJson.put("createdDate", "2020-01-01T01:00:00Z");
        fltJson.put("lastModifiedBy", "chris");
        fltJson.put("lastModifiedDate", "2020-01-01T01:00:00Z");
        ArrayNode fltLegsJsonArray = objectMapper.createArrayNode();
        ObjectNode fltLegHkgNrtJson = objectMapper.createObjectNode();
        fltLegHkgNrtJson.put("depDate", "2020-01-01");
        fltLegHkgNrtJson.put("depDow", 3);
        fltLegHkgNrtJson.put("legDep", "HKG");
        fltLegHkgNrtJson.put("legArr", "NRT");
        fltLegHkgNrtJson.put("legSeqNum", 1);
        fltLegHkgNrtJson.put("schDepTime", "2020-01-01T10:00:00");
        fltLegHkgNrtJson.put("schArrTime", "2020-01-01T16:00:00");
        fltLegHkgNrtJson.put("depTimeDiff", 480);
        fltLegHkgNrtJson.put("arrTimeDiff", 540);
        fltLegHkgNrtJson.put("acReg", "B-LAD");
        fltLegHkgNrtJson.put("iataAcType", "333");
        fltLegsJsonArray.add(fltLegHkgNrtJson);
        fltJson.set("fltLegs", fltLegsJsonArray);
        fltEventJson.set("flt", fltJson);
        template.setDefaultTopic(FltEventListener.TOPIC);

        // send a message to topic "flt"
        template.send(MessageBuilder
                .withPayload(objectMapper.writeValueAsString(fltEventJson))
                .setHeader(AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME, "fltEvent")
                .build());
        // wait, make sure the FltEventListener which is running in another thread does receive message before verify
        TimeUnit.SECONDS.sleep(1);
        // FltEventListener receives the message and call FltEventHandler.handle(FltEvent) once
        verify(fltEventHandler, times(1)).handle(any(FltEvent.class));
    }

}