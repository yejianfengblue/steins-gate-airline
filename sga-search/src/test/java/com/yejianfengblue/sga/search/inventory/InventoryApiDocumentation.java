package com.yejianfengblue.sga.search.inventory;

import com.yejianfengblue.sga.search.common.ServiceType;
import com.yejianfengblue.sga.search.util.RestdocsUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class InventoryApiDocumentation {

    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void configMockMvc(WebApplicationContext webAppContext, RestDocumentationContextProvider restDoc) {

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .apply(documentationConfiguration(restDoc)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @AfterEach
    void deleteTestData() {
        inventoryRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    void accessInventories() {

        // populate test inventories
        // SG001 / 2020-01-01 ~ 2020-01-02
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 21)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 0), fltDate.atTime(14, 0), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );
        // SG002 / 2020-01-01 ~ 2020-01-02
        LocalDate.of(2020, 1, 1).datesUntil(LocalDate.of(2020, 1, 21)).forEach(fltDate ->
            inventoryRepository.save(new Inventory("SG", "002", ServiceType.PAX,
                    fltDate, fltDate.getDayOfWeek().getValue(),
                    List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                            "HKG", "TPE", 1,
                            fltDate.atTime(10, 0), fltDate.atTime(14, 0), 480, 480,
                            100)),
                    Instant.now(), Instant.now()))
        );

        mockMvc
                .perform(get("/inventories?" +
                        "size=10&page=0" +
                        "&sort=fltNum,asc&sort=fltDate,desc" +
                        "&carrier=SG&fltNum=001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_embedded.inventories").isArray())
                .andExpect(jsonPath("_embedded.inventories", hasSize(10)))
                .andDo(document("inventories-get",
                        requestParameters(
                                parameterWithName("size").description("Optional pagination parameter, the item size per page"),
                                parameterWithName("page").description("Optional pagination parameter, the page to retrieve, starting from 0"),
                                parameterWithName("sort").description("Optional sorting criteria, support multiple criteria"),
                                parameterWithName("carrier").optional().description("Optional filter parameter flight carrier"),
                                parameterWithName("fltNum").optional().description("Optional filter parameter flight number"),
                                parameterWithName("fltDate").optional().description("Optional filter parameter flight date"),
                                parameterWithName("fltDow").optional().description("Optional filter parameter day of week of flight date")
                        ),
                        RestdocsUtil.responseFields(
                                subsectionWithPath("_embedded.inventories").description("An array of <<resource_inventory, Inventory resource>>"),
                                subsectionWithPath("page").description("Pagination parameter")
                        )
                ));

    }

    @SneakyThrows
    @Test
    void getInventory() {

        // test inventory
        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        Inventory inventory = inventoryRepository.save(new Inventory("SG", "001", ServiceType.PAX,
                fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(new InventoryLeg(fltDate, fltDate.getDayOfWeek().getValue(),
                        "HKG", "TPE", 1,
                        fltDate.atTime(10, 0), fltDate.atTime(14, 0), 480, 480,
                        100)),
                Instant.now(), Instant.now()));

        mockMvc
                .perform(
                        get("/inventories/" + inventory.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andDo(document("inventory-get",
                        RestdocsUtil.responseFields(
                                fieldWithPath("carrier").description("Flight carrier"),
                                fieldWithPath("fltNum").description("Flight number"),
                                fieldWithPath("serviceType").description("Service type, PAX for passenger, FRTR for freighter"),
                                fieldWithPath("fltDate").description("Flight date in format yyyy-MM-dd"),
                                fieldWithPath("fltDow").type(JsonFieldType.NUMBER).description("Day of week of flight date, from 1 (Monday) to 7 (Sunday)"),
                                fieldWithPath("legs").type(JsonFieldType.ARRAY).description("An array of inventory by leg")
                        ).andWithPrefix("legs[].",
                                fieldWithPath("depDate").description("Departure date in format yyyy-MM-dd, in local timezone"),
                                fieldWithPath("depDow").type(JsonFieldType.NUMBER).description("Day of week of departure date, from 1 (Monday) to 7 (Sunday)"),
                                fieldWithPath("legDep").description("Departure airport of this flight leg"),
                                fieldWithPath("legArr").description("Arrival airport of this flight leg"),
                                fieldWithPath("legSeqNum").type(JsonFieldType.NUMBER).description("Flight leg sequence number of routing, starting from 1"),
                                fieldWithPath("schDepTime").description("Scheduled departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("schArrTime").description("Scheduled arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("depTimeDiff").type(JsonFieldType.NUMBER).description("Time difference of the timezone of departure airport"),
                                fieldWithPath("arrTimeDiff").type(JsonFieldType.NUMBER).description("Time difference of the timezone of arrival airport"),
                                fieldWithPath("available").description("Seat available of this leg")
                        ).and(
                                fieldWithPath("createdDate").ignored(),
                                fieldWithPath("lastModifiedDate").description("Last modified date time")
                        )
                ));

    }
}
