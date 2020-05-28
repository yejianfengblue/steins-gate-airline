package com.yejianfengblue.sga.booking.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static com.yejianfengblue.sga.booking.inventory.InventoryEventHandler.INVENTORY_EVENT_OUT_BINDING;
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

        // when
        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));

        // then
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("available").asInt()).isEqualTo(inventory.getAvailable());
    }

    @Test
    @SneakyThrows
    void whenInventoryIsCreatedWithinTransaction_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        // when
        Inventory inventory = transactionTemplate.execute(status ->
                inventoryRepository.save(
                        new Inventory(carrier, fltNum, fltDate, 100)));

        // then
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(inventory.getFltNum());
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(inventory.getFltDate().toString());
        assertThat(inventoryJson.get("available").asInt()).isEqualTo(inventory.getAvailable());
    }

    @Test
    @SneakyThrows
    void whenInventoryIsCreatedWithinTransactionButRollback_thenInventoryEventIsNotSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        // when
        transactionTemplate.executeWithoutResult(status -> {
            Inventory i = inventoryRepository.save(
                    new Inventory(carrier, fltNum, fltDate, 100));
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

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        inventory.add(1);
        inventory = inventoryRepository.save(inventory);

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(carrier);
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(fltNum);
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(fltDate.toString());
        assertThat(inventoryJson.get("available").asInt()).isEqualTo(101);
    }

    @Test
    @SneakyThrows
    void whenAddAvailableToInventoryWithinTransaction_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().add(1);
            inventoryRepository.save(foundInventory.get());
        });

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(carrier);
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(fltNum);
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(fltDate.toString());
        assertThat(inventoryJson.get("available").asInt()).isEqualTo(101);
    }

    @Test
    @SneakyThrows
    void whenAddAvailableToInventoryWithinTransactionButRollback_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().add(1);
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

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        inventory.subtract(1);
        inventoryRepository.save(inventory);

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNotNull();
        JsonNode inventoryEventJson = objectMapper.readTree(new String(inventoryEventMessage.getPayload()));
        assertThat(inventoryEventJson.hasNonNull("id")).isTrue();
        assertThat(inventoryEventJson.hasNonNull("timestamp")).isTrue();
        JsonNode inventoryJson = inventoryEventJson.get("inventory");
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(inventory.getCarrier());
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(carrier);
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(fltNum);
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(fltDate.toString());
        assertThat(inventoryJson.get("available").asInt()).isEqualTo(99);
    }

    @Test
    @SneakyThrows
    void whenSubtractAvailableFromInventoryWithinTransaction_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().subtract(1);
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
        assertThat(inventoryJson.get("carrier").asText()).isEqualTo(carrier);
        assertThat(inventoryJson.get("fltNum").asText()).isEqualTo(fltNum);
        assertThat(inventoryJson.get("fltDate").asText()).isEqualTo(fltDate.toString());
        assertThat(inventoryJson.get("available").asInt()).isEqualTo(99);
    }

    @Test
    @SneakyThrows
    void whenSubtractAvailableFromInventoryWithinTransactionButRollback_thenInventoryEventIsSent() {

        String carrier = "SG", fltNum = "001";
        LocalDate fltDate = LocalDate.of(2020, 1, 1);

        Inventory inventory = inventoryRepository.save(
                new Inventory(carrier, fltNum, fltDate, 100));
        // consume the event published by the save
        Message<byte[]> inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);

        // when
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Optional<Inventory> foundInventory = inventoryRepository.findByCarrierAndFltNumAndFltDate(carrier, fltNum, fltDate);
            assertThat(foundInventory).isPresent();
            foundInventory.get().subtract(1);
            inventoryRepository.save(foundInventory.get());
            transactionStatus.setRollbackOnly();
        });

        // then
        inventoryEventMessage = outputDestination.receive(100, INVENTORY_EVENT_OUT_BINDING);
        assertThat(inventoryEventMessage).isNull();
    }
}
