package com.yejianfengblue.sga.search.inventory;

import com.yejianfengblue.sga.search.common.ServiceType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class InventoryControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void configMockMvc(WebApplicationContext webAppContext) {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .build();
    }

    @AfterEach
    void cleanTestData() {
        inventoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void pagingParameterSizeIs20ByDefault() {

        // populate 30 test inventories
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 31)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );

        mockMvc
                .perform(get("/inventories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(20)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[0]._links.self.href").exists())
                .andExpect(jsonPath("_embedded.inventories[19].fltDate").value("2020-01-20"))
                .andExpect(jsonPath("_links.first.href").value("http://localhost/inventories?page=0&size=20"))
                .andExpect(jsonPath("_links.self.href").value("http://localhost/inventories?page=0&size=20"))
                .andExpect(jsonPath("_links.next.href").value("http://localhost/inventories?page=1&size=20"))
                .andExpect(jsonPath("_links.last.href").value("http://localhost/inventories?page=1&size=20"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(30))
                .andExpect(jsonPath("page.totalPages").value(2))
                .andDo(print());

    }

    @Test
    @SneakyThrows
    void pagingParameterPageAndSizeTest() {

        // populate 30 test inventories
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 31)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );

        // page 0
        mockMvc
                .perform(get("/inventories?size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(10)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[0]._links.self.href").exists())
                .andExpect(jsonPath("_embedded.inventories[9].fltDate").value("2020-01-10"))
                .andExpect(jsonPath("_links.first.href").value("http://localhost/inventories?page=0&size=10"))
                .andExpect(jsonPath("_links.self.href").value("http://localhost/inventories?page=0&size=10"))
                .andExpect(jsonPath("_links.next.href").value("http://localhost/inventories?page=1&size=10"))
                .andExpect(jsonPath("_links.last.href").value("http://localhost/inventories?page=2&size=10"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("page.size").value(10))
                .andExpect(jsonPath("page.totalElements").value(30))
                .andExpect(jsonPath("page.totalPages").value(3))
                .andDo(print());

        // page 1
        mockMvc
                .perform(get("/inventories?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(10)))
                .andExpect(jsonPath("_embedded.inventories[0]._links.self.href").exists())
                .andExpect(jsonPath("_links.first.href").value("http://localhost/inventories?page=0&size=10"))
                .andExpect(jsonPath("_links.self.href").value("http://localhost/inventories?page=1&size=10"))
                .andExpect(jsonPath("_links.next.href").value("http://localhost/inventories?page=2&size=10"))
                .andExpect(jsonPath("_links.last.href").value("http://localhost/inventories?page=2&size=10"))
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("page.size").value(10))
                .andExpect(jsonPath("page.totalElements").value(30))
                .andExpect(jsonPath("page.totalPages").value(3))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void sortingTest() {

        // populate test inventories
        // SG001 / 2020-01-01 ~ 2020-01-02
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 3)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );
        // SG002 / 2020-01-01 ~ 2020-01-02
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 3)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "002", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );

        // default sorting
        mockMvc
                .perform(get("/inventories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(4)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[0].fltNum").value("001"))
                .andExpect(jsonPath("_embedded.inventories[1].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[1].fltNum").value("001"))
                .andExpect(jsonPath("_embedded.inventories[2].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[2].fltNum").value("002"))
                .andExpect(jsonPath("_embedded.inventories[3].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[3].fltNum").value("002"))
                .andDo(print());

        // sort by fltDate desc and fltNum desc
        mockMvc
                .perform(get("/inventories?sort=fltDate,desc&sort=fltNum,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(4)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[0].fltNum").value("002"))
                .andExpect(jsonPath("_embedded.inventories[1].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[1].fltNum").value("001"))
                .andExpect(jsonPath("_embedded.inventories[2].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[2].fltNum").value("002"))
                .andExpect(jsonPath("_embedded.inventories[3].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[3].fltNum").value("001"))
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void querydslWebSupportTest() {

        // populate test inventories
        // SG001 / 2020-01-01 ~ 2020-01-02
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 3)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );
        // SG002 / 2020-01-01 ~ 2020-01-02
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 3)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "002", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 00), fltDate.atTime(14, 00), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );

        // sort by fltNum asc and fltDate asc, without querydsl parameter
        mockMvc
                .perform(get("/inventories?sort=fltNum,asc&sort=fltDate,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(4)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[0].fltNum").value("001"))
                .andExpect(jsonPath("_embedded.inventories[1].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[1].fltNum").value("001"))
                .andExpect(jsonPath("_embedded.inventories[2].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[2].fltNum").value("002"))
                .andExpect(jsonPath("_embedded.inventories[3].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[3].fltNum").value("002"));

        // sort by fltNum asc and fltDate asc, with one querydsl parameter
        mockMvc
                .perform(get("/inventories?sort=fltNum,asc&sort=fltDate,asc&fltNum=001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(2)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[0].fltNum").value("001"))
                .andExpect(jsonPath("_embedded.inventories[1].fltDate").value("2020-01-02"))
                .andExpect(jsonPath("_embedded.inventories[1].fltNum").value("001"));

        // sort by fltNum asc and fltDate asc, with multiple querydsl parameters
        mockMvc
                .perform(get("/inventories?sort=fltNum,asc&sort=fltDate,asc&fltNum=001&fltDate=2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(1)))
                .andExpect(jsonPath("_embedded.inventories[0].fltDate").value("2020-01-01"))
                .andExpect(jsonPath("_embedded.inventories[0].fltNum").value("001"));
    }

}
