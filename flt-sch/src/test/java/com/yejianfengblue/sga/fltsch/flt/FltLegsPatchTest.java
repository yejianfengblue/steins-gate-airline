package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ensure the bean validation works when create flt via HTTP POST request and update flt via HTTP PATCH request
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class FltLegsPatchTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void givenFltContainingTwoFltLegs_whenPatchWithEmptyJson_thenFltLegsRemainSame() throws Exception {

        HashMap<String, Object> fltLegHkgTpe = new HashMap<>();
        fltLegHkgTpe.put("depDate", "2020-01-01");
        fltLegHkgTpe.put("depDow", 3);
        fltLegHkgTpe.put("legDep", "HKG");
        fltLegHkgTpe.put("legArr", "TPE");
        fltLegHkgTpe.put("legSeqNum", 1);
        fltLegHkgTpe.put("schDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("schArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("estDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("estArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("actDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("actArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("depTimeDiff", 480);
        fltLegHkgTpe.put("arrTimeDiff", 480);
        fltLegHkgTpe.put("acReg", "B-LAD");
        fltLegHkgTpe.put("iataAcType", "333");

        HashMap<String, Object> fltLegTpeNrt = new HashMap<>();
        fltLegTpeNrt.put("depDate", "2020-01-01");
        fltLegTpeNrt.put("depDow", 3);
        fltLegTpeNrt.put("legDep", "TPE");
        fltLegTpeNrt.put("legArr", "NRT");
        fltLegTpeNrt.put("legSeqNum", 2);
        fltLegTpeNrt.put("schDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("schArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("estDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("estArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("actDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("actArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("depTimeDiff", 480);
        fltLegTpeNrt.put("arrTimeDiff", 540);
        fltLegTpeNrt.put("acReg", "B-LAD");
        fltLegTpeNrt.put("iataAcType", "333");

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        fltPostRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));

        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // given
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));

        // when, PATCH update, empty JSON
        HashMap<String, Object> fltPatchRequestPayload = new HashMap<>();
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload))
                                .with(jwt()))
                .andExpect(status().isNoContent());

        // then, fltLegs remain same as before
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));
    }

    @Test
    void givenFltContainingTwoFltLegs_whenPatchJsonContainingOnlySecondFltLeg_thenFirstFltLegIsRemoved() throws Exception {

        HashMap<String, Object> fltLegHkgTpe = new HashMap<>();
        fltLegHkgTpe.put("depDate", "2020-01-01");
        fltLegHkgTpe.put("depDow", 3);
        fltLegHkgTpe.put("legDep", "HKG");
        fltLegHkgTpe.put("legArr", "TPE");
        fltLegHkgTpe.put("legSeqNum", 1);
        fltLegHkgTpe.put("schDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("schArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("estDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("estArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("actDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("actArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("depTimeDiff", 480);
        fltLegHkgTpe.put("arrTimeDiff", 480);
        fltLegHkgTpe.put("acReg", "B-LAD");
        fltLegHkgTpe.put("iataAcType", "333");

        HashMap<String, Object> fltLegTpeNrt = new HashMap<>();
        fltLegTpeNrt.put("depDate", "2020-01-01");
        fltLegTpeNrt.put("depDow", 3);
        fltLegTpeNrt.put("legDep", "TPE");
        fltLegTpeNrt.put("legArr", "NRT");
        fltLegTpeNrt.put("legSeqNum", 2);
        fltLegTpeNrt.put("schDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("schArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("estDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("estArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("actDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("actArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("depTimeDiff", 480);
        fltLegTpeNrt.put("arrTimeDiff", 540);
        fltLegTpeNrt.put("acReg", "B-LAD");
        fltLegTpeNrt.put("iataAcType", "333");

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        fltPostRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));

        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // given
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));

        // when, PATCH update, JSON contains 2nd FltLeg only
        HashMap<String, Object> fltPatchRequestPayload = new HashMap<>(fltPostRequestPayload);
        fltPatchRequestPayload.put("fltLegs", List.of(fltLegTpeNrt));
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload))
                                .with(jwt()))
                .andExpect(status().isNoContent());

        // then, first FltLeg is removed
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(1)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[0].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"));
    }

    @Test
    void givenFltContainingTwoFltLegs_whenPatchJsonContainingOnlyFirstFltLeg_thenSecondFltLegIsRemoved() throws Exception {

        HashMap<String, Object> fltLegHkgTpe = new HashMap<>();
        fltLegHkgTpe.put("depDate", "2020-01-01");
        fltLegHkgTpe.put("depDow", 3);
        fltLegHkgTpe.put("legDep", "HKG");
        fltLegHkgTpe.put("legArr", "TPE");
        fltLegHkgTpe.put("legSeqNum", 1);
        fltLegHkgTpe.put("schDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("schArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("estDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("estArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("actDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("actArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("depTimeDiff", 480);
        fltLegHkgTpe.put("arrTimeDiff", 480);
        fltLegHkgTpe.put("acReg", "B-LAD");
        fltLegHkgTpe.put("iataAcType", "333");

        HashMap<String, Object> fltLegTpeNrt = new HashMap<>();
        fltLegTpeNrt.put("depDate", "2020-01-01");
        fltLegTpeNrt.put("depDow", 3);
        fltLegTpeNrt.put("legDep", "TPE");
        fltLegTpeNrt.put("legArr", "NRT");
        fltLegTpeNrt.put("legSeqNum", 2);
        fltLegTpeNrt.put("schDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("schArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("estDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("estArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("actDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("actArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("depTimeDiff", 480);
        fltLegTpeNrt.put("arrTimeDiff", 540);
        fltLegTpeNrt.put("acReg", "B-LAD");
        fltLegTpeNrt.put("iataAcType", "333");

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        fltPostRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));

        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // given
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));

        // when, PATCH update, JSON contains 1st FltLeg only
        HashMap<String, Object> fltPatchRequestPayload = new HashMap<>(fltPostRequestPayload);
        fltPatchRequestPayload.put("fltLegs", List.of(fltLegHkgTpe));
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload))
                                .with(jwt()))
                .andExpect(status().isNoContent());

        // then, 2nd FltLeg is removed
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(1)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"));
    }

    @Test
    void givenFltContainingTwoFltLegs_whenPatchJsonContainFirstFltLegAndThirdFltLeg_thenSecondFltLegIsRemovedAndThirdFltLegIsAdded() throws Exception {

        HashMap<String, Object> fltLegHkgTpe = new HashMap<>();
        fltLegHkgTpe.put("depDate", "2020-01-01");
        fltLegHkgTpe.put("depDow", 3);
        fltLegHkgTpe.put("legDep", "HKG");
        fltLegHkgTpe.put("legArr", "TPE");
        fltLegHkgTpe.put("legSeqNum", 1);
        fltLegHkgTpe.put("schDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("schArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("estDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("estArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("actDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("actArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("depTimeDiff", 480);
        fltLegHkgTpe.put("arrTimeDiff", 480);
        fltLegHkgTpe.put("acReg", "B-LAD");
        fltLegHkgTpe.put("iataAcType", "333");

        HashMap<String, Object> fltLegTpeNrt = new HashMap<>();
        fltLegTpeNrt.put("depDate", "2020-01-01");
        fltLegTpeNrt.put("depDow", 3);
        fltLegTpeNrt.put("legDep", "TPE");
        fltLegTpeNrt.put("legArr", "NRT");
        fltLegTpeNrt.put("legSeqNum", 2);
        fltLegTpeNrt.put("schDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("schArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("estDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("estArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("actDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("actArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("depTimeDiff", 480);
        fltLegTpeNrt.put("arrTimeDiff", 540);
        fltLegTpeNrt.put("acReg", "B-LAD");
        fltLegTpeNrt.put("iataAcType", "333");

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        fltPostRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));

        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // given
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));

        // when, PATCH update, JSON contains 1st FltLeg and the 3rd new FltLeg
        HashMap<String, Object> fltLegNrtXyz = new HashMap<>();
        fltLegNrtXyz.put("depDate", "2020-01-01");
        fltLegNrtXyz.put("depDow", 3);
        fltLegNrtXyz.put("legDep", "NRT");
        fltLegNrtXyz.put("legArr", "XYZ");
        fltLegNrtXyz.put("legSeqNum", 3);
        fltLegNrtXyz.put("schDepTime", "2020-01-01T20:00:00");
        fltLegNrtXyz.put("schArrTime", "2020-01-01T22:00:00");
        fltLegNrtXyz.put("estDepTime", "2020-01-01T20:00:00");
        fltLegNrtXyz.put("estArrTime", "2020-01-01T22:00:00");
        fltLegNrtXyz.put("actDepTime", "2020-01-01T20:00:00");
        fltLegNrtXyz.put("actArrTime", "2020-01-01T22:00:00");
        fltLegNrtXyz.put("depTimeDiff", 540);
        fltLegNrtXyz.put("arrTimeDiff", 540);
        fltLegNrtXyz.put("acReg", "B-LAD");
        fltLegNrtXyz.put("iataAcType", "333");

        HashMap<String, Object> fltPatchRequestPayload = new HashMap<>(fltPostRequestPayload);
        fltPatchRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegNrtXyz));
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload))
                                .with(jwt()))
                .andExpect(status().isNoContent());

        // then, 2nd FltLeg is removed, 3nd FltLeg is added
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(3))
                .andExpect(jsonPath("fltLegs[1].legDep").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("XYZ"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));
    }

    @Test
    void givenFltContainingTwoFltLegs_whenPatchJsonContainBothFltLegAndSecondFltLegValueChanged_thenSecondFltLegIsUpdated() throws Exception {

        HashMap<String, Object> fltLegHkgTpe = new HashMap<>();
        fltLegHkgTpe.put("depDate", "2020-01-01");
        fltLegHkgTpe.put("depDow", 3);
        fltLegHkgTpe.put("legDep", "HKG");
        fltLegHkgTpe.put("legArr", "TPE");
        fltLegHkgTpe.put("legSeqNum", 1);
        fltLegHkgTpe.put("schDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("schArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("estDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("estArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("actDepTime", "2020-01-01T00:00:00");
        fltLegHkgTpe.put("actArrTime", "2020-01-01T04:00:00");
        fltLegHkgTpe.put("depTimeDiff", 480);
        fltLegHkgTpe.put("arrTimeDiff", 480);
        fltLegHkgTpe.put("acReg", "B-LAD");
        fltLegHkgTpe.put("iataAcType", "333");

        HashMap<String, Object> fltLegTpeNrt = new HashMap<>();
        fltLegTpeNrt.put("depDate", "2020-01-01");
        fltLegTpeNrt.put("depDow", 3);
        fltLegTpeNrt.put("legDep", "TPE");
        fltLegTpeNrt.put("legArr", "NRT");
        fltLegTpeNrt.put("legSeqNum", 2);
        fltLegTpeNrt.put("schDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("schArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("estDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("estArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("actDepTime", "2020-01-01T12:00:00");
        fltLegTpeNrt.put("actArrTime", "2020-01-01T16:00:00");
        fltLegTpeNrt.put("depTimeDiff", 480);
        fltLegTpeNrt.put("arrTimeDiff", 540);
        fltLegTpeNrt.put("acReg", "B-LAD");
        fltLegTpeNrt.put("iataAcType", "333");

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        fltPostRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));

        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload))
                                .with(jwt()))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // given
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-LAD"));

        // when, PATCH update, JSON contains original 1st FltLeg and updated 2nd FltLeg
        fltLegTpeNrt.put("acReg", "B-HNK");
        fltLegTpeNrt.put("iataAcType", "773");

        HashMap<String, Object> fltPatchRequestPayload = new HashMap<>(fltPostRequestPayload);
        fltPatchRequestPayload.put("fltLegs", List.of(fltLegHkgTpe, fltLegTpeNrt));
        this.mockMvc
                .perform(
                        patch(fltLocation)
                                .contentType(RestMediaTypes.MERGE_PATCH_JSON)
                                .content(objectMapper.writeValueAsString(fltPatchRequestPayload))
                                .with(jwt()))
                .andExpect(status().isNoContent());

        // then, 2nd FltLeg is updated
        this.mockMvc
                .perform(
                        get(fltLocation)
                                .accept(RestMediaTypes.HAL_JSON)
                                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("fltLegs").isArray())
                .andExpect(jsonPath("fltLegs", hasSize(2)))
                .andExpect(jsonPath("fltLegs[0].legSeqNum").value(1))
                .andExpect(jsonPath("fltLegs[0].legDep").value("HKG"))
                .andExpect(jsonPath("fltLegs[0].legArr").value("TPE"))
                .andExpect(jsonPath("fltLegs[0].acReg").value("B-LAD"))
                .andExpect(jsonPath("fltLegs[0].iataAcType").value("333"))
                .andExpect(jsonPath("fltLegs[1].legSeqNum").value(2))
                .andExpect(jsonPath("fltLegs[1].legDep").value("TPE"))
                .andExpect(jsonPath("fltLegs[1].legArr").value("NRT"))
                .andExpect(jsonPath("fltLegs[1].acReg").value("B-HNK"))
                .andExpect(jsonPath("fltLegs[1].iataAcType").value("773"));
    }

}
