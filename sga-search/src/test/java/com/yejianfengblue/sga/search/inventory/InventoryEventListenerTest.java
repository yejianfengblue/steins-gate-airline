package com.yejianfengblue.sga.search.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yejianfengblue.sga.search.common.ServiceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.kafka.consumer.value-deserializer = org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.bootstrap-servers = ${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka
@Slf4j
public class InventoryEventListenerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    InventoryEventHandler inventoryEventHandler;

    @BeforeEach
    void setup() {
        ContainerTestUtils.waitForAssignment(
                kafkaListenerEndpointRegistry.getListenerContainer("inventory-search-group"),
                embeddedKafka.getPartitionsPerTopic());
    }

    @Test
    @SneakyThrows
    void whenInventoryEventMessageIsSentToTopic_thenInventoryEventListenerListenIsExecuted() {

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(this.embeddedKafka);
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);

        ObjectNode inventoryEventJson = objectMapper.createObjectNode();
        inventoryEventJson.put("id", UUID.randomUUID().toString());
        inventoryEventJson.put("timestamp", "2020-01-01T01:00:00Z");
        ObjectNode inventoryJson = objectMapper.createObjectNode();
        inventoryJson.put("carrier", "SG");
        inventoryJson.put("fltNum", "520");
        inventoryJson.put("serviceType", "PAX");
        inventoryJson.put("fltDate", "2020-01-01");
        inventoryJson.put("fltDow", 3);
        inventoryJson.put("createdDate", "2020-01-01T01:00:00Z");
        inventoryJson.put("lastModifiedDate", "2020-01-01T01:00:00Z");
        ArrayNode inventoryLegsJsonArray = objectMapper.createArrayNode();
        ObjectNode inventoryLegHkgNrtJson = objectMapper.createObjectNode();
        inventoryLegHkgNrtJson.put("depDate", "2020-01-01");
        inventoryLegHkgNrtJson.put("depDow", 3);
        inventoryLegHkgNrtJson.put("legDep", "HKG");
        inventoryLegHkgNrtJson.put("legArr", "NRT");
        inventoryLegHkgNrtJson.put("legSeqNum", 1);
        inventoryLegHkgNrtJson.put("schDepTime", "2020-01-01T10:00:00");
        inventoryLegHkgNrtJson.put("schArrTime", "2020-01-01T16:00:00");
        inventoryLegHkgNrtJson.put("depTimeDiff", 480);
        inventoryLegHkgNrtJson.put("arrTimeDiff", 540);
        inventoryLegHkgNrtJson.put("available", 100);
        inventoryLegsJsonArray.add(inventoryLegHkgNrtJson);
        inventoryJson.set("legs", inventoryLegsJsonArray);
        inventoryEventJson.set("inventory", inventoryJson);
        template.setDefaultTopic(InventoryEventListener.TOPIC);

        // send a message to topic
        template.send(MessageBuilder
                .withPayload(objectMapper.writeValueAsString(inventoryEventJson))
                .build());

        // wait, make sure the InventoryEventListener which is running in another thread receive message before verify
        TimeUnit.SECONDS.sleep(1);

        ArgumentCaptor<InventoryEvent> inventoryEventCaptor = ArgumentCaptor.forClass(InventoryEvent.class);
        // InventoryEventListener.listen() receives the message and calls FltEventHandler.handle() once
        verify(inventoryEventHandler).handle(inventoryEventCaptor.capture());
        // verify received InventoryEvent
        InventoryEvent capturedInventoryEvent = inventoryEventCaptor.getValue();
        assertThat(capturedInventoryEvent.getId().toString()).isEqualTo(inventoryEventJson.get("id").asText());
        assertThat(capturedInventoryEvent.getTimestamp()).isEqualTo(inventoryEventJson.get("timestamp").asText());
        Inventory capturedInventory = capturedInventoryEvent.getInventory();
        assertThat(capturedInventory.getCarrier()).isEqualTo(inventoryJson.get("carrier").asText());
        assertThat(capturedInventory.getFltNum()).isEqualTo(inventoryJson.get("fltNum").asText());
        assertThat(capturedInventory.getServiceType()).isEqualTo(ServiceType.valueOf(inventoryJson.get("serviceType").asText()));
        assertThat(capturedInventory.getFltDate()).isEqualTo(inventoryJson.get("fltDate").asText());
        assertThat(capturedInventory.getFltDow()).isEqualTo(inventoryJson.get("fltDow").asInt());
        assertThat(capturedInventory.getCreatedDate()).isEqualTo(inventoryJson.get("createdDate").asText());
        assertThat(capturedInventory.getLastModifiedDate()).isEqualTo(inventoryJson.get("lastModifiedDate").asText());
        List<InventoryLeg> capturedInventoryLegs = capturedInventory.getLegs();
        assertThat(capturedInventoryLegs).hasSize(1);
        InventoryLeg capturedFltLeg0 = capturedInventoryLegs.get(0);
        assertThat(capturedFltLeg0.getDepDate()).isEqualTo(inventoryLegHkgNrtJson.get("depDate").asText());
        assertThat(capturedFltLeg0.getDepDow()).isEqualTo(inventoryLegHkgNrtJson.get("depDow").asInt());
        assertThat(capturedFltLeg0.getLegSeqNum()).isEqualTo(inventoryLegHkgNrtJson.get("legSeqNum").asInt());
        assertThat(capturedFltLeg0.getLegDep()).isEqualTo(inventoryLegHkgNrtJson.get("legDep").asText());
        assertThat(capturedFltLeg0.getLegArr()).isEqualTo(inventoryLegHkgNrtJson.get("legArr").asText());
        assertThat(capturedFltLeg0.getSchDepTime().format(ISO_LOCAL_DATE_TIME)).isEqualTo(inventoryLegHkgNrtJson.get("schDepTime").asText());
        assertThat(capturedFltLeg0.getSchArrTime().format(ISO_LOCAL_DATE_TIME)).isEqualTo(inventoryLegHkgNrtJson.get("schArrTime").asText());
        assertThat(capturedFltLeg0.getDepTimeDiff()).isEqualTo(inventoryLegHkgNrtJson.get("depTimeDiff").asInt());
        assertThat(capturedFltLeg0.getArrTimeDiff()).isEqualTo(inventoryLegHkgNrtJson.get("arrTimeDiff").asInt());
        assertThat(capturedFltLeg0.getAvailable()).isEqualTo(inventoryLegHkgNrtJson.get("available").asInt());

    }
}
