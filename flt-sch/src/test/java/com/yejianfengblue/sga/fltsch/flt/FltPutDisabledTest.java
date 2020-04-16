package com.yejianfengblue.sga.fltsch.flt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.RestMediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FltPutDisabledTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void putCreateIsNotAllowed() throws Exception {

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();

        this.mockMvc
                .perform(
                        put("/flts/" + UUID.randomUUID().toString())
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void putUpdateIsNotAllowed() throws Exception {

        HashMap<String, Object> fltPostRequestPayload = validFltWith1Leg();

        // create a flt with fltNum 001
        String fltLocation = this.mockMvc
                .perform(
                        post("/flts")
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse()
                .getHeader(HttpHeaders.LOCATION);

        // get the created flt
        this.mockMvc
                .perform(
                        get(fltLocation).accept(RestMediaTypes.HAL_JSON))
                .andExpect(status().isOk());

        // PUT update
        this.mockMvc
                .perform(
                        put(fltLocation)
                                .contentType(RestMediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(fltPostRequestPayload)))
                .andExpect(status().isMethodNotAllowed());
    }

    private static HashMap<String, Object> validFltWith1Leg() {

        HashMap<String, Object> fltPostRequestPayload = new HashMap<>();
        fltPostRequestPayload.put("carrier", "SG");
        fltPostRequestPayload.put("fltNum", "001");
        fltPostRequestPayload.put("serviceType", "PAX");
        fltPostRequestPayload.put("fltDate", "2020-01-01");
        fltPostRequestPayload.put("fltDow", 3);
        fltPostRequestPayload.put("fltLegs", List.of(validFltLeg()));

        return fltPostRequestPayload;
    }

    private static HashMap<String, Object> validFltLeg() {

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

        return fltLeg;
    }
}
