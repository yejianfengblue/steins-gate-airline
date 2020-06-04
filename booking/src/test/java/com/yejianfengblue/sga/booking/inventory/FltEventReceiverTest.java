package com.yejianfengblue.sga.booking.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yejianfengblue.sga.booking.common.ServiceType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.stream.function.definition = fltEventReceiver"
})
@Import(TestChannelBinderConfiguration.class)
public class FltEventReceiverTest {

    @Autowired
    InputDestination inputDestination;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @AfterEach
    void clean() {
        inventoryRepository.deleteAll();
    }

    @Test
    void whenReceiveCreatedFltEvent_thenFltIsCreatedAndInventoryIsCreated() {

        Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(
                "SG", "520", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isEmpty();

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

        // when
        inputDestination.send(
                MessageBuilder.withPayload(fltEventJson.toString()).build(),
                "fltEventReceiver-in-0");

        // then
        foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(
                "SG", "520", LocalDate.of(2020, 1, 1));
        assertThat(foundInventory).isPresent();
        Inventory inventory = foundInventory.get();
        assertThat(inventory.getCarrier()).isEqualTo(fltJson.get("carrier").asText());
        assertThat(inventory.getFltNum()).isEqualTo(fltJson.get("fltNum").asText());
        assertThat(inventory.getServiceType()).isEqualTo(ServiceType.valueOf(fltJson.get("serviceType").asText()));
        assertThat(inventory.getFltDate()).isEqualTo(fltJson.get("fltDate").asText());
        assertThat(inventory.getFltDow()).isEqualTo(fltJson.get("fltDow").asInt());

        List<InventoryLeg> inventoryLegList = inventory.getLegs();
        assertThat(inventoryLegList).hasSize(1);
        InventoryLeg inventoryLeg = inventoryLegList.get(0);
        assertThat(inventoryLeg.getDepDate()).isEqualTo(fltLegHkgNrtJson.get("depDate").asText());
        assertThat(inventoryLeg.getDepDow()).isEqualTo(fltLegHkgNrtJson.get("depDow").asInt());
        assertThat(inventoryLeg.getLegDep()).isEqualTo(fltLegHkgNrtJson.get("legDep").asText());
        assertThat(inventoryLeg.getLegArr()).isEqualTo(fltLegHkgNrtJson.get("legArr").asText());
        assertThat(inventoryLeg.getLegSeqNum()).isEqualTo(fltLegHkgNrtJson.get("legSeqNum").asInt());
        assertThat(inventoryLeg.getSchDepTime()).isEqualTo(fltLegHkgNrtJson.get("schDepTime").asText());
        assertThat(inventoryLeg.getSchArrTime()).isEqualTo(fltLegHkgNrtJson.get("schArrTime").asText());
        assertThat(inventoryLeg.getDepTimeDiff()).isEqualTo(fltLegHkgNrtJson.get("depTimeDiff").asInt());
        assertThat(inventoryLeg.getArrTimeDiff()).isEqualTo(fltLegHkgNrtJson.get("arrTimeDiff").asInt());
        assertThat(inventoryLeg.getAvailable()).isEqualTo(100);

        assertThat(inventory.getCreatedDate()).isNotNull();
        assertThat(inventory.getLastModifiedDate()).isNotNull();

    }

}
