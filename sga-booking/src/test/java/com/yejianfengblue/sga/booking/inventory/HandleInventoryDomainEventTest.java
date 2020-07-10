package com.yejianfengblue.sga.booking.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.booking.common.ServiceType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.yejianfengblue.sga.booking.inventory.InventoryEventHandler.INVENTORY_EVENT_OUT_BINDING;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {"spring.cloud.stream.source = inventory"}
)
@Import(TestChannelBinderConfiguration.class)
public class HandleInventoryDomainEventTest {

    @Autowired
    private OutputDestination outputDestination;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void deleteTestData() {

        inventoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void whenInventoryIsCreated_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);

        // when
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(inventoryLeg)));

        // then
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("serviceType").asText()).isEqualTo(inventory.getServiceType().toString());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("fltDow").asInt()).isEqualTo(inventory.getFltDow());

        JsonNode inventoryLegsJson = inventoryJson.get("legs");
        assertThat(inventoryLegsJson.isArray()).isTrue();
        assertThat(inventoryLegsJson.size()).isEqualTo(1);
        JsonNode inventoryLegJson = inventoryLegsJson.get(0);
        assertThat(inventoryLegJson.get("depDate").asText()).isEqualTo(inventoryLeg.getDepDate().toString());
        assertThat(inventoryLegJson.get("depDow").asInt()).isEqualTo(inventoryLeg.getDepDow());
        assertThat(inventoryLegJson.get("legDep").asText()).isEqualTo(inventoryLeg.getLegDep());
        assertThat(inventoryLegJson.get("legArr").asText()).isEqualTo(inventoryLeg.getLegArr());
        assertThat(inventoryLegJson.get("schDepTime").asText()).isEqualTo(inventoryLeg.getSchDepTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("schArrTime").asText()).isEqualTo(inventoryLeg.getSchArrTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("depTimeDiff").asInt()).isEqualTo(inventoryLeg.getDepTimeDiff());
        assertThat(inventoryLegJson.get("arrTimeDiff").asInt()).isEqualTo(inventoryLeg.getArrTimeDiff());
        assertThat(inventoryLegJson.get("available").asInt()).isEqualTo(inventoryLeg.getAvailable());

        assertThat(inventoryJson.hasNonNull("createdDate")).isTrue();
        assertThat(inventoryJson.hasNonNull("lastModifiedDate")).isTrue();
    }

    @Test
    @SneakyThrows
    void whenInventoryIsCreatedWithinTransaction_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);

        // when
        Inventory inventory = transactionTemplate.execute(status ->
                inventoryRepository.save(
                        new Inventory(carrier, fltNum, ServiceType.PAX,
                                fltDate, fltDate.getDayOfWeek().getValue(),
                                List.of(inventoryLeg))));

        // then
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("serviceType").asText()).isEqualTo(inventory.getServiceType().toString());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("fltDow").asInt()).isEqualTo(inventory.getFltDow());

        JsonNode inventoryLegsJson = inventoryJson.get("legs");
        assertThat(inventoryLegsJson.isArray()).isTrue();
        assertThat(inventoryLegsJson.size()).isEqualTo(1);
        JsonNode inventoryLegJson = inventoryLegsJson.get(0);
        assertThat(inventoryLegJson.get("depDate").asText()).isEqualTo(inventoryLeg.getDepDate().toString());
        assertThat(inventoryLegJson.get("depDow").asInt()).isEqualTo(inventoryLeg.getDepDow());
        assertThat(inventoryLegJson.get("legDep").asText()).isEqualTo(inventoryLeg.getLegDep());
        assertThat(inventoryLegJson.get("legArr").asText()).isEqualTo(inventoryLeg.getLegArr());
        assertThat(inventoryLegJson.get("schDepTime").asText()).isEqualTo(inventoryLeg.getSchDepTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("schArrTime").asText()).isEqualTo(inventoryLeg.getSchArrTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("depTimeDiff").asInt()).isEqualTo(inventoryLeg.getDepTimeDiff());
        assertThat(inventoryLegJson.get("arrTimeDiff").asInt()).isEqualTo(inventoryLeg.getArrTimeDiff());
        assertThat(inventoryLegJson.get("available").asInt()).isEqualTo(inventoryLeg.getAvailable());

        assertThat(inventoryJson.hasNonNull("createdDate")).isTrue();
        assertThat(inventoryJson.hasNonNull("lastModifiedDate")).isTrue();
    }

    @Test
    @SneakyThrows
    void whenInventoryIsCreatedWithinTransactionButRollback_thenInventoryEventIsNotSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);

        // when
        transactionTemplate.executeWithoutResult(status -> {
            Inventory i = inventoryRepository.save(
                    new Inventory(carrier, fltNum, ServiceType.PAX,
                            fltDate, fltDate.getDayOfWeek().getValue(),
                            List.of(inventoryLeg)));
            status.setRollbackOnly();
        });

        // then
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNull();
    }

    @Test
    @SneakyThrows
    void whenAddAvailableToInventory_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(inventoryLeg)));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        inventory.addAvailable(legDep, legArr, 1);
        inventoryRepository.save(inventory);

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("serviceType").asText()).isEqualTo(inventory.getServiceType().toString());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("fltDow").asInt()).isEqualTo(inventory.getFltDow());

        JsonNode inventoryLegsJson = inventoryJson.get("legs");
        assertThat(inventoryLegsJson.isArray()).isTrue();
        assertThat(inventoryLegsJson.size()).isEqualTo(1);
        JsonNode inventoryLegJson = inventoryLegsJson.get(0);
        assertThat(inventoryLegJson.get("depDate").asText()).isEqualTo(inventoryLeg.getDepDate().toString());
        assertThat(inventoryLegJson.get("depDow").asInt()).isEqualTo(inventoryLeg.getDepDow());
        assertThat(inventoryLegJson.get("legDep").asText()).isEqualTo(inventoryLeg.getLegDep());
        assertThat(inventoryLegJson.get("legArr").asText()).isEqualTo(inventoryLeg.getLegArr());
        assertThat(inventoryLegJson.get("schDepTime").asText()).isEqualTo(inventoryLeg.getSchDepTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("schArrTime").asText()).isEqualTo(inventoryLeg.getSchArrTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("depTimeDiff").asInt()).isEqualTo(inventoryLeg.getDepTimeDiff());
        assertThat(inventoryLegJson.get("arrTimeDiff").asInt()).isEqualTo(inventoryLeg.getArrTimeDiff());
        assertThat(inventoryLegJson.get("available").asInt()).isEqualTo(101);

        assertThat(inventoryJson.hasNonNull("createdDate")).isTrue();
        assertThat(inventoryJson.hasNonNull("lastModifiedDate")).isTrue();
    }

    @Test
    @SneakyThrows
    void whenAddAvailableToInventoryWithinTransaction_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(inventoryLeg)));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().addAvailable(legDep, legArr, 1);
            inventoryRepository.save(foundInventory.get());
        });

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("serviceType").asText()).isEqualTo(inventory.getServiceType().toString());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("fltDow").asInt()).isEqualTo(inventory.getFltDow());

        JsonNode inventoryLegsJson = inventoryJson.get("legs");
        assertThat(inventoryLegsJson.isArray()).isTrue();
        assertThat(inventoryLegsJson.size()).isEqualTo(1);
        JsonNode inventoryLegJson = inventoryLegsJson.get(0);
        assertThat(inventoryLegJson.get("depDate").asText()).isEqualTo(inventoryLeg.getDepDate().toString());
        assertThat(inventoryLegJson.get("depDow").asInt()).isEqualTo(inventoryLeg.getDepDow());
        assertThat(inventoryLegJson.get("legDep").asText()).isEqualTo(inventoryLeg.getLegDep());
        assertThat(inventoryLegJson.get("legArr").asText()).isEqualTo(inventoryLeg.getLegArr());
        assertThat(inventoryLegJson.get("schDepTime").asText()).isEqualTo(inventoryLeg.getSchDepTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("schArrTime").asText()).isEqualTo(inventoryLeg.getSchArrTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("depTimeDiff").asInt()).isEqualTo(inventoryLeg.getDepTimeDiff());
        assertThat(inventoryLegJson.get("arrTimeDiff").asInt()).isEqualTo(inventoryLeg.getArrTimeDiff());
        assertThat(inventoryLegJson.get("available").asInt()).isEqualTo(101);

        assertThat(inventoryJson.hasNonNull("createdDate")).isTrue();
        assertThat(inventoryJson.hasNonNull("lastModifiedDate")).isTrue();
    }

    @Test
    @SneakyThrows
    void whenAddAvailableToInventoryWithinTransactionButRollback_thenInventoryEventIsNotSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                                legDep, legArr, 1,
                                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                                100))));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().addAvailable(legDep, legArr, 1);
            inventoryRepository.save(foundInventory.get());
            transactionStatus.setRollbackOnly();
        });

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNull();
    }

    @Test
    @SneakyThrows
    void whenSubtractAvailableFromInventory_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(inventoryLeg)));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        inventory.subtractAvailable(legDep, legArr, 1);
        inventoryRepository.save(inventory);

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("serviceType").asText()).isEqualTo(inventory.getServiceType().toString());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("fltDow").asInt()).isEqualTo(inventory.getFltDow());

        JsonNode inventoryLegsJson = inventoryJson.get("legs");
        assertThat(inventoryLegsJson.isArray()).isTrue();
        assertThat(inventoryLegsJson.size()).isEqualTo(1);
        JsonNode inventoryLegJson = inventoryLegsJson.get(0);
        assertThat(inventoryLegJson.get("depDate").asText()).isEqualTo(inventoryLeg.getDepDate().toString());
        assertThat(inventoryLegJson.get("depDow").asInt()).isEqualTo(inventoryLeg.getDepDow());
        assertThat(inventoryLegJson.get("legDep").asText()).isEqualTo(inventoryLeg.getLegDep());
        assertThat(inventoryLegJson.get("legArr").asText()).isEqualTo(inventoryLeg.getLegArr());
        assertThat(inventoryLegJson.get("schDepTime").asText()).isEqualTo(inventoryLeg.getSchDepTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("schArrTime").asText()).isEqualTo(inventoryLeg.getSchArrTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("depTimeDiff").asInt()).isEqualTo(inventoryLeg.getDepTimeDiff());
        assertThat(inventoryLegJson.get("arrTimeDiff").asInt()).isEqualTo(inventoryLeg.getArrTimeDiff());
        assertThat(inventoryLegJson.get("available").asInt()).isEqualTo(99);

        assertThat(inventoryJson.hasNonNull("createdDate")).isTrue();
        assertThat(inventoryJson.hasNonNull("lastModifiedDate")).isTrue();
    }

    @Test
    @SneakyThrows
    void whenSubtractAvailableFromInventoryWithinTransaction_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(inventoryLeg)));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().subtractAvailable(legDep, legArr, 1);
            inventoryRepository.save(foundInventory.get());
        });

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("serviceType").asText()).isEqualTo(inventory.getServiceType().toString());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("fltDow").asInt()).isEqualTo(inventory.getFltDow());

        JsonNode inventoryLegsJson = inventoryJson.get("legs");
        assertThat(inventoryLegsJson.isArray()).isTrue();
        assertThat(inventoryLegsJson.size()).isEqualTo(1);
        JsonNode inventoryLegJson = inventoryLegsJson.get(0);
        assertThat(inventoryLegJson.get("depDate").asText()).isEqualTo(inventoryLeg.getDepDate().toString());
        assertThat(inventoryLegJson.get("depDow").asInt()).isEqualTo(inventoryLeg.getDepDow());
        assertThat(inventoryLegJson.get("legDep").asText()).isEqualTo(inventoryLeg.getLegDep());
        assertThat(inventoryLegJson.get("legArr").asText()).isEqualTo(inventoryLeg.getLegArr());
        assertThat(inventoryLegJson.get("schDepTime").asText()).isEqualTo(inventoryLeg.getSchDepTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("schArrTime").asText()).isEqualTo(inventoryLeg.getSchArrTime().format(ISO_LOCAL_DATE_TIME));
        assertThat(inventoryLegJson.get("depTimeDiff").asInt()).isEqualTo(inventoryLeg.getDepTimeDiff());
        assertThat(inventoryLegJson.get("arrTimeDiff").asInt()).isEqualTo(inventoryLeg.getArrTimeDiff());
        assertThat(inventoryLegJson.get("available").asInt()).isEqualTo(99);

        assertThat(inventoryJson.hasNonNull("createdDate")).isTrue();
        assertThat(inventoryJson.hasNonNull("lastModifiedDate")).isTrue();
    }

    @Test
    @SneakyThrows
    void whenSubtractAvailableFromInventoryWithinTransactionButRollback_thenInventoryEventIsNotSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        String legDep = "HKG", legArr = "TPE";
        InventoryLeg inventoryLeg = new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                legDep, legArr, 1,
                fltDate.atTime(10, 00), fltDate.atTime(16, 00), 480, 480,
                100);
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, ServiceType.PAX,
                        fltDate, fltDate.getDayOfWeek().getValue(),
                        List.of(inventoryLeg)));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().subtractAvailable(legDep, legArr, 1);
            inventoryRepository.save(foundInventory.get());
            transactionStatus.setRollbackOnly();
        });

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNull();
    }
}
