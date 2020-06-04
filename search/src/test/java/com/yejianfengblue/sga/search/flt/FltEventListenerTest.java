package com.yejianfengblue.sga.search.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yejianfengblue.sga.search.common.ServiceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
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
    void whenFltEventMessageIsSentToTopic_thenFltEventListenerListenIsExecuted() {

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

        ArgumentCaptor<FltEvent> fltEventCaptor = ArgumentCaptor.forClass(FltEvent.class);
        // FltEventListener receives the message and call FltEventHandler.handle(FltEvent) once
        verify(fltEventHandler, times(1)).handle(fltEventCaptor.capture());
        // verify received FltEvent value
        FltEvent capturedFltEvent = fltEventCaptor.getValue();
        assertThat(capturedFltEvent.getId().toString()).isEqualTo(fltEventJson.get("id").asText());
        assertThat(capturedFltEvent.getTimestamp()).isEqualTo(fltEventJson.get("timestamp").asText());
        assertThat(capturedFltEvent.getType()).isEqualTo(FltEvent.Type.valueOf(fltEventJson.get("type").asText()));
        Flt capturedFlt = capturedFltEvent.getFlt();
        assertThat(capturedFlt.getCarrier()).isEqualTo(fltJson.get("carrier").asText());
        assertThat(capturedFlt.getFltNum()).isEqualTo(fltJson.get("fltNum").asText());
        assertThat(capturedFlt.getServiceType()).isEqualTo(ServiceType.valueOf(fltJson.get("serviceType").asText()));
        assertThat(capturedFlt.getFltDate()).isEqualTo(fltJson.get("fltDate").asText());
        assertThat(capturedFlt.getFltDow()).isEqualTo(fltJson.get("fltDow").asInt());
        assertThat(capturedFlt.getCreatedBy()).isEqualTo(fltJson.get("createdBy").asText());
        assertThat(capturedFlt.getCreatedDate().toString()).isEqualTo(fltJson.get("createdDate").asText());
        assertThat(capturedFlt.getLastModifiedBy()).isEqualTo(fltJson.get("lastModifiedBy").asText());
        assertThat(capturedFlt.getLastModifiedDate()).isEqualTo(fltJson.get("lastModifiedDate").asText());
        List<FltLeg> capturedFltLegs = capturedFlt.getFltLegs();
        assertThat(capturedFltLegs).hasSize(1);
        FltLeg capturedFltLeg0 = capturedFltLegs.get(0);
        assertThat(capturedFltLeg0.getDepDate()).isEqualTo(fltLegHkgNrtJson.get("depDate").asText());
        assertThat(capturedFltLeg0.getDepDow()).isEqualTo(fltLegHkgNrtJson.get("depDow").asInt());
        assertThat(capturedFltLeg0.getLegSeqNum()).isEqualTo(fltLegHkgNrtJson.get("legSeqNum").asInt());
        assertThat(capturedFltLeg0.getLegDep()).isEqualTo(fltLegHkgNrtJson.get("legDep").asText());
        assertThat(capturedFltLeg0.getLegArr()).isEqualTo(fltLegHkgNrtJson.get("legArr").asText());
        assertThat(capturedFltLeg0.getSchDepTime().format(ISO_LOCAL_DATE_TIME)).isEqualTo(fltLegHkgNrtJson.get("schDepTime").asText());
        assertThat(capturedFltLeg0.getSchArrTime().format(ISO_LOCAL_DATE_TIME)).isEqualTo(fltLegHkgNrtJson.get("schArrTime").asText());
        assertThat(capturedFltLeg0.getEstDepTime()).isNull();
        assertThat(capturedFltLeg0.getEstArrTime()).isNull();
        assertThat(capturedFltLeg0.getActDepTime()).isNull();
        assertThat(capturedFltLeg0.getActArrTime()).isNull();
        assertThat(capturedFltLeg0.getDepTimeDiff()).isEqualTo(fltLegHkgNrtJson.get("depTimeDiff").asInt());
        assertThat(capturedFltLeg0.getArrTimeDiff()).isEqualTo(fltLegHkgNrtJson.get("arrTimeDiff").asInt());
        assertThat(capturedFltLeg0.getAcReg()).isEqualTo(fltLegHkgNrtJson.get("acReg").asText());
        assertThat(capturedFltLeg0.getIataAcType()).isEqualTo(fltLegHkgNrtJson.get("iataAcType").asText());

    }

}