package com.yejianfengblue.sga.booking.inventory.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yejianfengblue.sga.booking.inventory.Inventory;
import com.yejianfengblue.sga.booking.inventory.InventoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDate;
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

        Optional<Inventory> inventory_sg520_20200101 = inventoryRepository.findByCarrierAndFltNumAndFltDate(
                "SG", "520", LocalDate.of(2020, 1, 1));
        assertThat(inventory_sg520_20200101).isEmpty();

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
        inventory_sg520_20200101 = inventoryRepository.findByCarrierAndFltNumAndFltDate(
                "SG", "520", LocalDate.of(2020, 1, 1));
        assertThat(inventory_sg520_20200101).isPresent();
        assertThat(inventory_sg520_20200101.get().getCarrier()).isEqualTo("SG");
        assertThat(inventory_sg520_20200101.get().getFltNum()).isEqualTo("520");
        assertThat(inventory_sg520_20200101.get().getFltDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(inventory_sg520_20200101.get().getAvailable()).isEqualTo(100);
        assertThat(inventory_sg520_20200101.get().getCreatedDate()).isNotNull();
        assertThat(inventory_sg520_20200101.get().getLastModifiedDate()).isNotNull();

    }

}
