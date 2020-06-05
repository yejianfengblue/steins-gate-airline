package com.yejianfengblue.sga.fltsch.flt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yejianfengblue.sga.fltsch.constant.ServiceType;
import com.yejianfengblue.sga.fltsch.util.RestdocsUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
// the RestDocumentationExtension is auto configured with an output dir target/generated-snippets
@ExtendWith(RestDocumentationExtension.class)
public class FltApiDocumentation {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FltRepository fltRepository;

    @BeforeEach
    public void configMockMvc(WebApplicationContext webAppContext, RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @AfterEach
    void deleteTestData() {
        this.fltRepository.deleteAll();
    }

    @Test
    void getFlts() throws Exception {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        Flt sg001 = new Flt("SG", "001", ServiceType.PAX, fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(
                        new FltLeg(fltDate, fltDate.getDayOfWeek().getValue(), "HKG", "TPE", 1,
                                fltDate.atTime(00, 00), fltDate.atTime(04, 00),
                                fltDate.atTime(00, 00), fltDate.atTime(04, 00),
                                fltDate.atTime(00, 00), fltDate.atTime(04, 00),
                                480, 480, "B-LAD", "333")
                ));
        sg001 = this.fltRepository.save(sg001);

        Flt sg002 = new Flt("SG", "002", ServiceType.PAX, fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(
                        new FltLeg(fltDate, fltDate.getDayOfWeek().getValue(), "TPE", "HKG", 1,
                                fltDate.atTime(12, 00), fltDate.atTime(16, 00),
                                fltDate.atTime(12, 00), fltDate.atTime(16, 00),
                                fltDate.atTime(12, 00), fltDate.atTime(16, 00),
                                480, 480, "B-LAD", "333")
                ));
        sg002 = this.fltRepository.save(sg002);

        this.mockMvc.perform(
                get("/flts")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andDo(document("flts-get",  //  snippet dir "flts" under target/generated-snippets
                        // generate a snippet "links.adoc" under "flts"
                        RestdocsUtil.links(
                                linkWithRel("profile").description("The ALPS profile for this resource"),
                                linkWithRel("search").description("Search for this resource")),
                        // generate a snippet "response-fields.adoc" under "flts"
                        RestdocsUtil.responseFields(
                                subsectionWithPath("_embedded.flts").description("An array of <<resources_flt, Flight resources>>"),
                                subsectionWithPath("page").description("Pagination parameter")
                        )
                ));
    }

    @Test
    void createFlt() throws Exception {

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        HashMap<String, Object> fltLeg = new HashMap<>();
        fltLeg.put("depDate", "2020-01-01");
        fltLeg.put("depDow", 3);
        fltLeg.put("legDep", "HKG");
        fltLeg.put("legArr", "TPE");
        fltLeg.put("legSeqNum", 1);
        fltLeg.put("schDepTime", "2020-01-01T00:00:00");
        fltLeg.put("schArrTime", "2020-01-01T04:00:00");
        fltLeg.put("estDepTime", "2020-01-01T00:00:00");
        fltLeg.put("estArrTime", "2020-01-01T04:00:00");
        fltLeg.put("actDepTime", "2020-01-01T00:00:00");
        fltLeg.put("actArrTime", "2020-01-01T04:00:00");
        fltLeg.put("depTimeDiff", 480);
        fltLeg.put("arrTimeDiff", 480);
        fltLeg.put("acReg", "B-LAD");
        fltLeg.put("iataAcType", "333");
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        RestdocsUtil.ConstrainedFields fltConstrainedFields = new RestdocsUtil.ConstrainedFields(Flt.class);
        RestdocsUtil.ConstrainedFields fltLegConstrainedFields = new RestdocsUtil.ConstrainedFields(FltLeg.class);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andDo(document("flts-create",
                        requestFields(
                                fltConstrainedFields.withPath("carrier").description("Flight carrier"),
                                fltConstrainedFields.withPath("fltNum").description("Flight number"),
                                fltConstrainedFields.withPath("serviceType").description("Service type, PAX for passenger, FRTR for freighter"),
                                fltConstrainedFields.withPath("fltDate").description("Flight date in format yyyy-MM-dd"),
                                fltConstrainedFields.withPath("fltDow").type(JsonFieldType.NUMBER).description("Day of week of flight date, from 1 (Monday) to 7 (Sunday)"),
                                fltConstrainedFields.withPath("fltLegs").type(JsonFieldType.ARRAY).description("An array of flight legs")
                        ).andWithPrefix("fltLegs[].",
                                fltLegConstrainedFields.withPath("depDate").description("Departure date in format yyyy-MM-dd, in local timezone"),
                                fltLegConstrainedFields.withPath("depDow").type(JsonFieldType.NUMBER).description("Day of week of departure date, from 1 (Monday) to 7 (Sunday)"),
                                fltLegConstrainedFields.withPath("legDep").description("Departure airport of this flight leg"),
                                fltLegConstrainedFields.withPath("legArr").description("Arrival airport of this flight leg"),
                                fltLegConstrainedFields.withPath("legSeqNum").type(JsonFieldType.NUMBER).description("Flight leg sequence number of routing, starting from 1"),
                                fltLegConstrainedFields.withPath("schDepTime").description("Scheduled departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("schArrTime").description("Scheduled arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("estDepTime").description("Estimated departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("estArrTime").description("Estimated arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("actDepTime").description("Actual departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("actArrTime").description("Actual arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("depTimeDiff").type(JsonFieldType.NUMBER).description("Time difference in minutes of the timezone of departure airport"),
                                fltLegConstrainedFields.withPath("arrTimeDiff").type(JsonFieldType.NUMBER).description("Time difference in minutes of the timezone of arrival airport"),
                                fltLegConstrainedFields.withPath("acReg").description("Aircraft registration, alternatively called tail number"),
                                fltLegConstrainedFields.withPath("iataAcType").description("IATA aircraft type code")),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Created flt resource location"))
                ));
    }

    @Test
    void createDuplicateFlt() throws Exception {

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        HashMap<String, Object> fltLeg = new HashMap<>();
        fltLeg.put("depDate", "2020-01-01");
        fltLeg.put("depDow", 3);
        fltLeg.put("legDep", "HKG");
        fltLeg.put("legArr", "TPE");
        fltLeg.put("legSeqNum", 1);
        fltLeg.put("schDepTime", "2020-01-01T00:00:00");
        fltLeg.put("schArrTime", "2020-01-01T04:00:00");
        fltLeg.put("estDepTime", "2020-01-01T00:00:00");
        fltLeg.put("estArrTime", "2020-01-01T04:00:00");
        fltLeg.put("actDepTime", "2020-01-01T00:00:00");
        fltLeg.put("actArrTime", "2020-01-01T04:00:00");
        fltLeg.put("depTimeDiff", 480);
        fltLeg.put("arrTimeDiff", 480);
        fltLeg.put("acReg", "B-LAD");
        fltLeg.put("iataAcType", "333");
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        RestdocsUtil.ConstrainedFields fltConstrainedFields = new RestdocsUtil.ConstrainedFields(Flt.class);
        RestdocsUtil.ConstrainedFields fltLegConstrainedFields = new RestdocsUtil.ConstrainedFields(FltLeg.class);

        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated());

        // create duplicate flt
        this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isConflict())
                .andDo(document("flts-create-duplicate"));
    }

    @Test
    void getFlt() throws Exception {

        LocalDate fltDate = LocalDate.of(2020, 1, 1);
        Flt flt = new Flt("SG", "001", ServiceType.PAX, fltDate, fltDate.getDayOfWeek().getValue(),
                List.of(
                        new FltLeg(fltDate, fltDate.getDayOfWeek().getValue(), "HKG", "TPE", 1,
                                fltDate.atTime(00, 00), fltDate.atTime(04, 00),
                                fltDate.atTime(00, 00), fltDate.atTime(04, 00),
                                fltDate.atTime(00, 00), fltDate.atTime(04, 00),
                                480, 480, "B-LAD", "333")
                ));

        flt = this.fltRepository.save(flt);

        this.mockMvc.perform(
                get("/flts/" + flt.getId())
                        .accept(RestMediaTypes.HAL_JSON)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("carrier").value(flt.getCarrier()))
                .andExpect(jsonPath("fltNum").value(flt.getFltNum()))
                .andExpect(jsonPath("serviceType").value(flt.getServiceType().toString()))
                .andExpect(jsonPath("fltDate").value(flt.getFltDate().format(ISO_DATE)))
                .andExpect(jsonPath("fltDow").value(flt.getFltDow()))
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(1)))
                .andExpect(jsonPath("fltLegs[0].depDate").value(flt.getFltLegs().get(0).getDepDate().format(ISO_DATE)))
                .andExpect(jsonPath("fltLegs[0].depDow").value(flt.getFltLegs().get(0).getDepDow()))
                .andExpect(jsonPath("fltLegs[0].legDep").value(flt.getFltLegs().get(0).getLegDep()))
                .andExpect(jsonPath("fltLegs[0].legArr").value(flt.getFltLegs().get(0).getLegArr()))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(flt.getFltLegs().get(0).getLegSeqNum()))
                .andExpect(jsonPath("fltLegs[0].schDepTime").value(flt.getFltLegs().get(0).getSchDepTime().format(ISO_DATE_TIME)))
                .andExpect(jsonPath("fltLegs[0].schArrTime").value(flt.getFltLegs().get(0).getSchArrTime().format(ISO_DATE_TIME)))
                .andExpect(jsonPath("fltLegs[0].estDepTime").value(flt.getFltLegs().get(0).getEstDepTime().format(ISO_DATE_TIME)))
                .andExpect(jsonPath("fltLegs[0].estArrTime").value(flt.getFltLegs().get(0).getEstArrTime().format(ISO_DATE_TIME)))
                .andExpect(jsonPath("fltLegs[0].actDepTime").value(flt.getFltLegs().get(0).getActDepTime().format(ISO_DATE_TIME)))
                .andExpect(jsonPath("fltLegs[0].actArrTime").value(flt.getFltLegs().get(0).getActArrTime().format(ISO_DATE_TIME)))
                .andExpect(jsonPath("fltLegs[0].depTimeDiff").value(flt.getFltLegs().get(0).getDepTimeDiff()))
                .andExpect(jsonPath("fltLegs[0].arrTimeDiff").value(flt.getFltLegs().get(0).getArrTimeDiff()))
                .andExpect(jsonPath("fltLegs[0].acReg").value(flt.getFltLegs().get(0).getAcReg()))
                .andExpect(jsonPath("fltLegs[0].iataAcType").value(flt.getFltLegs().get(0).getIataAcType()))
                .andDo(document("flt-get",
                        RestdocsUtil.links(
                                linkWithRel("flt").description("The flight itself")),
                        RestdocsUtil.responseFields(
                                fieldWithPath("carrier").description("Flight carrier"),
                                fieldWithPath("fltNum").description("Flight number"),
                                fieldWithPath("serviceType").description("Service type, PAX for passenger, FRTR for freighter"),
                                fieldWithPath("fltDate").description("Flight date in format yyyy-MM-dd"),
                                fieldWithPath("fltDow").type(JsonFieldType.NUMBER).description("Day of week of flight date, from 1 (Monday) to 7 (Sunday)"),
                                fieldWithPath("fltLegs").type(JsonFieldType.ARRAY).description("An array of flight legs")
                        ).andWithPrefix("fltLegs[].",
                                fieldWithPath("depDate").description("Departure date in format yyyy-MM-dd, in local timezone"),
                                fieldWithPath("depDow").type(JsonFieldType.NUMBER).description("Day of week of departure date, from 1 (Monday) to 7 (Sunday)"),
                                fieldWithPath("legDep").description("Departure airport of this flight leg"),
                                fieldWithPath("legArr").description("Arrival airport of this flight leg"),
                                fieldWithPath("legSeqNum").type(JsonFieldType.NUMBER).description("Flight leg sequence number of routing, starting from 1"),
                                fieldWithPath("schDepTime").description("Scheduled departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("schArrTime").description("Scheduled arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("estDepTime").description("Estimated departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("estArrTime").description("Estimated arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("actDepTime").description("Actual departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("actArrTime").description("Actual arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fieldWithPath("depTimeDiff").type(JsonFieldType.NUMBER).description("Time difference of the timezone of departure airport"),
                                fieldWithPath("arrTimeDiff").type(JsonFieldType.NUMBER).description("Time difference of the timezone of arrival airport"),
                                fieldWithPath("acReg").description("Aircraft registration, alternatively called tail number"),
                                fieldWithPath("iataAcType").description("IATA aircraft type code")
                        ).and(RestdocsUtil.AUDIT_FIELDS)
                ));
    }

    @Test
    void updateFlt() throws Exception {

        // create
        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        HashMap<String, Object> fltLeg = new HashMap<>();
        fltLeg.put("depDate", "2020-01-01");
        fltLeg.put("depDow", 3);
        fltLeg.put("legDep", "HKG");
        fltLeg.put("legArr", "TPE");
        fltLeg.put("legSeqNum", 1);
        fltLeg.put("schDepTime", "2020-01-01T00:00:00");
        fltLeg.put("schArrTime", "2020-01-01T04:00:00");
        fltLeg.put("estDepTime", "2020-01-01T00:00:00");
        fltLeg.put("estArrTime", "2020-01-01T04:00:00");
        fltLeg.put("actDepTime", "2020-01-01T00:00:00");
        fltLeg.put("actArrTime", "2020-01-01T04:00:00");
        fltLeg.put("depTimeDiff", 480);
        fltLeg.put("arrTimeDiff", 480);
        fltLeg.put("acReg", "B-LAD");
        fltLeg.put("iataAcType", "333");
        fltPostRequestPayload.put("fltLegs", List.of(fltLeg));

        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // update
        HashMap<String, Object> updatedFltLeg = new HashMap<>();
        updatedFltLeg.put("depDate", "2020-01-01");
        updatedFltLeg.put("depDow", 3);
        updatedFltLeg.put("legDep", "HKG");
        updatedFltLeg.put("legArr", "TPE");
        updatedFltLeg.put("legSeqNum", 1);
        updatedFltLeg.put("schDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("schArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("estDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("estArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("actDepTime", "2020-01-01T00:00:00");
        updatedFltLeg.put("actArrTime", "2020-01-01T04:00:00");
        updatedFltLeg.put("depTimeDiff", 480);
        updatedFltLeg.put("arrTimeDiff", 480);
        updatedFltLeg.put("acReg", "B-HNK");
        updatedFltLeg.put("iataAcType", "773");
        HashMap<String, Object> updateRequestPayload = new HashMap<>();
        updateRequestPayload.put("fltLegs", List.of(updatedFltLeg));

        RestdocsUtil.ConstrainedFields fltConstrainedFields = new RestdocsUtil.ConstrainedFields(Flt.class);
        RestdocsUtil.ConstrainedFields fltLegConstrainedFields = new RestdocsUtil.ConstrainedFields(FltLeg.class);

        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestPayload))
                                .with(jwt()))
                .andExpect(status().isNoContent())
                .andDo(document("flt-update",
                        requestFields(
                                fltConstrainedFields.withPath("fltLegs").type(JsonFieldType.ARRAY).description("An array of flight legs")
                        ).andWithPrefix("fltLegs[].",
                                fltLegConstrainedFields.withPath("depDate").description("Departure date in format yyyy-MM-dd, in local timezone"),
                                fltLegConstrainedFields.withPath("depDow").type(JsonFieldType.NUMBER).description("Day of week of departure date, from 1 (Monday) to 7 (Sunday)"),
                                fltLegConstrainedFields.withPath("legDep").description("Departure airport of this flight leg"),
                                fltLegConstrainedFields.withPath("legArr").description("Arrival airport of this flight leg"),
                                fltLegConstrainedFields.withPath("legSeqNum").type(JsonFieldType.NUMBER).description("Flight leg sequence number of routing, starting from 1"),
                                fltLegConstrainedFields.withPath("schDepTime").description("Scheduled departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("schArrTime").description("Scheduled arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("estDepTime").description("Estimated departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("estArrTime").description("Estimated arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("actDepTime").description("Actual departure time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("actArrTime").description("Actual arrival time, in local timezone, in format yyyy-MM-ddTHH:mm:ss"),
                                fltLegConstrainedFields.withPath("depTimeDiff").type(JsonFieldType.NUMBER).description("Time difference in minutes of the timezone of departure airport"),
                                fltLegConstrainedFields.withPath("arrTimeDiff").type(JsonFieldType.NUMBER).description("Time difference in minutes of the timezone of arrival airport"),
                                fltLegConstrainedFields.withPath("acReg").description("Aircraft registration, alternatively called tail number"),
                                fltLegConstrainedFields.withPath("iataAcType").description("IATA aircraft type code"))
                ));

        // verify
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(updateRequestPayload))
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs[0].acReg").value(updatedFltLeg.get("acReg")))
                .andExpect(jsonPath("fltLegs[0].iataAcType").value(updatedFltLeg.get("iataAcType")));
    }


}
